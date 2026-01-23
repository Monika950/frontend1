package com.example.treasurehuntapp.features.login
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() && password.length >= 6 && !isLoading
}
