package com.example.treasurehuntapp.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.treasurehuntapp.ui.MainBottomNavBar
import com.example.treasurehuntapp.ui.MainNavTab
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun ProfileScreen(
    onHomeClick: () -> Unit = {},
    onMyHuntsClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    vm: ProfileViewModel = hiltViewModel()
) {
    val s = vm.state

    LaunchedEffect(Unit) { vm.loadMe() }
    LaunchedEffect(s.deleted) { if (s.deleted) onLogout() }
    LaunchedEffect(s.loggedOut) { if (s.loggedOut) onLogout() }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF13071E), Color(0xFF180A26), Color(0xFF100519))
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            MainBottomNavBar(
                selected = MainNavTab.Profile,
                onHomeClick = onHomeClick,
                onMyHuntsClick = onMyHuntsClick,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = {}
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
        ) {
            when {
                s.loading || s.user == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF8E63FF))
                    }
                }

                s.error != null -> {
                    ProfileErrorCard(message = s.error!!, onRetry = vm::loadMe)
                }

                else -> {
                    ProfileContent(
                        state = s,
                        onEditProfileClick = onEditProfileClick,
                        onLogout = vm::logout,
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
    onEditProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    val user = state.user ?: return
    var showDeleteDialog by remember { mutableStateOf(false) }

    val displayName = "${state.firstName} ${state.lastName}".trim().ifBlank { state.username }
    val initials = (
        (state.firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "") +
            (state.lastName.firstOrNull()?.uppercaseChar()?.toString() ?: "")
        ).ifBlank {
            state.username.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeroCard(
            initials = initials,
            displayName = displayName,
            username = state.username,
            userId = user.id
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle("STATISTICS")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Flag,
                    value = state.huntsCompletedCount.toString(),
                    label = "Hunts Completed"
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.PersonOutline,
                    value = state.huntsCreatedCount.toString(),
                    label = "Hunts Created"
                )
            }

            SectionTitle("PERSONAL INFO")

            InfoCardRow(
                icon = Icons.Rounded.Email,
                label = "Email",
                value = state.email
            )
            InfoCardRow(
                icon = Icons.Rounded.Today,
                label = "Joined",
                value = formatJoinedLabel(user.createdAt)
            )

            Button(
                onClick = onEditProfileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8E63FF),
                    contentColor = Color.White
                )
            ) {
                Text("Edit Profile", fontWeight = FontWeight.Bold)
            }

            if (!state.logoutError.isNullOrBlank()) {
                Text(
                    text = state.logoutError.orEmpty(),
                    color = Color(0xFFFF6E8A),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!state.deleteError.isNullOrBlank()) {
                Text(
                    text = state.deleteError.orEmpty(),
                    color = Color(0xFFFF6E8A),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlineActionButton(
                text = "Logout",
                icon = Icons.Rounded.ExitToApp,
                color = Color(0xFF8E63FF),
                border = Color(0xFF4D257B),
                loading = state.loggingOut,
                onClick = onLogout
            )

            OutlineActionButton(
                text = "Delete Account",
                icon = Icons.Rounded.DeleteOutline,
                color = Color(0xFFFF5E7A),
                border = Color(0xFF612338),
                loading = state.deleting,
                onClick = { showDeleteDialog = true }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1B1125),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0A8C6),
            title = {
                Text(
                    "Delete account?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB4233D),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD0BCFF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4D257B))
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@Composable
private fun ProfileHeroCard(
    initials: String,
    displayName: String,
    username: String,
    userId: String
) {
    val heroGradient = Brush.verticalGradient(
        listOf(Color(0xFF8A19FF), Color(0xFF6A16C8), Color(0xFF4E119A))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(174.dp)
            .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            .background(heroGradient)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 50.dp)
                .size(68.dp)
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 0.dp, end = 18.dp)
                .size(78.dp)
                .background(Color.White.copy(alpha = 0.10f), CircleShape)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = displayName,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "@$username",
                color = Color(0xFFD8C7FF),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "ID: ${formatProfileId(userId)}",
                color = Color(0xFFD8C7FF).copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF8E63FF),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1B1125),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF8E63FF), modifier = Modifier.size(16.dp))
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                color = Color(0xFF8C84A8),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun InfoCardRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Surface(
        color = Color(0xFF1B1125),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = Color(0xFF8E63FF), modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(label, color = Color(0xFF8C84A8))
            }
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EditableProfileCard(
    state: ProfileState,
    onEmail: (String) -> Unit,
    onUsername: (String) -> Unit,
    onFirstName: (String) -> Unit,
    onLastName: (String) -> Unit,
    onSave: () -> Unit,
    isDirty: Boolean
) {
    Surface(
        color = Color(0xFF1B1125),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Edit Profile",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            ProfileField(label = "Username", value = state.username, onValueChange = onUsername)
            ProfileField(label = "Email", value = state.email, onValueChange = onEmail)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    ProfileField(label = "First Name", value = state.firstName, onValueChange = onFirstName)
                }
                Box(modifier = Modifier.weight(1f)) {
                    ProfileField(label = "Last Name", value = state.lastName, onValueChange = onLastName)
                }
            }

            Button(
                onClick = onSave,
                enabled = isDirty && !state.saving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
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
        }
    }
}

@Composable
private fun ProfileField(
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

@Composable
private fun OutlineActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    border: Color,
    loading: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, border)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = color
            )
        } else {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ProfileErrorCard(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF1B1125),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Error", color = Color.White, fontWeight = FontWeight.Bold)
                Text(message, color = Color(0xFF8C84A8))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E63FF),
                        contentColor = Color.White
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

private fun formatJoinedLabel(iso: String?): String {
    if (iso.isNullOrBlank()) return "-"
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (pattern in patterns) {
        try {
            val inFmt = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val date = inFmt.parse(iso)
            if (date != null) return outFmt.format(date)
        } catch (_: Exception) {
        }
    }
    return iso.take(7)
}

private fun formatProfileId(id: String): String {
    if (id.isBlank()) return "-"
    val compact = id.replace("-", "")
    return when {
        compact.length >= 12 -> "${compact.take(4)}-${compact.substring(4, 8)}-${compact.substring(8, 12)}"
        id.length > 16 -> id.take(16)
        else -> id
    }
}
