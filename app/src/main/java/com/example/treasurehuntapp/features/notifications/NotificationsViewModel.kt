package com.example.treasurehuntapp.features.notifications

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(
        NotificationsState(
            notifications = listOf(
                "Hunt started!",
                "New location added!",
                "You found a clue 🎉"
            )
        )
    )
    val state = _state.asStateFlow()
}
