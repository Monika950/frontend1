package com.example.treasurehuntapp.features.join_hunt

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class JoinHuntViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(JoinHuntState())
    val state = _state.asStateFlow()

    fun onCodeChange(value: String) {
        _state.value = _state.value.copy(code = value)
    }

    fun join(onSuccess: () -> Unit) {
        // TODO: validate code
        if (_state.value.code.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Code is required")
            return
        }
        onSuccess()
    }
}
