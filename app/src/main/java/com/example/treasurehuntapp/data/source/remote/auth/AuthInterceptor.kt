package com.example.treasurehuntapp.data.source.remote.auth

import com.example.treasurehuntapp.data.source.remote.auth.EncryptedTokenStorage
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStorage: EncryptedTokenStorage
) : Interceptor {
    private fun isAuthEndpoint(path: String): Boolean {
        return path.startsWith("/auth/login") ||
                path.startsWith("/auth/register") ||
                path.startsWith("/auth/refresh") ||
                path.startsWith("/auth/forgot-password") ||
                path.startsWith("/auth/reset-password")
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        if (isAuthEndpoint(path)) {
            return chain.proceed(request)
        }

        val token = tokenStorage.accessTokenNow()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
