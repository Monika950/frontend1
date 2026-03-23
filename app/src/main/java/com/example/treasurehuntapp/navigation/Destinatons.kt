package com.example.treasurehuntapp.navigation

sealed class Destinations(val route: String) {

    data object Login : Destinations("login")
    data object Register : Destinations("register")
    data object ForgotPassword : Destinations("forgot_password")
    data object ResetPassword : Destinations("reset_password?token={token}") {
        fun route(token: String? = null): String =
            if (token.isNullOrBlank()) "reset_password" else "reset_password?token=$token"
    }

    data object Home : Destinations("home")
    data object Profile : Destinations("profile")
    data object EditProfile : Destinations("edit_profile")
    data object Notifications : Destinations("notifications")

    data object MyHunts : Destinations("my_hunts?tab={tab}")
    data object CreateHunt : Destinations("create_hunt")
    data object EditHunt : Destinations("edit_hunt")
    data object ActiveHunt : Destinations("active_hunt")

    data object CreateLocation : Destinations("create_location")
    data object EditLocation : Destinations("edit_location/{locationId}") {
        fun route(locationId: String) = "edit_location/$locationId"
    }
}
