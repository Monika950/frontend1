package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import com.example.treasurehuntapp.data.source.remote.dto.uploads.PresignDownloadDto
import com.example.treasurehuntapp.data.source.remote.dto.uploads.PresignUploadDto
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.POST

interface UploadsApi {

    @POST("uploads/presign")
    suspend fun presign(
        @Body body: PresignUploadDto
    ): ApiResponse<JsonObject>

    @POST("uploads/presign-download")
    suspend fun presignDownload(
        @Body body: PresignDownloadDto
    ): ApiResponse<JsonObject>
}
