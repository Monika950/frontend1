package com.example.treasurehuntapp.features.create_location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.local.CurrentHuntStorage
import com.example.treasurehuntapp.data.repository.LocationsRepository
import com.example.treasurehuntapp.data.repository.UploadsRepository
import com.example.treasurehuntapp.data.source.remote.dto.locations.CoordinatesDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.CreateLocationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateLocationViewModel @Inject constructor(
    private val locationsRepository: LocationsRepository,
    private val uploadsRepository: UploadsRepository,
    private val currentHuntStorage: CurrentHuntStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateLocationState())
    val state = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value, error = null)
    }

    fun onQuestionChange(value: String) {
        _state.value = _state.value.copy(question = value, error = null)
    }

    fun onCorrectAnswerChange(value: String) {
        _state.value = _state.value.copy(correctAnswer = value, error = null)
    }

    fun onHintChange(value: String) {
        _state.value = _state.value.copy(hint = value, error = null)
    }

    fun onLatChange(value: String) {
        _state.value = _state.value.copy(lat = value, error = null)
    }

    fun onLngChange(value: String) {
        _state.value = _state.value.copy(lng = value, error = null)
    }

    fun onImagePicked(uri: String) {
        _state.value = _state.value.copy(
            imageUri = uri,
            imageLabel = "Image selected",
            allowSaveWithoutImage = false,
            error = null
        )
    }

    fun onMapPicked(lat: Double, lng: Double) {
        _state.value = _state.value.copy(
            lat = String.format(Locale.US, "%.6f", lat),
            lng = String.format(Locale.US, "%.6f", lng),
            error = null
        )
    }

    fun save(onSaved: () -> Unit) {
        saveInternal(onSaved, skipImageUpload = false)
    }

    fun saveWithoutImage(onSaved: () -> Unit) {
        saveInternal(onSaved, skipImageUpload = true)
    }

    private fun saveInternal(onSaved: () -> Unit, skipImageUpload: Boolean) {
        val s = _state.value
        if (s.name.isBlank() || s.question.isBlank() || s.correctAnswer.isBlank()) {
            _state.value = s.copy(error = "Name, question, and correct answer are required.")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true, error = null)

            val huntId = currentHuntStorage.huntIdFlow.first()
            if (huntId.isNullOrBlank()) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = "No active hunt. Join or create a hunt first."
                )
                return@launch
            }

            val lat = _state.value.lat.toDoubleOrNull()
            val lng = _state.value.lng.toDoubleOrNull()
            if (lat == null || lng == null) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = "Latitude and longitude must be valid numbers."
                )
                return@launch
            }

            try {
                val existing = locationsRepository.getLocationsByHunt(huntId)
                val nextOrderIndex = existing.size + 1
                val uploadedImageUrl = if (skipImageUpload) {
                    null
                } else {
                    try {
                        _state.value.imageUri
                            .takeIf { it.isNotBlank() }
                            ?.let { uploadsRepository.uploadLocationImage(it) }
                    } catch (e: Exception) {
                        _state.value = _state.value.copy(
                            saving = false,
                            allowSaveWithoutImage = _state.value.imageUri.isNotBlank(),
                            error = "Image upload failed: ${e.message ?: "unknown error"}. You can save without image."
                        )
                        return@launch
                    }
                }

                val dto = CreateLocationDto(
                    coordinates = CoordinatesDto(lat = lat, lng = lng),
                    name = _state.value.name.trim(),
                    question = _state.value.question.trim(),
                    correctAnswer = _state.value.correctAnswer.trim(),
                    hint = _state.value.hint.trim(),
                    image = uploadedImageUrl,
                    orderIndex = nextOrderIndex,
                    treasureHuntId = huntId
                )

                try {
                    locationsRepository.createLocation(dto)
                } catch (e: Exception) {
                    throw IllegalStateException("Create location request failed: ${e.message ?: "unknown error"}", e)
                }

                delay(150)

                _state.value = _state.value.copy(saving = false, error = null)
                onSaved()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    saving = false,
                    error = e.message ?: "Failed to create location."
                )
            }
        }
    }
}
