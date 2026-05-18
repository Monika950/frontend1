package com.example.treasurehuntapp.features.reset_password

data class ResetPasswordState(
    val token: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
