package com.example.treasurehuntapp.data.source.realtime

import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackingSocketDataSource @Inject constructor(
    private val socketFactory: SocketFactory
) {
    private var socket: Socket? = null
    private var lastPositionUpdateAtMs: Long = 0L

    private val _events = MutableSharedFlow<TrackingSocketEvent>(extraBufferCapacity = 32)
    val events: SharedFlow<TrackingSocketEvent> = _events

    fun connect(): Boolean {
        if (socket?.connected() == true) return true
        val created = socketFactory.create("tracking") ?: return false
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

    fun joinHunt(huntId: String) {
        socket?.emit("tracking:join", JSONObject().put("huntId", huntId))
    }

    fun leaveHunt(huntId: String) {
        socket?.emit("tracking:leave", JSONObject().put("huntId", huntId))
    }

    fun updatePosition(huntId: String, lat: Double, lng: Double, ts: String? = null) {
        val now = System.currentTimeMillis()
        if (now - lastPositionUpdateAtMs < 1000L) return
        lastPositionUpdateAtMs = now

        val payload = JSONObject()
            .put("huntId", huntId)
            .put("lat", lat)
            .put("lng", lng)
        if (!ts.isNullOrBlank()) payload.put("ts", ts)

        socket?.emit("tracking:update_position", payload)
    }

    private fun bindEvents(socket: Socket) {
        socket.on("tracking:participant_joined") { args ->
            val json = args.firstOrNull() as? JSONObject ?: return@on
            val userId = json.optString("userId", "")
            if (userId.isNotBlank()) {
                _events.tryEmit(TrackingSocketEvent.ParticipantJoined(userId = userId))
            }
        }

        socket.on("tracking:participant_left") { args ->
            val json = args.firstOrNull() as? JSONObject ?: return@on
            val userId = json.optString("userId", "")
            if (userId.isNotBlank()) {
                _events.tryEmit(TrackingSocketEvent.ParticipantLeft(userId = userId))
            }
        }

        socket.on("tracking:position_updated") { args ->
            val json = args.firstOrNull() as? JSONObject ?: return@on
            val userId = json.optString("userId", "")
            val ts = json.optString("ts", "")
            val hasLat = json.has("lat")
            val hasLng = json.has("lng")
            if (userId.isBlank() || ts.isBlank() || !hasLat || !hasLng) return@on

            _events.tryEmit(
                TrackingSocketEvent.PositionUpdated(
                    userId = userId,
                    lat = json.optDouble("lat"),
                    lng = json.optDouble("lng"),
                    ts = ts,
                    currentLocationId = json.optString("currentLocationId", "").ifBlank { null }
                )
            )
        }
    }
}
