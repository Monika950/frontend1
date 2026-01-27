package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import com.example.treasurehuntapp.data.source.remote.dto.locations.CreateLocationDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.LocationDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.UpdateLocationDto
import retrofit2.http.*

interface LocationsApi {

    @POST("location")
    suspend fun create(@Body body: CreateLocationDto): ApiResponse<LocationDto>

    @GET("location/hunt/{treasureHuntId}")
    suspend fun getByHunt(@Path("treasureHuntId") treasureHuntId: String): ApiResponse<List<LocationDto>>

    @GET("location/{id}")
    suspend fun getById(@Path("id") id: String): ApiResponse<LocationDto>

    @PATCH("location/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body body: UpdateLocationDto
    ): ApiResponse<LocationDto>

    @DELETE("location/{id}")
    suspend fun delete(@Path("id") id: String): ApiResponse<Unit>
}
