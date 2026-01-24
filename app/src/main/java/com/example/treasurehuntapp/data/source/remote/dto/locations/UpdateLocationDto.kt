package com.example.treasurehuntapp.data.source.remote.dto.locations

data class UpdateLocationDto(
    val coordinates: CoordinatesDto? = null,
    val name: String? = null,
    val question: String? = null,
    val correctAnswer: String? = null,
    val hint: String? = null,
    val image: String? = null,
    val orderIndex: Int? = null,
    val treasureHuntId: String? = null
)
