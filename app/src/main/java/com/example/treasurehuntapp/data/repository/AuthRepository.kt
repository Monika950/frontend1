package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.remote.api.AuthApi
import com.example.treasurehuntapp.data.remote.auth.TokenStorage
import com.example.treasurehuntapp.data.source.remote.dto.auth.LoginDto
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
            userId = res.id,
            username = res.username,
            email = res.email,
            accessToken = res.accessToken,
            refreshToken = res.refreshToken
        )
    }

    suspend fun logout() {
        api.logout()
        tokenStorage.clear()
    }
}
