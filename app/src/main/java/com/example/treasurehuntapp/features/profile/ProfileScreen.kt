// ProfileScreen.kt
package com.example.treasurehuntapp.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel()
) {
    val s = vm.state

    LaunchedEffect(Unit) {
        vm.loadMe()
    }

    LaunchedEffect(s.deleted) {
        if (s.deleted) onLogout()
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF1B0B2A), Color(0xFF3A1458), Color(0xFF7A2B7F))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←", color = Color.White) }
                },
                actions = {
                    IconButton(onClick = { vm.loadMe() }) { Text("↻", color = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3A1458),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { pad ->
        Box(
            Modifier
                .fillMaxSize()
                .background(bg)
                .padding(pad)
        ) {

            when {
                s.loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(Modifier.height(10.dp))
                        Text("Loading...", color = Color.White.copy(alpha = 0.8f))
                    }
                }

                s.error != null -> {
                    ErrorBox(s.error!!, onRetry = { vm.loadMe() })
                }

                s.user == null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(Modifier.height(10.dp))
                        Text("Loading profile...", color = Color.White.copy(alpha = 0.8f))
                    }
                }

                else -> {
                    ProfileContent(
                        state = s,
                        onEmail = vm::onEmail,
                        onUsername = vm::onUsername,
                        onFirstName = vm::onFirstName,
                        onLastName = vm::onLastName,
                        onSave = vm::save,
                        onLogout = onLogout,
                        onDeleteAccount = vm::deleteAccount
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    onEmail: (String) -> Unit,
    onUsername: (String) -> Unit,
    onFirstName: (String) -> Unit,
    onLastName: (String) -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val me = state.user!!
    var showDeleteDialog by remember { mutableStateOf(false) }
    val initials = ((state.firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "") +
            (state.lastName.firstOrNull()?.uppercaseChar()?.toString() ?: ""))
        .ifBlank { state.username.firstOrNull()?.uppercaseChar()?.toString() ?: "U" }

    val isDirty =
        state.email != me.email ||
                state.username != me.username ||
                state.firstName != me.firstName ||
                state.lastName != me.lastName

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        GlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .background(Color(0xFFEC4899).copy(alpha = 0.22f), CircleShape)
                        .border(2.dp, Color(0xFFA855F7).copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color.White, fontWeight = FontWeight.Black)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "${state.firstName} ${state.lastName}".trim().ifBlank { state.username },
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = state.email,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "ID: ${me.id}",
                        color = Color.White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Created: ${me.createdAt.take(10)}",
                        color = Color.White.copy(alpha = 0.55f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        GlassCard {
            Field("Email", state.email, onEmail)
            Field("Username", state.username, onUsername)

            Row {
                Box(Modifier.weight(1f).padding(end = 8.dp)) {
                    Field("First name", state.firstName, onFirstName)
                }
                Box(Modifier.weight(1f).padding(start = 8.dp)) {
                    Field("Last name", state.lastName, onLastName)
                }
            }

            Spacer(Modifier.height(6.dp))

            Button(
                onClick = onSave,
                enabled = isDirty && !state.saving,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEC4899),
                    contentColor = Color.White
                )
            ) {
                if (state.saving) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                else Text("Save", fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color(0xFFA855F7).copy(alpha = 0.18f),
                    contentColor = Color.White
                )
            ) {
                Text("Logout", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { showDeleteDialog = true },
                enabled = !state.deleting,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                )
            ) {
                if (state.deleting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Delete account", fontWeight = FontWeight.Bold)
                }
            }

            if (state.deleteError != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.deleteError, color = Color(0xFFFCA5A5))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GlassCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
//    keyboardType: KeyboardType
) {
    Text(label, color = Color.White.copy(alpha = 0.75f), fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
//        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White.copy(alpha = 0.22f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
            focusedContainerColor = Color.White.copy(alpha = 0.08f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        GlassCard {
            Text("Error", color = Color.White, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(8.dp))
            Text(message, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899))
            ) { Text("Retry", color = Color.White, fontWeight = FontWeight.Black) }
        }
    }
}

