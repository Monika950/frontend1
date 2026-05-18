package com.example.treasurehuntapp.features.reset_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state = _state.asStateFlow()

    fun setToken(token: String?) {
        _state.update { it.copy(token = token.orEmpty(), error = null) }
    }

    fun onNewPasswordChange(value: String) {
        _state.update { it.copy(newPassword = value, error = null, successMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _state.update { it.copy(confirmPassword = value, error = null, successMessage = null) }
    }

    fun submit(onSuccess: () -> Unit) {
        val s = _state.value
        val token = s.token.trim()
        if (token.isBlank()) {
            _state.update { it.copy(error = "Invalid reset link.") }
            return
        }
        if (s.newPassword.isBlank() || s.confirmPassword.isBlank()) {
            _state.update { it.copy(error = "Please fill both password fields.") }
            return
        }
        if (s.newPassword != s.confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                authRepository.resetPassword(token = token, newPassword = s.newPassword)
                _state.update {
                    it.copy(
                        isLoading = false,
                        newPassword = "",
                        confirmPassword = "",
                        successMessage = "Password reset successful."
                    )
                }
                onSuccess()
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    400 -> "Invalid password. Please check the requirements."
                    403 -> "This reset link is invalid or expired."
                    else -> e.message()
                }
                _state.update { it.copy(isLoading = false, error = message.ifBlank { "Reset failed." }) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message ?: "Failed to reset password.")
                }
            }
        }
    }
}
