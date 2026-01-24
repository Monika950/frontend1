package com.example.treasurehuntapp.data.source.remote.dto.progress

data class UpdatePositionDto(
    val huntId: String,
    val lat: Double,
    val lng: Double
)