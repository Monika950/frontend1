package com.example.treasurehuntapp.features.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state

    fun onEmailChange(value: String) = _state.update { it.copy(email = value, error = null) }
    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }
    fun onUsernameChange(value: String) = _state.update { it.copy(username = value, error = null) }
    fun onFirstNameChange(value: String) = _state.update { it.copy(firstName = value, error = null) }
    fun onLastNameChange(value: String) = _state.update { it.copy(lastName = value, error = null) }

    fun register(onSuccess: () -> Unit) {
        val s = _state.value

        if (s.email.isBlank() || s.password.isBlank() || s.username.isBlank() || s.firstName.isBlank() || s.lastName.isBlank()) {
            _state.update { it.copy(error = "Please fill all fields.") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                authRepository.register(
                    email = s.email,
                    password = s.password,
                    username = s.username,
                    firstName = s.firstName,
                    lastName = s.lastName
                )
                _state.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = t.message ?: "Registration failed"
                    )
                }
            }
        }
    }
}
