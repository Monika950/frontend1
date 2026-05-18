package com.example.treasurehuntapp.data.source.remote.dto.notifications

data class NotificationPageDto(
    val data: List<NotificationDto> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val limit: Int = 0
)
