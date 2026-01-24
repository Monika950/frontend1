package com.example.treasurehuntapp.data.source.remote.dto.locations

data class CreateLocationDto(
    val coordinates: CoordinatesDto,
    val name: String,
    val question: String,
    val correctAnswer: String,
    val hint: String? = null,
    val image: String? = null,
    val orderIndex: Int,
    val treasureHuntId: String
)
