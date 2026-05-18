package com.example.treasurehuntapp.data.repository

import com.example.treasurehuntapp.data.source.remote.api.NotificationsApi
import com.example.treasurehuntapp.data.source.remote.dto.notifications.NotificationDto
import com.example.treasurehuntapp.data.source.remote.dto.notifications.ReadBatchDto
import com.example.treasurehuntapp.data.source.remote.dto.notifications.ReadBatchResultDto
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepository @Inject constructor(
    private val api: NotificationsApi
) {

    suspend fun listNotifications(
        limit: Int? = null,
        page: Int? = null,
        read: Boolean? = null
    ): List<NotificationDto> {
        val data = api.list(limit = limit, page = page, read = read).data
        return parseNotificationsList(data)
    }

    suspend fun markRead(id: String): NotificationDto {
        return api.markRead(id).data
    }

    suspend fun markReadBatch(ids: List<String>): ReadBatchResultDto {
        return api.markReadBatch(ReadBatchDto(ids)).data
    }

    private fun parseNotificationsList(data: JsonElement): List<NotificationDto> {
        val gson = Gson()
        val listType = object : TypeToken<List<NotificationDto>>() {}.type

        return when {
            data.isJsonArray -> gson.fromJson(data as JsonArray, listType)
            data.isJsonObject -> {
                val obj = data.asJsonObject
                val nested = obj.get("data")
                if (nested != null && nested.isJsonArray) {
                    gson.fromJson(nested, listType)
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
