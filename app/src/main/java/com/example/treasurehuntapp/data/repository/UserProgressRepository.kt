package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.remote.api.UserProgressApi
import com.example.treasurehuntapp.data.source.remote.dto.progress.AbandonHuntDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.CompleteLocationDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.StartHuntDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.UpdatePositionDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.UserProgressDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProgressRepository @Inject constructor(
    private val api: UserProgressApi
) {
    suspend fun start(huntId: String) {
        api.start(StartHuntDto(huntId))
    }

    suspend fun updatePosition(huntId: String, lat: Double, lng: Double) {
        api.updatePosition(
            UpdatePositionDto(
                huntId = huntId,
                lat = lat,
                lng = lng
            )
        )
    }

    suspend fun completeLocation(huntId: String, locationId: String) {
        api.completeLocation(
            CompleteLocationDto(
                huntId = huntId,
                locationId = locationId
            )
        )
    }

    suspend fun abandon(huntId: String) {
        api.abandon(AbandonHuntDto(huntId))
    }

    suspend fun getByHunt(huntId: String): UserProgressDto {
        return api.getByHunt(huntId).data
    }

    suspend fun getByHuntForUser(huntId: String, userId: String): UserProgressDto {
        return api.getByHuntForUser(huntId = huntId, userId = userId).data
    }
}
