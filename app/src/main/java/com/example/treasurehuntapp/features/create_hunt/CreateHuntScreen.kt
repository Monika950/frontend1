package com.example.treasurehuntapp.features.create_hunt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateHuntScreen(
    onHuntCreated: () -> Unit,
    onBack: () -> Unit,
    vm: CreateHuntViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Create Hunt", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = vm::onTitleChange,
            label = { Text("Hunt Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.description,
            onValueChange = vm::onDescriptionChange,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.createHunt(onHuntCreated) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create")
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
