package com.example.treasurehuntapp.data.source.remote.auth

import com.example.treasurehuntapp.data.source.remote.auth.TokenStorage
import com.example.treasurehuntapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnauthorizedInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage,
    @ApplicationScope private val appScope: CoroutineScope
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val hadAuthHeader = request.header("Authorization") != null
        if (hadAuthHeader && response.code == 401 && !tokenStorage.accessTokenNow().isNullOrBlank()) {
            appScope.launch {
                tokenStorage.clear()
            }
        }

        return response
    }
}
