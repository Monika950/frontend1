package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import com.example.treasurehuntapp.data.source.remote.dto.notifications.NotificationDto
import com.example.treasurehuntapp.data.source.remote.dto.notifications.ReadBatchDto
import com.example.treasurehuntapp.data.source.remote.dto.notifications.ReadBatchResultDto
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    @GET("notifications")
    suspend fun list(
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
        @Query("read") read: Boolean? = null
    ): ApiResponse<JsonElement>

    @PATCH("notifications/{id}/read")
    suspend fun markRead(
        @Path("id") id: String
    ): ApiResponse<NotificationDto>

    @PATCH("notifications/read-batch")
    suspend fun markReadBatch(
        @Body body: ReadBatchDto
    ): ApiResponse<ReadBatchResultDto>
}
