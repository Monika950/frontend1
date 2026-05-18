package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import com.example.treasurehuntapp.data.source.remote.dto.progress.AbandonHuntDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.CompleteLocationDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.StartHuntDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.UpdatePositionDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.UserProgressDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface UserProgressApi {
    @POST("user-progress/start")
    suspend fun start(@Body body: StartHuntDto): ApiResponse<Unit>

    @PATCH("user-progress/update-position")
    suspend fun updatePosition(@Body body: UpdatePositionDto): ApiResponse<Unit>

    @PATCH("user-progress/complete-location")
    suspend fun completeLocation(@Body body: CompleteLocationDto): ApiResponse<Unit>

    @PATCH("user-progress/abandon")
    suspend fun abandon(@Body body: AbandonHuntDto): ApiResponse<Unit>

    @GET("user-progress/{huntId}")
    suspend fun getByHunt(@Path("huntId") huntId: String): ApiResponse<UserProgressDto>

    @GET("user-progress/{huntId}/user/{userId}")
    suspend fun getByHuntForUser(
        @Path("huntId") huntId: String,
        @Path("userId") userId: String
    ): ApiResponse<UserProgressDto>
}
