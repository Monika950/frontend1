package com.example.treasurehuntapp.features.notifications

import com.example.treasurehuntapp.data.source.remote.dto.notifications.NotificationDto

data class NotificationsState(
    val notifications: List<NotificationDto> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)
