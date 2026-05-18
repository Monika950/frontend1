package com.example.treasurehuntapp.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.treasurehuntapp.data.repository.NotificationsRepository
import com.example.treasurehuntapp.data.repository.RealtimeRepository
import com.example.treasurehuntapp.data.source.realtime.NotificationsSocketEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repo: NotificationsRepository,
    private val realtimeRepository: RealtimeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsState())
    val state = _state.asStateFlow()

    init {
        observeRealtimeNotifications()
    }

    fun load() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(loading = true, error = null)
                val items = repo.listNotifications()
                _state.value = _state.value.copy(
                    loading = false,
                    notifications = items
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load notifications"
                )
            }
        }
    }

    fun connectRealtime() {
        realtimeRepository.connectNotifications()
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val ids = _state.value.notifications
                .filter { it.readAt.isNullOrBlank() }
                .mapNotNull { it.id }
            if (ids.isEmpty()) return@launch

            try {
                repo.markReadBatch(ids)
                val nowReadMarker = System.currentTimeMillis().toString()
                _state.update { current ->
                    current.copy(
                        notifications = current.notifications.map { n ->
                            if (n.id != null && ids.contains(n.id)) {
                                n.copy(read = true, readAt = n.readAt ?: nowReadMarker)
                            } else {
                                n
                            }
                        }
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    fun markRead(id: String?) {
        val notificationId = id?.takeIf { it.isNotBlank() } ?: return
        viewModelScope.launch {
            try {
                val updated = repo.markRead(notificationId)
                _state.update { current ->
                    current.copy(
                        notifications = current.notifications.map { n ->
                            if (n.id == notificationId) {
                                n.copy(
                                    read = true,
                                    readAt = updated.readAt ?: n.readAt ?: System.currentTimeMillis().toString()
                                )
                            } else n
                        }
                    )
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun observeRealtimeNotifications() {
        viewModelScope.launch {
            realtimeRepository.notificationEvents.collect { event ->
                when (event) {
                    is NotificationsSocketEvent.New -> {
                        _state.update { current ->
                            current.copy(notifications = listOf(event.notification) + current.notifications)
                        }
                    }

                    is NotificationsSocketEvent.Read -> {
                        _state.update { current ->
                            current.copy(
                                notifications = current.notifications.map { n ->
                                    if (n.id == event.id) {
                                        n.copy(read = true, readAt = event.readAt)
                                    } else {
                                        n
                                    }
                                }
                            )
                        }
                    }

                    is NotificationsSocketEvent.ReadBatch -> {
                        val ids = event.ids.toSet()
                        _state.update { current ->
                            current.copy(
                                notifications = current.notifications.map { n ->
                                    if (n.id != null && ids.contains(n.id)) {
                                        n.copy(read = true, readAt = event.readAt)
                                    } else {
                                        n
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        realtimeRepository.disconnectNotifications()
        super.onCleared()
    }
}
