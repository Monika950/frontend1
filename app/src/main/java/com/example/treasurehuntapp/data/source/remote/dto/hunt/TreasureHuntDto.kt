package com.example.treasurehuntapp.data.source.remote.dto.hunt

data class TreasureHuntDto(
    val id: String,
    val name: String,
    val description: String?,
    val image: String?,
    val start: String,
    val end: String,
    val code: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)