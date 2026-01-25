package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.auth.*
import com.example.treasurehuntapp.data.source.remote.dto.auth.AuthResponseDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.ChangePasswordDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.ForgotPasswordDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.LoginDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.RefreshTokenDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.RegisterDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.ResetPasswordDto
import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import retrofit2.http.*

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterDto): ApiResponse<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(@Body body: LoginDto): ApiResponse<AuthResponseDto>

    @GET("auth/me")
    suspend fun me(): ApiResponse<MeDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenDto): ApiResponse<AuthResponseDto>

    @PATCH("auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordDto)

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordDto)

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordDto)

    @POST("auth/logout")
    suspend fun logout()
}

