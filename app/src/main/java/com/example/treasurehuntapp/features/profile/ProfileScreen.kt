package com.example.treasurehuntapp.features.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))
        Text("Username: ${state.username}")
        Text("Email: ${state.email}")

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
