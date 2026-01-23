package com.example.treasurehuntapp.data.source.remote.dto.auth

data class AuthResponseDto(
    val id: String,
    val username: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)
