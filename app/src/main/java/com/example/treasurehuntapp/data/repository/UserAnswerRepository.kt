package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.remote.api.UserAnswerApi
import com.example.treasurehuntapp.data.source.remote.dto.answer.CreateUserAnswerDto
import com.example.treasurehuntapp.data.source.remote.dto.answer.UserAnswerResultDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAnswerRepository @Inject constructor(
    private val api: UserAnswerApi
) {
    suspend fun submitAnswer(locationId: String, answer: String): UserAnswerResultDto {
        return api.create(
            CreateUserAnswerDto(
                locationId = locationId,
                answer = answer
            )
        ).data
    }
}
