package com.example.treasurehuntapp.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.treasurehuntapp.data.source.remote.dto.notifications.NotificationDto
import com.example.treasurehuntapp.ui.MainBottomNavBar
import com.example.treasurehuntapp.ui.MainNavTab
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

@Composable
fun NotificationsScreen(
    onHomeClick: () -> Unit,
    onMyHuntsClick: () -> Unit,
    onProfileClick: () -> Unit,
    vm: NotificationsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()

    LaunchedEffect(Unit) {
        vm.load()
        vm.connectRealtime()
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF140A1F), Color(0xFF160A22), Color(0xFF11071B))
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            MainBottomNavBar(
                selected = MainNavTab.Notifications,
                onHomeClick = onHomeClick,
                onMyHuntsClick = onMyHuntsClick,
                onNotificationsClick = {},
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .appPageHeaderPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Mark all as read",
                    color = Color(0xFFA23CFF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { vm.markAllAsRead() }
                )
            }

            Spacer(Modifier.height(14.dp))

            when {
                state.loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFA23CFF))
                    }
                }

                state.error != null -> {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }

                state.notifications.isEmpty() -> {
                    Surface(
                        color = Color(0xFF20162A),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "No notifications yet.",
                            color = Color.White,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            state.notifications,
                            key = { it.id ?: "${it.type}-${it.createdAt}" }
                        ) { n ->
                            val isRead = isNotificationRead(n)
                            NotificationCard(
                                notification = n,
                                accent = !isRead,
                                onClick = {
                                    if (!isRead) vm.markRead(n.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDto,
    accent: Boolean,
    onClick: () -> Unit
) {
    val title = notification.title?.takeIf { it.isNotBlank() }
        ?: notification.type?.replace("_", " ")?.lowercase()?.split(" ")
            ?.joinToString(" ") { word -> word.replaceFirstChar { c -> c.titlecase() } }
        ?: "Notification"
    val message = notification.message.orEmpty()
    val timeLabel = relativeTimeLabel(notification.createdAt)
    val icon = notificationIcon(notification.type)

    val container = if (accent) Color(0xFF261236) else Color(0xFF221A2C)
    val iconBg = if (accent) Color(0xFF3A1658) else Color(0xFF273246)
    val iconTint = if (accent) Color(0xFFB24BFF) else Color(0xFF94A3B8)

    Surface(
        color = container,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (message.isNotBlank()) {
                    Text(
                        text = message,
                        color = Color(0xFF8C84A8),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = timeLabel,
                    color = Color(0xFF8C84A8),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            if (accent) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(6.dp)
                        .background(Color(0xFFA23CFF), CircleShape)
                )
            }
        }
    }
}

private fun isNotificationRead(notification: NotificationDto): Boolean {
    return !notification.readAt.isNullOrBlank()
}

private fun notificationIcon(type: String?): ImageVector {
    val normalized = type.orEmpty().lowercase(Locale.getDefault())
    return when {
        "join" in normalized -> Icons.Rounded.Explore
        "start" in normalized -> Icons.Rounded.PlayCircle
        "location" in normalized || "reach" in normalized -> Icons.Rounded.LocationOn
        "clue" in normalized -> Icons.Rounded.TipsAndUpdates
        "confirm" in normalized || "verified" in normalized -> Icons.Rounded.CheckCircle
        else -> Icons.Rounded.Info
    }
}

private fun relativeTimeLabel(iso: String?): String {
    val millis = parseIsoMillis(iso) ?: return "Just now"
    val diff = abs(System.currentTimeMillis() - millis)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < minute -> "Just now"
        diff < hour -> "${diff / minute} min ago"
        diff < day -> "${diff / hour} hours ago"
        diff < 2 * day -> "Yesterday"
        else -> "${diff / day} days ago"
    }
}

private fun parseIsoMillis(iso: String?): Long? {
    if (iso.isNullOrBlank()) return null
    val patterns = arrayOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'"
    )
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = sdf.parse(iso)
            if (parsed != null) return parsed.time
        } catch (_: Exception) {
        }
    }
    return null
}
