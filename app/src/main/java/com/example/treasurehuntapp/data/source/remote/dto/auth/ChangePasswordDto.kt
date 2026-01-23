package com.example.treasurehuntapp.data.source.remote.dto.auth

data class ChangePasswordDto(
    val oldPassword: String,
    val newPassword: String
)