package com.example.treasurehuntapp.features.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    // later
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _state.value = _state.value.copy(
            isPasswordVisible = !_state.value.isPasswordVisible
        )
    }

    fun login(onSuccess: () -> Unit) {
        val s = _state.value
        
        if (s.email.isBlank()) {
            _state.value = s.copy(errorMessage = "Email is required")
            return
        }
        if (!s.email.contains("@")) {
            _state.value = s.copy(errorMessage = "Email is not valid")
            return
        }
        if (s.password.length < 6) {
            _state.value = s.copy(errorMessage = "Password must be at least 6 characters")
            return
        }

        // repository call
        onSuccess()
    }
}

