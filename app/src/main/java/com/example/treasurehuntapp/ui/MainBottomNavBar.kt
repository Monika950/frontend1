package com.example.treasurehuntapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.treasurehuntapp.ui.theme.AppColors

enum class MainNavTab {
    Home, MyHunts, Notifications, Profile
}

@Composable
fun MainBottomNavBar(
    selected: MainNavTab,
    onHomeClick: () -> Unit,
    onMyHuntsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    NavigationBar(
        containerColor = AppColors.BgTop,
        tonalElevation = 0.dp,
        windowInsets = NavigationBarDefaults.windowInsets,
        modifier = Modifier
    ) {
        NavigationBarItem(
            selected = selected == MainNavTab.Home,
            onClick = onHomeClick,
            icon = { Icon(Icons.Rounded.Home, contentDescription = "Home") },
            label = { NavLabel("HOME") },
            colors = mainNavColors()
        )
        NavigationBarItem(
            selected = selected == MainNavTab.MyHunts,
            onClick = onMyHuntsClick,
            icon = { Icon(Icons.Rounded.Map, contentDescription = "My Hunts") },
            label = { NavLabel("MY HUNTS") },
            colors = mainNavColors()
        )
        NavigationBarItem(
            selected = selected == MainNavTab.Notifications,
            onClick = onNotificationsClick,
            icon = { Icon(Icons.Rounded.NotificationsActive, contentDescription = "Notifications") },
            label = { NavLabel("NOTIFICATIONS") },
            colors = mainNavColors()
        )
        NavigationBarItem(
            selected = selected == MainNavTab.Profile,
            onClick = onProfileClick,
            icon = { Icon(Icons.Rounded.Person, contentDescription = "Profile") },
            label = { NavLabel("PROFILE") },
            colors = mainNavColors()
        )
    }
}

@Composable
private fun NavLabel(text: String) {
    Text(
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun mainNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = AppColors.PurpleAccent,
    selectedTextColor = AppColors.PurpleAccent,
    indicatorColor = AppColors.SurfaceChip,
    unselectedIconColor = AppColors.TextSecondary,
    unselectedTextColor = AppColors.TextSecondary
)
