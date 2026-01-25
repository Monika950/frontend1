package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.remote.api.AuthApi
import com.example.treasurehuntapp.data.source.remote.auth.TokenStorage
import com.example.treasurehuntapp.data.source.remote.dto.auth.LoginDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.RefreshTokenDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.RegisterDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    private val tokenStorage: TokenStorage
) {
    suspend fun login(email: String, password: String) {
        val res = api.login(LoginDto(email = email, password = password))

        tokenStorage.save(
            userId = res.data.id,
            username = res.data.username,
            email = res.data.email,
            accessToken = res.data.accessToken,
            refreshToken = res.data.refreshToken
        )

    }

    suspend fun logout() {
        api.logout()
        tokenStorage.clear()
    }

    suspend fun register(
        email: String,
        password: String,
        username: String,
        firstName: String,
        lastName: String
    ) {
        val res = api.register(
            RegisterDto(
                email = email.trim(),
                password = password,
                username = username.trim(),
                firstName = firstName.trim(),
                lastName = lastName.trim()
            )
        )

        tokenStorage.save(
            userId = res.data.id,
            username = res.data.username,
            email = res.data.email,
            accessToken = res.data.accessToken,
            refreshToken = res.data.refreshToken
        )
    }

    suspend fun refreshToken(): Boolean {
        val refreshToken = tokenStorage.refreshTokenNow() ?: return false
        val userId = tokenStorage.userIdNow() ?: return false

        return try {
            val res = api.refresh(
                RefreshTokenDto(
                    id = userId,
                    refreshToken = refreshToken
                )
            )

            tokenStorage.save(
                userId = res.data.id,
                username = res.data.username,
                email = res.data.email,
                accessToken = res.data.accessToken,
                refreshToken = res.data.refreshToken
            )

            true
        } catch (e: Exception) {
            false
        }
    }
}

