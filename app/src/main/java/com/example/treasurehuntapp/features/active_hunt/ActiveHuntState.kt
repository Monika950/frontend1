package com.example.treasurehuntapp.features.active_hunt

import com.example.treasurehuntapp.data.source.remote.dto.hunt.TreasureHuntDto
import com.example.treasurehuntapp.data.source.remote.dto.hunt.ParticipantDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.LocationDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.UserProgressDto

data class ActiveHuntState(
    val hunt: TreasureHuntDto? = null,
    val locations: List<LocationDto> = emptyList(),
    val participants: List<ParticipantDto> = emptyList(),
    val participantsCount: Int = 0,
    val isOwner: Boolean = false,
    val myLat: Double? = null,
    val myLng: Double? = null,
    val isSubmittingAnswer: Boolean = false,
    val answerError: String? = null,
    val currentLocationId: String? = null,
    val completedLocationIds: List<String> = emptyList(),
    val participantLivePositions: Map<String, ParticipantLivePosition> = emptyMap(),
    val participantProgressByUserId: Map<String, UserProgressDto> = emptyMap(),
    val participantProgressLoadingUserId: String? = null,
    val participantProgressError: String? = null,
    val showMissionAccomplished: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

data class ParticipantLivePosition(
    val userId: String,
    val lat: Double,
    val lng: Double,
    val ts: String,
    val currentLocationId: String? = null
)
