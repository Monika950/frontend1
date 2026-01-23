package com.example.treasurehuntapp.data.source.remote.dto.auth

data class RegisterDto(
    val email: String,
    val password: String,
    val username: String,
    val firstName: String,
    val lastName: String
)
