package com.example.treasurehuntapp.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state = vm.state

    LaunchedEffect(Unit) {
        if (state.user == null && !state.loading) vm.loadMe()
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF13071E), Color(0xFF180A26), Color(0xFF100519))
    )

    val user = state.user
    val isDirty = user != null && (
        state.email != user.email ||
            state.username != user.username ||
            state.firstName != user.firstName ||
            state.lastName != user.lastName
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF160A22),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.loading && state.user == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF8E63FF))
                }
                return@Column
            }

            if (!state.error.isNullOrBlank()) {
                Surface(
                    color = Color(0xFF2A1524),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = state.error.orEmpty(),
                        color = Color(0xFFFF6E8A),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Surface(
                color = Color(0xFF1B1125),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Update your profile information",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "These changes will be visible in hunts and rankings.",
                        color = Color(0xFF8C84A8),
                        style = MaterialTheme.typography.bodySmall
                    )

                    EditField("Username", state.username, vm::onUsername)
                    EditField("Email", state.email, vm::onEmail)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) {
                            EditField("First name", state.firstName, vm::onFirstName)
                        }
                        Box(Modifier.weight(1f)) {
                            EditField("Last name", state.lastName, vm::onLastName)
                        }
                    }
                }
            }

            Surface(
                color = Color(0xFF1B1125),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Change password",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    PasswordEditField("Current password", state.changePasswordCurrent, vm::onCurrentPassword)
                    PasswordEditField("New password", state.changePasswordNew, vm::onNewPassword)
                    PasswordEditField("Confirm new password", state.changePasswordConfirm, vm::onConfirmPassword)

                    state.changePasswordError?.let {
                        Text(it, color = Color(0xFFFF6E8A), style = MaterialTheme.typography.bodySmall)
                    }
                    state.changePasswordSuccess?.let {
                        Text(it, color = Color(0xFF7DE39E), style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = vm::changePassword,
                        enabled = !state.changingPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F1E40),
                            contentColor = Color(0xFFD0BCFF)
                        )
                    ) {
                        if (state.changingPassword) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Change Password", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Button(
                onClick = vm::save,
                enabled = isDirty && !state.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8E63FF),
                    contentColor = Color.White
                )
            ) {
                if (state.saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFD0BCFF)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4D257B))
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun PasswordEditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = Color(0xFF8C84A8),
            style = MaterialTheme.typography.labelSmall
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = Color(0xFF8C84A8)
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1A1431),
                unfocusedContainerColor = Color(0xFF1A1431),
                disabledContainerColor = Color(0xFF1A1431),
                focusedBorderColor = Color(0xFF8E63FF),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF8E63FF)
            )
        )
    }
}

@Composable
private fun EditField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = Color(0xFF8C84A8),
            style = MaterialTheme.typography.labelSmall
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF221530),
                unfocusedContainerColor = Color(0xFF221530),
                focusedBorderColor = Color(0xFF8E63FF),
                unfocusedBorderColor = Color(0xFF3A2B4C),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF8E63FF)
            )
        )
    }
}
