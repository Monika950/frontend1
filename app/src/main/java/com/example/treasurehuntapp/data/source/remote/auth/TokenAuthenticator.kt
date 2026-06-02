package com.example.treasurehuntapp.data.source.remote.auth

import com.example.treasurehuntapp.data.source.remote.api.AuthApi
import com.example.treasurehuntapp.data.source.remote.auth.EncryptedTokenStorage
import com.example.treasurehuntapp.data.source.remote.dto.auth.RefreshTokenDto
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStorage: EncryptedTokenStorage,
    @Named("authApiNoAuth") private val noAuthApi: AuthApi
) : Authenticator {

    private val refreshMutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        return runBlocking {
            refreshMutex.withLock {
                val latestToken = tokenStorage.accessTokenNow()
                if (!latestToken.isNullOrBlank() && latestToken != requestToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $latestToken")
                        .build()
                }

                val refreshed = refreshToken()
                if (!refreshed) return@withLock null

                val newToken = tokenStorage.accessTokenNow() ?: return@withLock null
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var r: Response? = response
        var count = 1
        while (r?.priorResponse != null) {
            count++
            r = r.priorResponse
        }
        return count
    }

    private suspend fun refreshToken(): Boolean {
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
}
