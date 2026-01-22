package com.example.treasurehuntapp.navigation

sealed class Destinations(val route: String) {

    // Auth
    data object Login : Destinations("login")
    data object Register : Destinations("register")

    // Main
    data object Home : Destinations("home")
    data object Profile : Destinations("profile")
    data object Notifications : Destinations("notifications")

    // Hunts
    data object CreateHunt : Destinations("create_hunt")
    data object JoinHunt : Destinations("join_hunt")
    data object ActiveHunt : Destinations("active_hunt")

    // Locations
    data object CreateLocation : Destinations("create_location")
}
