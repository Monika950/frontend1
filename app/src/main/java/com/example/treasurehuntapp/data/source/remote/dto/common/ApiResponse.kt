package com.example.treasurehuntapp.data.source.remote.dto.common

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T,
    val timestamp: String,
    val path: String
)