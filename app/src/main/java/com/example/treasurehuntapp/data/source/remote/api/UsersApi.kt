package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import com.example.treasurehuntapp.data.source.remote.dto.user.UserDto
import com.example.treasurehuntapp.data.source.remote.dto.user.UpdateUserDto
import retrofit2.http.*

interface UsersApi {

    @GET("/users")
    suspend fun getAll(): ApiResponse<List<UserDto>>

    @GET("/users/me")
    suspend fun getMe(): ApiResponse<UserDto>

    @GET("/users/{id}")
    suspend fun getById(
        @Path("id") id: String
    ): ApiResponse<UserDto>

    @PATCH("/users/{id}")
    suspend fun update(
        @Path("id") id: String,
        @Body dto: UpdateUserDto
    ): ApiResponse<UserDto>

    @DELETE("/users/{id}")
    suspend fun delete(
        @Path("id") id: String
    )
}