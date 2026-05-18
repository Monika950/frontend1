package com.example.treasurehuntapp.features.create_hunt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.local.CurrentHuntStorage
import com.example.treasurehuntapp.data.repository.TreasureHuntRepository
import com.example.treasurehuntapp.data.repository.UploadsRepository
import com.example.treasurehuntapp.data.source.remote.dto.hunt.CreateTreasureHuntDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class CreateHuntViewModel @Inject constructor(
    private val repo: TreasureHuntRepository,
    private val storage: CurrentHuntStorage,
    private val uploadsRepository: UploadsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateHuntState())
    val state = _state.asStateFlow()

    fun onNameChange(v: String) = _state.value.let { _state.value = it.copy(name = v) }
    fun onDescriptionChange(v: String) = _state.value.let { _state.value = it.copy(description = v) }
    fun onImageChange(v: String) = _state.value.let { _state.value = it.copy(image = v) }
    fun onStartChange(v: String) = _state.value.let { _state.value = it.copy(startIso = v) }
    fun onEndChange(v: String) = _state.value.let { _state.value = it.copy(endIso = v) }

    fun create(onSuccess: () -> Unit) {
        val s = _state.value

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
                _state.value = s.copy(isLoading = true, error = null)
                val imageValue = run {
                    val raw = s.image.trim().ifBlank { null } ?: return@run null
                    val isLocal = raw.startsWith("content://") || raw.startsWith("file://")
                    if (!isLocal) raw else {
                        try {
                            uploadsRepository.uploadHuntImage(raw)
                        } catch (e: Exception) {
                            throw IllegalStateException(
                                "Image upload failed: ${e.message ?: "unknown error"}",
                                e
                            )
                        }
                    }
                }

                val created = repo.createHunt(
                    CreateTreasureHuntDto(
                        name = s.name.trim(),
                        description = s.description.trim().ifBlank { null },
                        image = imageValue,
                        start = s.startIso.trim(),
                        end = s.endIso.trim()
                    )
                )

                storage.saveHunt(created.id, created.code)
                _state.value = _state.value.copy(isLoading = false)
                onSuccess()
            } catch (e: HttpException) {
                val serverMessage = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = parseApiErrorMessage(serverMessage)
                        ?: "Create hunt failed (${e.code()})"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Create hunt failed"
                )
            }
        }
    }

    private fun isEndAfterStart(startIso: String, endIso: String): Boolean {
        val start = parseIso(startIso) ?: return true
        val end = parseIso(endIso) ?: return true
        return end > start
    }

    private fun parseIso(value: String): Long? {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (pattern in patterns) {
            runCatching {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(value)?.time
            }.getOrNull()?.let { return it }
        }
        return null
    }

    private fun parseApiErrorMessage(raw: String?): String? {
        val body = raw?.trim().orEmpty()
        if (body.isBlank()) return null
        val messageMatch = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"").find(body)
        if (messageMatch != null) return messageMatch.groupValues[1]
        return body.take(180)
    }
}
