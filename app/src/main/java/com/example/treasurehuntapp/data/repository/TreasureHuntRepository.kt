package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.remote.api.TreasureHuntApi
import com.example.treasurehuntapp.data.source.remote.dto.hunt.*
import javax.inject.Inject

class TreasureHuntRepository @Inject constructor(
    private val api: TreasureHuntApi
) {

    suspend fun createHunt(dto: CreateTreasureHuntDto): TreasureHuntDto {
        return api.create(dto).data
    }

    suspend fun getAllHunts(): List<TreasureHuntDto> {
        val memberships = api.getAll().data
        return memberships.map { it.treasureHunt }
    }

    suspend fun getMemberships(): List<HuntMembershipDto> {
        return api.getAll().data
    }

    suspend fun getHuntById(huntId: String): TreasureHuntDto {
        return api.getById(huntId).data
    }

    suspend fun updateHunt(huntId: String, dto: UpdateTreasureHuntDto): TreasureHuntDto {
        return api.update(huntId, dto).data
    }

    suspend fun deleteHunt(huntId: String) {
        api.delete(huntId)
    }

    suspend fun joinHunt(code: String): TreasureHuntDto {
        return api.join(JoinTreasureHuntDto(code)).data
    }

    suspend fun addOwner(huntId: String, userId: String): TreasureHuntDto {
        return api.addOwner(huntId, userId).data
    }

    suspend fun getParticipants(huntId: String): List<ParticipantDto> {
        return api.getParticipants(huntId).data
    }

    suspend fun updateParticipantRole(
        huntId: String,
        userId: String,
        role: String
    ): ParticipantDto {
        return api.updateParticipantRole(
            huntId = huntId,
            userId = userId,
            body = UpdateParticipantRoleDto(role = role)
        ).data
    }

    suspend fun removeParticipant(huntId: String, userId: String) {
        api.removeParticipant(huntId = huntId, userId = userId)
    }
}
