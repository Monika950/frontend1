package com.example.treasurehuntapp.data.source.remote.dto.hunt

data class CreateTreasureHuntDto(
    val name: String,
    val description: String? = null,
    val image: String? = null,
    val start: String, // ISO date
    val end: String    // ISO date
)