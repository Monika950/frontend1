package com.example.treasurehuntapp.features.home

import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allHunts: List<TreasureHuntDto> = emptyList(),
    val activeHunts: List<TreasureHuntDto> = emptyList(),
    val activeHuntCards: List<ActiveHuntCardState> = emptyList(),
    val isOwner: Boolean = false,
    val joiningByCode: Boolean = false,
    val joinError: String? = null,
)

data class ActiveHuntCardState(
    val hunt: TreasureHuntDto,
    val imageUrl: String?,
    val participantsCount: Int,
    val currentLocationIndex: Int,
    val totalLocations: Int,
    val progress: Float
)
