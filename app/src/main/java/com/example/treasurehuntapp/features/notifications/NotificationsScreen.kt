package com.example.treasurehuntapp.features.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    vm: NotificationsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Notifications", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        state.notifications.forEach {
            Text("• $it")
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
