package com.example.treasurehuntapp.features.my_hunts

import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto

sealed class HuntStatus(val label: String) {
    data object Active : HuntStatus("ACTIVE")
    data object Scheduled : HuntStatus("SCHEDULED")
    data object Finished : HuntStatus("FINISHED")
}

data class MyHuntsData(
    val createdHunts: List<MyHuntCard> = emptyList(),
    val joinedHunts: List<MyHuntCard> = emptyList()
)

data class MyHuntCard(
    val hunt: TreasureHuntDto,
    val imageUrl: String?,
    val dateText: String,
    val status: HuntStatus,
    val participants: Int?,
    val rating: String?,
    val ctaText: String?
)
