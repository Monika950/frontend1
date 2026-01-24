package com.example.treasurehuntapp.data.source.remote.dto.locations

data class LocationDto(
    val id: String,
    val coordinates: CoordinatesDto,
    val name: String,
    val question: String,
    val hint: String?,
    val correctAnswer: String,
    val image: String?,
    val createdAt: String,
    val updatedAt: String
)
