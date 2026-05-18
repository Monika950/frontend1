package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.realtime.NotificationsSocketDataSource
import com.example.treasurehuntapp.data.source.realtime.NotificationsSocketEvent
import com.example.treasurehuntapp.data.source.realtime.TrackingSocketDataSource
import com.example.treasurehuntapp.data.source.realtime.TrackingSocketEvent
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RealtimeRepository @Inject constructor(
    private val trackingDataSource: TrackingSocketDataSource,
    private val notificationsDataSource: NotificationsSocketDataSource
) {
    val trackingEvents: SharedFlow<TrackingSocketEvent> = trackingDataSource.events
    val notificationEvents: SharedFlow<NotificationsSocketEvent> = notificationsDataSource.events

    fun connectNotifications(): Boolean = notificationsDataSource.connect()
    fun disconnectNotifications() = notificationsDataSource.disconnect()

    fun connectTracking(): Boolean = trackingDataSource.connect()
    fun disconnectTracking() = trackingDataSource.disconnect()

    fun joinTrackingRoom(huntId: String) = trackingDataSource.joinHunt(huntId)
    fun leaveTrackingRoom(huntId: String) = trackingDataSource.leaveHunt(huntId)
    fun updateTrackingPosition(huntId: String, lat: Double, lng: Double, ts: String? = null) {
        trackingDataSource.updatePosition(huntId, lat, lng, ts)
    }
}
