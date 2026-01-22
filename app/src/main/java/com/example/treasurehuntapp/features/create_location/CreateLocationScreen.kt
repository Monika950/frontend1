package com.example.treasurehuntapp.features.create_location

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateLocationScreen(
    onLocationCreated: () -> Unit,
    onBack: () -> Unit,
    vm: CreateLocationViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create Location", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.locationName,
            onValueChange = vm::onLocationNameChange,
            label = { Text("Location name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.clue,
            onValueChange = vm::onClueChange,
            label = { Text("Clue / Hint") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.save(onLocationCreated) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Location")
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
