package com.example.treasurehuntapp.features.my_hunts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.treasurehuntapp.ui.UiState
import com.example.treasurehuntapp.ui.appPageHeaderPadding

private enum class HuntsTab { Created, Joined }

@Composable
fun MyHuntsScreen(
    initialTabIsCreated: Boolean,
    onAddClick: () -> Unit,
    onOpenHunt: () -> Unit,
    onHomeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    vm: MyHuntsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var selectedTab by remember(initialTabIsCreated) {
        mutableStateOf(if (initialTabIsCreated) HuntsTab.Created else HuntsTab.Joined)
    }

    LaunchedEffect(Unit) { vm.load() }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF120A1C), Color(0xFF1E0F2B), Color(0xFF2A1336))
    )

    val data = (state as? UiState.Success)?.data
    val list = if (selectedTab == HuntsTab.Created) data?.createdHunts else data?.joinedHunts

    Scaffold(
        topBar = {
            MyHuntsTopBar(onAddClick = onAddClick)
        },
        bottomBar = {
            MainBottomNavBar(
                selected = MainNavTab.MyHunts,
                onHomeClick = onHomeClick,
                onMyHuntsClick = {},
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SegmentedTabs(
                selected = selectedTab,
                onCreatedClick = { selectedTab = HuntsTab.Created },
                onJoinedClick = { selectedTab = HuntsTab.Joined }
            )

            when (state) {
                is UiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFB44BF3))
                    }
                }

                is UiState.Error -> {
                    Text((state as UiState.Error).message, color = MaterialTheme.colorScheme.error)
                }

                is UiState.Success -> {
                    if (list.isNullOrEmpty()) {
                        EmptyCard()
                    } else {
                        list.forEach { item ->
                            HuntCard(
                                item = item,
                                onPrimaryActionClick = {
                                    vm.openHunt(item.hunt, onOpenHunt)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MyHuntsTopBar(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .appPageHeaderPadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "My Hunts",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onAddClick) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B1B3C)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add", tint = Color(0xFFB44BF3))
            }
        }
    }
}

@Composable
private fun SegmentedTabs(
    selected: HuntsTab,
    onCreatedClick: () -> Unit,
    onJoinedClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1F1628),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabPill(
                text = "Created",
                selected = selected == HuntsTab.Created,
                onClick = onCreatedClick
            )
            TabPill(
                text = "Joined",
                selected = selected == HuntsTab.Joined,
                onClick = onJoinedClick
            )
        }
    }
}

@Composable
private fun RowScope.TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFFB44BF3) else Color.Transparent
    val textColor = if (selected) Color.White else Color(0xFF8C7AA8)
    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HuntCard(
    item: MyHuntCard,
    onPrimaryActionClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162A)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF2C2037))
            ) {
                if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.imageUrl,
                        contentDescription = "${item.hunt.name} cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.22f))
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.participants != null) {
                        Surface(
                            color = Color(0xFF2F1E40),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${item.participants} participants",
                                color = Color(0xFFB44BF3),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    StatusPill(item.status.label)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.hunt.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFB44BF3),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = item.rating,
                            color = Color(0xFFB44BF3),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Text(
                text = item.dateText,
                color = Color(0xFF8C7AA8),
                style = MaterialTheme.typography.bodySmall
            )

            if (item.ctaText != null) {
                Button(
                    onClick = onPrimaryActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB44BF3),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(item.ctaText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162A)),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("No hunts yet.", color = Color.White)
            Text(
                "Create or join a hunt to see it here.",
                color = Color(0xFF8C7AA8),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    Surface(
        color = Color(0xFF2F1E40),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFFB44BF3),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
