package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import com.example.treasurehuntapp.data.source.remote.dto.hunt.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface TreasureHuntApi {

    @POST("treasure-hunt")
    suspend fun create(@Body body: CreateTreasureHuntDto): ApiResponse<TreasureHuntDto>

    @GET("treasure-hunt")
    suspend fun getAll(): ApiResponse<List<HuntMembershipDto>>

    @GET("treasure-hunt/{id}")
    suspend fun getById(@Path("id") id: String): ApiResponse<TreasureHuntDto>

    @PATCH("treasure-hunt/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UpdateTreasureHuntDto
    ): ApiResponse<TreasureHuntDto>

    @DELETE("treasure-hunt/{id}")
    suspend fun delete(@Path("id") id: String) : ApiResponse<Unit>

    @POST("treasure-hunt/join")
    suspend fun join(@Body body: JoinTreasureHuntDto): ApiResponse<TreasureHuntDto>

    @POST("treasure-hunt/{id}/owners/{userId}")
    suspend fun addOwner(
        @Path("id") huntId: String,
        @Path("userId") userId: String
    ): ApiResponse<TreasureHuntDto>

    @GET("treasure-hunt/{id}/participants")
    suspend fun getParticipants(
        @Path("id") huntId: String
    ): ApiResponse<List<ParticipantDto>>

    @PATCH("treasure-hunt/{id}/participants/{userId}")
    suspend fun updateParticipantRole(
        @Path("id") huntId: String,
        @Path("userId") userId: String,
        @Body body: UpdateParticipantRoleDto
    ): ApiResponse<ParticipantDto>

    @DELETE("treasure-hunt/{id}/participants/{userId}")
    suspend fun removeParticipant(
        @Path("id") huntId: String,
        @Path("userId") userId: String
    ): ApiResponse<Unit>
}
