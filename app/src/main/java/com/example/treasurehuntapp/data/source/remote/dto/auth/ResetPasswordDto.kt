package com.example.treasurehuntapp.data.source.remote.dto.auth

data class ResetPasswordDto(
    val token: String,
    val newPassword: String
)