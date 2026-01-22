package com.example.treasurehuntapp.features.active_hunt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ActiveHuntScreen(
    onCreateLocationClick: () -> Unit,
    onBack: () -> Unit,
    vm: ActiveHuntViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Active Hunt", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))
        Text("Hunt: ${state.huntName}")

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onCreateLocationClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Location")
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
