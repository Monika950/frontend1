package com.example.treasurehuntapp.features.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state = _state.asStateFlow()

    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null, successMessage = null) }
    }

    fun submit() {
        val email = _state.value.email.trim()
        if (email.isBlank()) {
            _state.update { it.copy(error = "Email is required.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                authRepository.forgotPassword(email)
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "If an account exists for this email, reset instructions were sent."
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to request password reset."
                    )
                }
            }
        }
    }
}
