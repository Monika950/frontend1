package com.example.treasurehuntapp.data.source.remote.dto.hunt

data class ParticipantDto(
    val id: String,
    val email: String?,
    val role: String?,
    val username: String? = null,
    val joinedAt: String? = null,
    val user: ParticipantUserDto? = null,
)

data class ParticipantUserDto(
    val id: String,
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
