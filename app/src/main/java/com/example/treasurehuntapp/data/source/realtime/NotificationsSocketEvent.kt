package com.example.treasurehuntapp.data.source.realtime

import com.example.treasurehuntapp.data.source.remote.dto.notifications.NotificationDto

sealed interface NotificationsSocketEvent {
    data class New(val notification: NotificationDto) : NotificationsSocketEvent
    data class Read(val id: String, val readAt: String) : NotificationsSocketEvent
    data class ReadBatch(val ids: List<String>, val readAt: String) : NotificationsSocketEvent
}
