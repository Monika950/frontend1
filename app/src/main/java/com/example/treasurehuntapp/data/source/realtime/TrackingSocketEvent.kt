package com.example.treasurehuntapp.data.source.realtime

sealed interface TrackingSocketEvent {
    data class ParticipantJoined(val userId: String) : TrackingSocketEvent
    data class ParticipantLeft(val userId: String) : TrackingSocketEvent
    data class PositionUpdated(
        val userId: String,
        val lat: Double,
        val lng: Double,
        val ts: String,
        val currentLocationId: String?
    ) : TrackingSocketEvent
}
