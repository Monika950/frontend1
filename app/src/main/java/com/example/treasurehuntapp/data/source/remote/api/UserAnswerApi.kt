package com.example.treasurehuntapp.data.source.remote.api

import com.example.treasurehuntapp.data.source.remote.dto.answer.CreateUserAnswerDto
import com.example.treasurehuntapp.data.source.remote.dto.answer.UserAnswerResultDto
import com.example.treasurehuntapp.data.source.remote.dto.common.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface UserAnswerApi {
    @POST("user-answer")
    suspend fun create(@Body body: CreateUserAnswerDto): ApiResponse<UserAnswerResultDto>
}
