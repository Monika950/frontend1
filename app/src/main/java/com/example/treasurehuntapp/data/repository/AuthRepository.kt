package com.example.treasurehuntapp.data.repository

import android.util.Log
import com.example.treasurehuntapp.data.source.remote.api.AuthApi
import com.example.treasurehuntapp.data.source.remote.auth.EncryptedTokenStorage
import com.example.treasurehuntapp.data.source.remote.dto.auth.LoginDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.RefreshTokenDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.RegisterDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.ForgotPasswordDto
import com.example.treasurehuntapp.data.source.remote.dto.auth.ChangePasswordDto
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApi,
    @Named("authApiNoAuth") private val noAuthApi: AuthApi,
    private val tokenStorage: EncryptedTokenStorage
) {
    companion object {
        private const val TAG = "AuthRepository"
    }

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

    suspend fun forgotPassword(email: String) {
        val body = ForgotPasswordDto(email = email.trim())
        Log.d(TAG, "forgotPassword request: email=${body.email}")

        val response = noAuthApi.forgotPassword(body)

        val errorBody = response.errorBody()?.string()
        Log.d(
            TAG,
            "forgotPassword response: code=${response.code()} success=${response.isSuccessful} body=${errorBody ?: "<empty>"}"
        )

        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    suspend fun resetPassword(token: String, newPassword: String) {
        api.resetPassword(
            com.example.treasurehuntapp.data.source.remote.dto.auth.ResetPasswordDto(
                token = token,
                newPassword = newPassword
            )
        )
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        api.changePassword(
            ChangePasswordDto(
                oldPassword = currentPassword,
                newPassword = newPassword
            )
        )
    }

    suspend fun refreshToken(): Boolean {
        val refreshToken = tokenStorage.refreshTokenNow() ?: return false
        val userId = tokenStorage.userIdNow() ?: return false

        return try {
            val res = noAuthApi.refresh(
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
            tokenStorage.clear()
            false
        }
    }

    suspend fun validateSession(): Boolean {
        val token = tokenStorage.accessTokenNow()
        if (token.isNullOrBlank()) return false

        return try {
            api.me()
            true
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401) {
                tokenStorage.clear()
                false
            } else {
                true
            }
        }
    }
}

