package com.example.treasurehuntapp.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.treasurehuntapp.ui.MainBottomNavBar
import com.example.treasurehuntapp.ui.MainNavTab
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun HomeScreen(
    onCreateHuntClick: () -> Unit,
    onActiveHuntClick: () -> Unit,
    onHuntsClick: (Boolean) -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    vm: HomeViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var joinCode by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.load() }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF1B0B2A), Color(0xFF2A0F3A), Color(0xFF3A1458))
    )

    Scaffold(
        topBar = {
            TopHeader(onNotificationsClick = onNotificationsClick)
        },
        bottomBar = {
            MainBottomNavBar(
                selected = MainNavTab.Home,
                onHomeClick = {},
                onMyHuntsClick = { onHuntsClick(state.isOwner) },
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateHuntClick,
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF381E72)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Create Hunt")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(Modifier.height(10.dp))
                        Text("Loading hunts...", color = Color.White.copy(alpha = 0.8f))
                    }
                }

                state.error != null -> {
                    ErrorCard(
                        message = state.error.orEmpty(),
                        onRetry = { vm.load() }
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        JoinGameCard(
                            joinCode = joinCode,
                            onJoinCodeChange = { joinCode = it },
                            onJoinClick = {
                                vm.joinByCode(joinCode) {
                                    onActiveHuntClick()
                                }
                            },
                            isJoining = state.joiningByCode,
                            joinError = state.joinError
                        )

                        SectionHeader(title = "Active Hunts")

                        if (state.activeHuntCards.isEmpty()) {
                            EmptyStateCard()
                        } else {
                            state.activeHuntCards.forEach { card ->
                                ActiveHuntCard(card = card, onOpen = onActiveHuntClick)
                            }
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun TopHeader(onNotificationsClick: () -> Unit) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .appPageHeaderPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Hello, Explorer!",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ready for your next hunt?",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    Icons.Rounded.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFFD0BCFF)
                )
            }
        }
    }
}

@Composable
private fun JoinGameCard(
    joinCode: String,
    onJoinCodeChange: (String) -> Unit,
    onJoinClick: () -> Unit,
    isJoining: Boolean,
    joinError: String?
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFF2B2930)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Join a Hunt",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = joinCode,
                onValueChange = { onJoinCodeChange(it.take(6)) },
                label = { Text("Enter Access Code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFD0BCFF),
                    focusedLabelColor = Color(0xFFD0BCFF),
                    cursorColor = Color(0xFFD0BCFF),
                    unfocusedBorderColor = Color(0xFF4A4458),
                    unfocusedLabelColor = Color(0xFFB0A8C6),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            if (!joinError.isNullOrBlank()) {
                Text(
                    text = joinError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = onJoinClick,
                enabled = !isJoining,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF673AB7),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (isJoining) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("JOIN GAME", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFFD0BCFF),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ActiveHuntCard(
    card: ActiveHuntCardState,
    onOpen: () -> Unit
) {
    val hunt = card.hunt
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!card.imageUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = card.imageUrl,
                        contentDescription = "${hunt.name} cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.22f))
                    )
                }
            }
            Text(
                text = hunt.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Started: ${formatStartDate(hunt.start)}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Members: ${card.participantsCount}",
                    color = Color(0xFFB0A8C6),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            LinearProgressIndicator(
                progress = { card.progress },
                color = Color(0xFFD0BCFF),
                trackColor = Color(0xFF4A4458),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(6.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progressLabel(card.currentLocationIndex, card.totalLocations),
                    color = Color(0xFFB0A8C6),
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(onClick = onOpen) {
                    Text("Resume", color = Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("No active hunts yet.", color = Color.White)
            Text(
                "Join or create a hunt to get started.",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatStartDate(isoUtc: String): String {
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (p in patterns) {
        try {
            val inFmt = SimpleDateFormat(p, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outFmt = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = inFmt.parse(isoUtc)
            if (date != null) return outFmt.format(date)
        } catch (_: Exception) {
        }
    }
    return isoUtc
}

private fun progressLabel(currentIndex: Int, total: Int): String {
    if (total <= 0 || currentIndex <= 0) return "Progress: 0%"
    val pct = ((currentIndex.toFloat() / total.toFloat()) * 100f).toInt().coerceIn(0, 100)
    return "Progress: $pct%"
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2B2930))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Error", color = Color.White, fontWeight = FontWeight.Bold)
                Text(message, color = Color.White.copy(alpha = 0.8f))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retry", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
