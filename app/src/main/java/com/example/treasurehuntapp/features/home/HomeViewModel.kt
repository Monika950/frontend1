package com.example.treasurehuntapp.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.local.CurrentHuntStorage
import com.example.treasurehuntapp.data.repository.LocationsRepository
import com.example.treasurehuntapp.data.repository.TreasureHuntRepository
import com.example.treasurehuntapp.data.repository.UploadsRepository
import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: TreasureHuntRepository,
    private val storage: CurrentHuntStorage,
    private val locationsRepository: LocationsRepository,
    private val uploadsRepository: UploadsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val memberships = repo.getMemberships()
                val all = memberships.map { it.treasureHunt }
                val activeByDate = all.filter { isActive(it) }
                val storedHuntId = storage.huntIdFlow.first()
                val storedHunt = storedHuntId?.let { id ->
                    runCatching { repo.getHuntById(id) }.getOrNull()
                }
                val isOwner = memberships.any { it.role?.equals("owner", ignoreCase = true) == true }
                val active = buildList {
                    if (storedHunt != null) add(storedHunt)
                    addAll(activeByDate.filterNot { it.id == storedHunt?.id })
                }

                val activeCards = active.map { hunt ->
                    val participantsCount = runCatching {
                        repo.getParticipants(hunt.id).size
                    }.getOrDefault(0)

                    val totalLocations = runCatching {
                        locationsRepository.getLocationsByHunt(hunt.id).size
                    }.getOrDefault(0)

                    val currentLocationIndex = if (totalLocations > 0) 1 else 0
                    val progress = if (currentLocationIndex > 0 && totalLocations > 0) {
                        (currentLocationIndex.toFloat() / totalLocations.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    ActiveHuntCardState(
                        hunt = hunt,
                        imageUrl = runCatching { uploadsRepository.resolveDisplayImageUrl(hunt.image) }.getOrNull(),
                        participantsCount = participantsCount,
                        currentLocationIndex = currentLocationIndex,
                        totalLocations = totalLocations,
                        progress = progress
                    )
                }

                _state.value = _state.value.copy(
                    isLoading = false,
                    allHunts = all,
                    activeHunts = active,
                    activeHuntCards = activeCards,
                    isOwner = isOwner
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load hunts"
                )
            }
        }
    }

    fun selectHunt(hunt: TreasureHuntDto, onDone: () -> Unit) {
        viewModelScope.launch {
            storage.saveHunt(hunt.id, hunt.code)
            onDone()
        }
    }

    fun joinByCode(code: String, onSuccess: () -> Unit) {
        val trimmed = code.trim()
        if (trimmed.length != 6) {
            _state.value = _state.value.copy(joinError = "Code must be exactly 6 characters")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(joiningByCode = true, joinError = null)
                val hunt = repo.joinHunt(trimmed)
                storage.saveHunt(hunt.id, hunt.code)
                _state.value = _state.value.copy(joiningByCode = false, joinError = null)
                load()
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    joiningByCode = false,
                    joinError = e.message ?: "Join failed"
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

}
