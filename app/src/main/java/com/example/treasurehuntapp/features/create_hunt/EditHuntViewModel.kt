package com.example.treasurehuntapp.features.create_hunt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.local.CurrentHuntStorage
import com.example.treasurehuntapp.data.repository.TreasureHuntRepository
import com.example.treasurehuntapp.data.repository.UploadsRepository
import com.example.treasurehuntapp.data.source.remote.dto.hunt.UpdateTreasureHuntDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class EditHuntViewModel @Inject constructor(
    private val repo: TreasureHuntRepository,
    private val storage: CurrentHuntStorage,
    private val uploadsRepository: UploadsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditHuntState())
    val state = _state.asStateFlow()

    private var currentHuntId: String? = null

    fun onNameChange(v: String) = _state.value.let { _state.value = it.copy(name = v) }
    fun onDescriptionChange(v: String) = _state.value.let { _state.value = it.copy(description = v) }
    fun onImageChange(v: String) = _state.value.let { _state.value = it.copy(image = v) }
    fun onStartChange(v: String) = _state.value.let { _state.value = it.copy(startIso = v) }
    fun onEndChange(v: String) = _state.value.let { _state.value = it.copy(endIso = v) }
    fun clearFeedback() = _state.value.let { _state.value = it.copy(error = null, successMessage = null) }

    fun load() {
        if (!_state.value.isLoading && currentHuntId != null) return
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val huntId = storage.huntIdFlow.first()?.takeIf { it.isNotBlank() }
                    ?: error("No selected hunt")
                currentHuntId = huntId
                val hunt = repo.getHuntById(huntId)
                _state.value = EditHuntState(
                    name = hunt.name,
                    description = hunt.description.orEmpty(),
                    image = hunt.image.orEmpty(),
                    startIso = hunt.start,
                    endIso = hunt.end,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load hunt"
                )
            }
        }
    }

    fun update(onSuccess: () -> Unit) {
        val s = _state.value
        val huntId = currentHuntId
        if (huntId.isNullOrBlank()) {
            _state.value = s.copy(error = "No selected hunt")
            return
        }
        if (s.name.isBlank() || s.startIso.isBlank() || s.endIso.isBlank()) {
            _state.value = s.copy(error = "Name, start and end are required")
            return
        }
        if (!isEndAfterStart(s.startIso, s.endIso)) {
            _state.value = s.copy(error = "End time must be after start time")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isSaving = true, error = null)
                val imageValue = run {
                    val raw = _state.value.image.trim().ifBlank { null } ?: return@run null
                    val isLocal = raw.startsWith("content://") || raw.startsWith("file://")
                    if (!isLocal) raw else uploadsRepository.uploadHuntImage(raw)
                }

                repo.updateHunt(
                    huntId = huntId,
                    dto = UpdateTreasureHuntDto(
                        name = _state.value.name.trim(),
                        description = _state.value.description.trim().ifBlank { null },
                        image = imageValue,
                        start = _state.value.startIso.trim(),
                        end = _state.value.endIso.trim()
                    )
                )

                _state.value = _state.value.copy(isSaving = false, successMessage = "Hunt updated")
                onSuccess()
            } catch (e: HttpException) {
                val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = parseApiErrorMessage(raw) ?: "Update hunt failed (${e.code()})"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = e.message ?: "Update hunt failed"
                )
            }
        }
    }

    fun addOwner(userId: String) {
        val huntId = currentHuntId
        if (huntId.isNullOrBlank()) {
            _state.value = _state.value.copy(error = "No selected hunt")
            return
        }
        val targetUserId = userId.trim()
        if (targetUserId.isBlank()) {
            _state.value = _state.value.copy(error = "User ID is required")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null, successMessage = null)
                repo.addOwner(huntId, targetUserId)
                _state.value = _state.value.copy(successMessage = "Owner added")
            } catch (e: HttpException) {
                val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                _state.value = _state.value.copy(
                    error = parseApiErrorMessage(raw) ?: "Add owner failed (${e.code()})"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Add owner failed")
            }
        }
    }

    fun deleteHunt(onDeleted: () -> Unit) {
        val huntId = currentHuntId
        if (huntId.isNullOrBlank()) {
            _state.value = _state.value.copy(error = "No selected hunt")
            return
        }
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(error = null, successMessage = null)
                repo.deleteHunt(huntId)
                storage.clear()
                onDeleted()
            } catch (e: HttpException) {
                val raw = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                _state.value = _state.value.copy(
                    error = parseApiErrorMessage(raw) ?: "Delete hunt failed (${e.code()})"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message ?: "Delete hunt failed")
            }
        }
    }

    private fun isEndAfterStart(startIso: String, endIso: String): Boolean {
        val start = parseIso(startIso) ?: return true
        val end = parseIso(endIso) ?: return true
        return end > start
    }

    private fun parseIso(value: String): Long? {
        val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
        for (pattern in patterns) {
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(value)?.time
            }.getOrNull()?.let { return it }
        }
        return null
    }

    private fun parseApiErrorMessage(raw: String?): String? {
        val body = raw?.trim().orEmpty()
        if (body.isBlank()) return null
        val match = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
        return match?.groupValues?.getOrNull(1) ?: body.take(180)
    }
}
