package com.example.treasurehuntapp.features.create_location

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.LocationsRepository
import com.example.treasurehuntapp.data.repository.UploadsRepository
import com.example.treasurehuntapp.data.source.remote.dto.locations.CoordinatesDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.UpdateLocationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class EditLocationViewModel @Inject constructor(
    private val locationsRepository: LocationsRepository,
    private val uploadsRepository: UploadsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val locationId: String = savedStateHandle.get<String>("locationId").orEmpty()

    private val _state = MutableStateFlow(EditLocationState(locationId = locationId))
    val state = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (locationId.isBlank()) {
            _state.update { it.copy(error = "Missing location id") }
            return
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                val location = locationsRepository.getLocationById(locationId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        name = location.name,
                        question = location.question,
                        correctAnswer = location.correctAnswer,
                        hint = location.hint.orEmpty(),
                        imageUri = location.image.orEmpty(),
                        originalImageUrl = location.image,
                        imageLabel = if (location.image.isNullOrBlank()) {
                            "Add location image (tap to select)"
                        } else {
                            "Image selected"
                        },
                        lat = String.format(Locale.US, "%.6f", location.coordinates.lat),
                        lng = String.format(Locale.US, "%.6f", location.coordinates.lng)
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load location"
                    )
                }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, error = null) }
    fun onQuestionChange(value: String) = _state.update { it.copy(question = value, error = null) }
    fun onCorrectAnswerChange(value: String) = _state.update { it.copy(correctAnswer = value, error = null) }
    fun onHintChange(value: String) = _state.update { it.copy(hint = value, error = null) }
    fun onLatChange(value: String) = _state.update { it.copy(lat = value, error = null) }
    fun onLngChange(value: String) = _state.update { it.copy(lng = value, error = null) }

    fun onImagePicked(uri: String) {
        _state.update {
            it.copy(
                imageUri = uri,
                imageLabel = "Image selected",
                allowSaveWithoutImage = false,
                error = null
            )
        }
    }

    fun onMapPicked(lat: Double, lng: Double) {
        _state.update {
            it.copy(
                lat = String.format(Locale.US, "%.6f", lat),
                lng = String.format(Locale.US, "%.6f", lng),
                error = null
            )
        }
    }

    fun save(onSaved: () -> Unit) = saveInternal(onSaved, skipImageUpload = false)

    fun saveWithoutImage(onSaved: () -> Unit) = saveInternal(onSaved, skipImageUpload = true)

    private fun saveInternal(onSaved: () -> Unit, skipImageUpload: Boolean) {
        val s = _state.value
        if (s.locationId.isBlank()) {
            _state.update { it.copy(error = "Missing location id") }
            return
        }
        if (s.name.isBlank() || s.question.isBlank() || s.correctAnswer.isBlank()) {
            _state.update { it.copy(error = "Name, question, and correct answer are required.") }
            return
        }

        val lat = s.lat.toDoubleOrNull()
        val lng = s.lng.toDoubleOrNull()
        if (lat == null || lng == null) {
            _state.update { it.copy(error = "Latitude and longitude must be valid numbers.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            try {
                val imageValue = resolveImageForSave(skipImageUpload)
                val dto = UpdateLocationDto(
                    coordinates = CoordinatesDto(lat = lat, lng = lng),
                    name = _state.value.name.trim(),
                    question = _state.value.question.trim(),
                    correctAnswer = _state.value.correctAnswer.trim(),
                    hint = _state.value.hint.trim(),
                    image = imageValue
                )

                locationsRepository.updateLocation(_state.value.locationId, dto)
                _state.update { it.copy(saving = false, error = null) }
                onSaved()
            } catch (e: Exception) {
                val current = _state.value
                if (current.allowSaveWithoutImage && current.error?.startsWith("Image upload failed:") == true) {
                    return@launch
                }
                _state.update {
                    it.copy(saving = false, error = e.message ?: "Failed to update location")
                }
            }
        }
    }

    private suspend fun resolveImageForSave(skipImageUpload: Boolean): String? {
        val current = _state.value
        val imageUri = current.imageUri.trim()
        if (imageUri.isBlank()) return null

        val isLocalUri = imageUri.startsWith("content://") || imageUri.startsWith("file://")
        if (!isLocalUri) return imageUri

        if (skipImageUpload) {
            return current.originalImageUrl
        }

        return try {
            uploadsRepository.uploadLocationImage(imageUri)
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    saving = false,
                    allowSaveWithoutImage = true,
                    error = "Image upload failed: ${e.message ?: "unknown error"}. You can save without image."
                )
            }
            throw e
        }
    }
}
