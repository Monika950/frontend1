package com.example.treasurehuntapp.data.source.realtime

import com.example.treasurehuntapp.BuildConfig
import com.example.treasurehuntapp.data.source.remote.auth.TokenStorage
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketFactory @Inject constructor(
    private val tokenStorage: TokenStorage
) {

    fun create(namespace: String): Socket? {
        val token = tokenStorage.accessTokenNow() ?: return null
        if (token.isBlank()) return null

        val options = IO.Options().apply {
            transports = arrayOf(WebSocket.NAME)
            forceNew = true
            reconnection = true
            timeout = 10000
            auth = mapOf("token" to token)
            extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
        }

        return IO.socket("$SOCKET_BASE_URL/$namespace", options)
    }

    private companion object {
        const val SOCKET_BASE_URL = BuildConfig.SOCKET_BASE_URL
    }
}
