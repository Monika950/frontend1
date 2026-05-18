package com.example.treasurehuntapp.data.source.remote.dto.answer

data class UserAnswerResultDto(
    val isCorrect: Boolean,
    val attemptNumber: Int? = null,
    val progress: UserAnswerProgressDto? = null
)

data class UserAnswerProgressDto(
    val status: String? = null,
    val currentLocationId: String? = null,
    val completedLocations: List<String> = emptyList(),
    val completedAt: String? = null
)

