package com.example.treasurehuntapp.features.home

import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val allHunts: List<TreasureHuntDto> = emptyList(),
    val activeHunts: List<TreasureHuntDto> = emptyList()
)