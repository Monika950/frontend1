package com.example.treasurehuntapp.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen(
    onCreateHuntClick: () -> Unit,
    onJoinHuntClick: () -> Unit,
    onActiveHuntClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Column {
        Button(onClick = onCreateHuntClick) { Text("Create Hunt") }
        Button(onClick = onJoinHuntClick) { Text("Join Hunt") }
        Button(onClick = onActiveHuntClick) { Text("Active Hunt") }
        Button(onClick = onProfileClick) { Text("Profile") }
        Button(onClick = onNotificationsClick) { Text("Notifications") }
    }
}

