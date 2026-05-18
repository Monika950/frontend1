package com.example.treasurehuntapp.data.source.remote.dto.notifications

data class NotificationDto(
    val id: String? = null,
    val type: String? = null,
    val payload: Map<String, Any?>? = null,
    val huntId: String? = null,
    val title: String? = null,
    val message: String? = null,
    val read: Boolean? = null,
    val readAt: String? = null,
    val createdAt: String? = null
)
