package com.example.treasurehuntapp.data.source.remote.dto.hunt

data class HuntMembershipDto(
    val id: String,
    val role: String? = null,
    val joinedAt: String? = null,
    val treasureHunt: TreasureHuntDto,
)

