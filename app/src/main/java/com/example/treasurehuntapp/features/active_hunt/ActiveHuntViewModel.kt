package com.example.treasurehuntapp.features.active_hunt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.treasurehuntapp.data.local.CurrentHuntStorage
import com.example.treasurehuntapp.data.repository.LocationsRepository
import com.example.treasurehuntapp.data.repository.RealtimeRepository
import com.example.treasurehuntapp.data.repository.TreasureHuntRepository
import com.example.treasurehuntapp.data.repository.UserAnswerRepository
import com.example.treasurehuntapp.data.repository.UserProgressRepository
import com.example.treasurehuntapp.data.source.location.DeviceLocationDataSource
import com.example.treasurehuntapp.data.source.realtime.TrackingSocketEvent
import com.example.treasurehuntapp.data.source.remote.dto.hunt.ParticipantDto
import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.LocationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class ActiveHuntViewModel @Inject constructor(
    private val repo: TreasureHuntRepository,
    private val storage: CurrentHuntStorage,
    private val locationsRepository: LocationsRepository,
    private val realtimeRepository: RealtimeRepository,
    private val deviceLocationDataSource: DeviceLocationDataSource,
    private val userAnswerRepository: UserAnswerRepository,
    private val userProgressRepository: UserProgressRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ActiveHuntVM"
    }

    private val _state = MutableStateFlow(ActiveHuntState())
    val state = _state.asStateFlow()
    private var trackingHuntId: String? = null
    private var locationJob: Job? = null
    private var locationPermissionGranted: Boolean = false

    init {
        observeTrackingEvents()
    }

    fun load() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                val storedHuntId = storage.huntIdFlow.first()
                val memberships = repo.getMemberships()
                val resolvedHuntId = if (!storedHuntId.isNullOrBlank()) {
                    storedHuntId
                } else {
                    val all = memberships.map { it.treasureHunt }
                    val active = all.firstOrNull { isActive(it) } ?: all.firstOrNull()
                    if (active != null) {
                        storage.saveHunt(active.id, active.code)
                        active.id
                    } else {
                        null
                    }
                }

                if (resolvedHuntId.isNullOrBlank()) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "No active hunt saved"
                    )
                    return@launch
                }

                val hunt = repo.getHuntById(resolvedHuntId)
                val role = memberships.firstOrNull { it.treasureHunt.id == hunt.id }?.role
                val isOwner = role?.equals("owner", ignoreCase = true) == true

                var participantsCount = 0
                var participants = emptyList<ParticipantDto>()
                var locations = emptyList<LocationDto>()
                var currentLocationId: String? = null
                var completedLocationIds: List<String> = emptyList()
                var partialError: String? = null

                try {
                    participants = repo.getParticipants(hunt.id)
                    participantsCount = participants.size
                } catch (e: Exception) {
                    partialError = e.message ?: "Failed to load participants"
                }

                try {
                    locations = locationsRepository.getLocationsByHunt(hunt.id)
                        .sortedBy { it.createdAt }
                } catch (e: Exception) {
                    val msg = e.message ?: "Failed to load locations"
                    partialError = if (partialError == null) msg else "$partialError; $msg"
                }

                try {
                    val progress = userProgressRepository.getByHunt(hunt.id)
                    currentLocationId = progress.currentLocationId
                    completedLocationIds = progress.completedLocations
                } catch (e: HttpException) {
                    if (!isOwner && e.code() == 404) {
                        runCatching { userProgressRepository.start(hunt.id) }
                        runCatching { userProgressRepository.getByHunt(hunt.id) }.getOrNull()?.let { progress ->
                            currentLocationId = progress.currentLocationId
                            completedLocationIds = progress.completedLocations
                        }
                    }
                } catch (e: Exception) {
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    hunt = hunt,
                    participantsCount = participantsCount,
                    participants = participants,
                    locations = locations,
                    currentLocationId = currentLocationId,
                    completedLocationIds = completedLocationIds,
                    participantProgressByUserId = emptyMap(),
                    participantProgressLoadingUserId = null,
                    participantProgressError = null,
                    isOwner = isOwner,
                    answerError = null,
                    error = partialError
                )

                if (realtimeRepository.connectTracking()) {
                    if (trackingHuntId != null && trackingHuntId != hunt.id) {
                        realtimeRepository.leaveTrackingRoom(trackingHuntId!!)
                    }
                    trackingHuntId = hunt.id
                    realtimeRepository.joinTrackingRoom(hunt.id)
                    startLocationStreamingIfPossible()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load hunt"
                )
            }
        }
    }

    fun leaveHunt() {
        viewModelScope.launch {
            trackingHuntId?.let { realtimeRepository.leaveTrackingRoom(it) }
            trackingHuntId = null
            locationJob?.cancel()
            locationJob = null
            storage.clear()
            _state.value = ActiveHuntState()
        }
    }

    fun onLocationPermissionResult(granted: Boolean) {
        locationPermissionGranted = granted
        if (granted) {
            startLocationStreamingIfPossible()
        } else {
            locationJob?.cancel()
            locationJob = null
        }
    }

    fun submitAnswerForLocation(
        locationId: String,
        answer: String,
        onCorrectAnswer: (isLastLocation: Boolean) -> Unit
    ) {
        val trimmed = answer.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(answerError = "Answer cannot be empty") }
            return
        }

        viewModelScope.launch {
            val huntId = _state.value.hunt?.id
            if (huntId.isNullOrBlank()) {
                _state.update { it.copy(answerError = "No active hunt loaded") }
                return@launch
            }

            try {
                _state.update { it.copy(isSubmittingAnswer = true, answerError = null) }
                Log.d(TAG, "submitAnswer request: huntId=$huntId locationId=$locationId answer=${trimmed.take(64)}")
                val result = try {
                    userAnswerRepository.submitAnswer(locationId = locationId, answer = trimmed)
                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        userProgressRepository.start(huntId)
                        userAnswerRepository.submitAnswer(locationId = locationId, answer = trimmed)
                    } else {
                        throw e
                    }
                }
                if (!result.isCorrect) {
                    _state.update {
                        it.copy(
                            isSubmittingAnswer = false,
                            answerError = "Incorrect answer. Please try again!"
                        )
                    }
                    return@launch
                }

                val progress = result.progress
                val completed = progress?.completedLocations ?: _state.value.completedLocationIds
                val nextLocationId = progress?.currentLocationId
                val totalLocations = _state.value.locations.size
                val isLastLocation = nextLocationId == null && totalLocations > 0 &&
                    completed.size >= totalLocations

                _state.update {
                    it.copy(
                        isSubmittingAnswer = false,
                        answerError = null,
                        currentLocationId = nextLocationId,
                        completedLocationIds = completed,
                        showMissionAccomplished = isLastLocation
                    )
                }

                load()
                onCorrectAnswer(isLastLocation)
            } catch (e: HttpException) {
                val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                val parsedMessage = parseApiErrorMessage(raw)
                Log.e(
                    TAG,
                    "submitAnswer failed: code=${e.code()} body=${raw ?: "<empty>"}"
                )
                _state.update {
                    it.copy(
                        isSubmittingAnswer = false,
                        answerError = parsedMessage ?: "Failed to submit answer (${e.code()})"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSubmittingAnswer = false,
                        answerError = e.message ?: "Failed to submit answer"
                    )
                }
            }
        }
    }

    fun dismissMissionAccomplished() {
        _state.update { it.copy(showMissionAccomplished = false) }
    }

    fun deleteLocation(locationId: String) {
        viewModelScope.launch {
            try {
                locationsRepository.deleteLocation(locationId)
                _state.update { current ->
                    current.copy(
                        locations = current.locations.filterNot { it.id == locationId },
                        error = null,
                        successMessage = "Location deleted"
                    )
                }
                load()
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = e.message ?: "Failed to delete location")
                }
            }
        }
    }

    fun loadParticipantProgressForOwner(userId: String) {
        val huntId = _state.value.hunt?.id
        if (huntId.isNullOrBlank() || userId.isBlank()) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    participantProgressLoadingUserId = userId,
                    participantProgressError = null
                )
            }
            try {
                val progress = userProgressRepository.getByHuntForUser(huntId, userId)
                _state.update { current ->
                    current.copy(
                        participantProgressByUserId = current.participantProgressByUserId + (userId to progress),
                        participantProgressLoadingUserId = null,
                        participantProgressError = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        participantProgressLoadingUserId = null,
                        participantProgressError = e.message ?: "Failed to load participant progress"
                    )
                }
            }
        }
    }

    fun addOwner(userId: String) {
        val huntId = _state.value.hunt?.id
        if (huntId.isNullOrBlank()) {
            _state.update { it.copy(error = "No active hunt loaded") }
            return
        }
        val trimmed = userId.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(error = "User ID is required") }
            return
        }

        viewModelScope.launch {
            try {
                repo.addOwner(huntId, trimmed)
                _state.update { it.copy(error = null, successMessage = "Owner added successfully") }
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to add owner") }
            }
        }
    }

    fun removeParticipant(userId: String) {
        val huntId = _state.value.hunt?.id
        if (huntId.isNullOrBlank()) {
            _state.update { it.copy(error = "No active hunt loaded") }
            return
        }
        val trimmed = userId.trim()
        if (trimmed.isBlank()) {
            _state.update { it.copy(error = "User ID is required") }
            return
        }

        viewModelScope.launch {
            try {
                repo.removeParticipant(huntId, trimmed)
                _state.update {
                    it.copy(
                        error = null,
                        successMessage = "Participant removed"
                    )
                }
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to remove participant") }
            }
        }
    }

    fun updateParticipantRole(userId: String, role: String) {
        val huntId = _state.value.hunt?.id
        if (huntId.isNullOrBlank()) {
            _state.update { it.copy(error = "No active hunt loaded") }
            return
        }
        val trimmedUserId = userId.trim()
        val trimmedRole = role.trim()
        if (trimmedUserId.isBlank() || trimmedRole.isBlank()) {
            _state.update { it.copy(error = "User and role are required") }
            return
        }

        viewModelScope.launch {
            try {
                repo.updateParticipantRole(huntId, trimmedUserId, trimmedRole)
                _state.update { it.copy(error = null, successMessage = "Participant role updated") }
                load()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to update participant role") }
            }
        }
    }

    fun abandonHunt(onAbandoned: () -> Unit) {
        val huntId = _state.value.hunt?.id
        if (huntId.isNullOrBlank()) {
            _state.update { it.copy(error = "No active hunt loaded") }
            return
        }

        viewModelScope.launch {
            try {
                userProgressRepository.abandon(huntId)
                leaveHunt()
                onAbandoned()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to abandon hunt") }
            }
        }
    }

    fun deleteHunt(onDeleted: () -> Unit) {
        val huntId = _state.value.hunt?.id
        if (huntId.isNullOrBlank()) {
            _state.update { it.copy(error = "No active hunt loaded") }
            return
        }

        viewModelScope.launch {
            try {
                repo.deleteHunt(huntId)
                storage.clear()
                _state.value = ActiveHuntState(successMessage = "Hunt deleted")
                onDeleted()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message ?: "Failed to delete hunt") }
            }
        }
    }

    fun clearFeedback() {
        _state.update { it.copy(error = null, successMessage = null) }
    }

    private fun observeTrackingEvents() {
        viewModelScope.launch {
            realtimeRepository.trackingEvents.collect { event ->
                when (event) {
                    is TrackingSocketEvent.ParticipantJoined -> {
                        _state.update { current ->
                            current.copy(participantsCount = current.participantsCount + 1)
                        }
                    }

                    is TrackingSocketEvent.ParticipantLeft -> {
                        _state.update { current ->
                            current.copy(
                                participantsCount = (current.participantsCount - 1).coerceAtLeast(0),
                                participantLivePositions = current.participantLivePositions - event.userId
                            )
                        }
                    }

                    is TrackingSocketEvent.PositionUpdated -> {
                        _state.update { current ->
                            current.copy(
                                participantLivePositions = current.participantLivePositions + (
                                    event.userId to ParticipantLivePosition(
                                        userId = event.userId,
                                        lat = event.lat,
                                        lng = event.lng,
                                        ts = event.ts,
                                        currentLocationId = event.currentLocationId
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        trackingHuntId?.let { realtimeRepository.leaveTrackingRoom(it) }
        locationJob?.cancel()
        realtimeRepository.disconnectTracking()
        super.onCleared()
    }

    private fun startLocationStreamingIfPossible() {
        val huntId = trackingHuntId ?: return
        if (!locationPermissionGranted || !deviceLocationDataSource.hasPermission()) return
        if (locationJob?.isActive == true) return

        locationJob = viewModelScope.launch {
            deviceLocationDataSource.locationUpdates(intervalMs = 1000L).collect { location ->
                _state.update { current ->
                    current.copy(
                        myLat = location.latitude,
                        myLng = location.longitude
                    )
                }
                runCatching {
                    userProgressRepository.updatePosition(
                        huntId = huntId,
                        lat = location.latitude,
                        lng = location.longitude
                    )
                }
                realtimeRepository.updateTrackingPosition(
                    huntId = huntId,
                    lat = location.latitude,
                    lng = location.longitude,
                    ts = Instant.ofEpochMilli(location.time).toString()
                )
            }
        }
    }

    private fun parseIsoMillis(iso: String?): Long? {
        val value = iso?.trim()
        if (value.isNullOrEmpty()) return null

        val patterns = arrayOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )

        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val d = sdf.parse(value)
                if (d != null) return d.time
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun isActive(hunt: TreasureHuntDto): Boolean {
        val start = parseIsoMillis(hunt.start) ?: return false
        val end = parseIsoMillis(hunt.end) ?: return false
        val now = System.currentTimeMillis()
        return now in start..end
    }

    private fun parseApiErrorMessage(raw: String?): String? {
        val body = raw?.trim().orEmpty()
        if (body.isBlank()) return null
        Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
            .find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        return body.take(220)
    }
}
