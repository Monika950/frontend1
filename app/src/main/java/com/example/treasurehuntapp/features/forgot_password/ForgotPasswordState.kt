package com.example.treasurehuntapp.features.forgot_password

data class ForgotPasswordState(
    val email: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)
