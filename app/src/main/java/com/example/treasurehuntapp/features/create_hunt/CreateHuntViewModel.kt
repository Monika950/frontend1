package com.example.treasurehuntapp.features.create_hunt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CreateHuntViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(CreateHuntState())
    val state = _state.asStateFlow()

    fun onTitleChange(value: String) {
        _state.value = _state.value.copy(title = value)
    }

    fun onDescriptionChange(value: String) {
        _state.value = _state.value.copy(description = value)
    }

    fun createHunt(onCreated: () -> Unit) {
        // TODO: repository create hunt
        onCreated()
    }
}
