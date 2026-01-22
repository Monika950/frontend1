package com.example.treasurehuntapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.treasurehuntapp.features.active_hunt.ActiveHuntScreen
import com.example.treasurehuntapp.features.create_hunt.CreateHuntScreen
import com.example.treasurehuntapp.features.create_location.CreateLocationScreen
import com.example.treasurehuntapp.features.home.HomeScreen
import com.example.treasurehuntapp.features.join_hunt.JoinHuntScreen
import com.example.treasurehuntapp.features.login.LoginScreen
import com.example.treasurehuntapp.features.notifications.NotificationsScreen
import com.example.treasurehuntapp.features.profile.ProfileScreen
import com.example.treasurehuntapp.features.register.RegisterScreen

@Composable
fun AppNavGraph(
    startDestination: String = Destinations.Login.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // AUTH
        composable(Destinations.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destinations.Home.route) {
                        popUpTo(Destinations.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Destinations.Register.route)
                }
            )
        }

        composable(Destinations.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Destinations.Home.route) {
                        popUpTo(Destinations.Register.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // MAIN
        composable(Destinations.Home.route) {
            HomeScreen(
                onCreateHuntClick = { navController.navigate(Destinations.CreateHunt.route) },
                onJoinHuntClick = { navController.navigate(Destinations.JoinHunt.route) },
                onActiveHuntClick = { navController.navigate(Destinations.ActiveHunt.route) },
                onProfileClick = { navController.navigate(Destinations.Profile.route) },
                onNotificationsClick = { navController.navigate(Destinations.Notifications.route) },
            )
        }

        composable(Destinations.Profile.route) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }

        composable(Destinations.Notifications.route) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        // HUNTS
        composable(Destinations.CreateHunt.route) {
            CreateHuntScreen(
                onHuntCreated = {
                    navController.navigate(Destinations.ActiveHunt.route) {
                        popUpTo(Destinations.CreateHunt.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.JoinHunt.route) {
            JoinHuntScreen(
                onJoinSuccess = {
                    navController.navigate(Destinations.ActiveHunt.route) {
                        popUpTo(Destinations.JoinHunt.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.ActiveHunt.route) {
            ActiveHuntScreen(
                onCreateLocationClick = {
                    navController.navigate(Destinations.CreateLocation.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        // LOCATIONS
        composable(Destinations.CreateLocation.route) {
            CreateLocationScreen(
                onLocationCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
