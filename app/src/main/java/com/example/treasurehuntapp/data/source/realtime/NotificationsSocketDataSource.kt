package com.example.treasurehuntapp.data.source.realtime

import com.example.treasurehuntapp.data.source.remote.dto.notifications.NotificationDto
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsSocketDataSource @Inject constructor(
    private val socketFactory: SocketFactory
) {
    private var socket: Socket? = null

    private val _events = MutableSharedFlow<NotificationsSocketEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<NotificationsSocketEvent> = _events

    fun connect(): Boolean {
        if (socket?.connected() == true) return true
        val created = socketFactory.create("notifications") ?: return false
        socket = created
        bindEvents(created)
        created.connect()
        return true
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
    }

    private fun bindEvents(socket: Socket) {
        socket.on("notifications:new") { args ->
            val json = args.firstOrNull() as? JSONObject ?: return@on
            val event = NotificationsSocketEvent.New(json.toNotificationDto())
            _events.tryEmit(event)
        }

        socket.on("notifications:read") { args ->
            val json = args.firstOrNull() as? JSONObject ?: return@on
            val id = json.optString("id", "")
            val readAt = json.optString("readAt", "")
            if (id.isNotBlank() && readAt.isNotBlank()) {
                _events.tryEmit(NotificationsSocketEvent.Read(id = id, readAt = readAt))
            }
        }

        socket.on("notifications:read_batch") { args ->
            val json = args.firstOrNull() as? JSONObject ?: return@on
            val idsArray = json.optJSONArray("ids") ?: JSONArray()
            val ids = buildList {
                for (i in 0 until idsArray.length()) {
                    val id = idsArray.optString(i)
                    if (!id.isNullOrBlank()) add(id)
                }
            }
            val readAt = json.optString("readAt", "")
            if (ids.isNotEmpty() && readAt.isNotBlank()) {
                _events.tryEmit(NotificationsSocketEvent.ReadBatch(ids = ids, readAt = readAt))
            }
        }
    }

    private fun JSONObject.toNotificationDto(): NotificationDto {
        return NotificationDto(
            id = nullableString("id"),
            type = nullableString("type"),
            huntId = nullableString("huntId"),
            readAt = nullableString("readAt"),
            createdAt = nullableString("createdAt")
        )
    }

    private fun JSONObject.nullableString(key: String): String? {
        val value = optString(key, "")
        return value.ifBlank { null }
    }
}
