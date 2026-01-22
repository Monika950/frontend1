package com.example.treasurehuntapp.features.create_location

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CreateLocationViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CreateLocationState())
    val state = _state.asStateFlow()

    fun onLocationNameChange(value: String) {
        _state.value = _state.value.copy(locationName = value)
    }

    fun onClueChange(value: String) {
        _state.value = _state.value.copy(clue = value)
    }

    fun save(onSaved: () -> Unit) {
        // TODO: save location
        onSaved()
    }
}
