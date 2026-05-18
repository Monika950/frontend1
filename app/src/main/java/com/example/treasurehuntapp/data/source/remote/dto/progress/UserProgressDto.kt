package com.example.treasurehuntapp.data.source.remote.dto.progress

data class UserProgressDto(
    val id: String? = null,
    val status: String? = null,
    val currentCoordinates: Map<String, Any?>? = null,
    val currentLocationId: String? = null,
    val completedLocations: List<String> = emptyList(),
    val startedAt: String? = null,
    val completedAt: String? = null,
    val updatedAt: String? = null
)
