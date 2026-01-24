package com.example.treasurehuntapp.data.source.remote.dto.hunt

data class UpdateTreasureHuntDto(
    val name: String? = null,
    val description: String? = null,
    val image: String? = null,
    val start: String? = null,
    val end: String? = null
)
