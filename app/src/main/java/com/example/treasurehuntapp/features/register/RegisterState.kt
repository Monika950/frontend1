package com.example.treasurehuntapp.features.register

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",

    val isLoading: Boolean = false,
    val error: String? = null
)
