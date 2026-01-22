package com.example.treasurehuntapp.features.join_hunt

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun JoinHuntScreen(
    onJoinSuccess: () -> Unit,
    onBack: () -> Unit,
    vm: JoinHuntViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Join Hunt", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.code,
            onValueChange = vm::onCodeChange,
            label = { Text("Enter code") },
            modifier = Modifier.fillMaxWidth()
        )

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { vm.join(onJoinSuccess) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Join")
        }

        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}
