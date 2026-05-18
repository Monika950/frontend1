package com.example.treasurehuntapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.treasurehuntapp.features.active_hunt.ActiveHuntScreen
import com.example.treasurehuntapp.features.create_hunt.CreateHuntScreen
import com.example.treasurehuntapp.features.create_hunt.EditHuntScreen
import com.example.treasurehuntapp.features.create_location.CreateLocationScreen
import com.example.treasurehuntapp.features.create_location.EditLocationScreen
import com.example.treasurehuntapp.features.forgot_password.ForgotPasswordScreen
import com.example.treasurehuntapp.features.home.HomeScreen
import com.example.treasurehuntapp.features.my_hunts.MyHuntsScreen
import com.example.treasurehuntapp.features.login.LoginScreen
import com.example.treasurehuntapp.features.notifications.NotificationsScreen
import com.example.treasurehuntapp.features.profile.ProfileScreen
import com.example.treasurehuntapp.features.profile.EditProfileScreen
import com.example.treasurehuntapp.features.register.RegisterScreen
import com.example.treasurehuntapp.features.reset_password.ResetPasswordScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Destinations.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Destinations.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destinations.Home.route) {
                        popUpTo(Destinations.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Destinations.Register.route)
                },
                onForgotPasswordClick = {
                    navController.navigate(Destinations.ForgotPassword.route)
                }
            )
        }

        composable(Destinations.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.ResetPassword.route,
            arguments = listOf(
                navArgument("token") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "treasurehuntapp://auth/reset-password?token={token}" },
                navDeepLink { uriPattern = "http://com.company.app:3513/auth/reset-password?token={token}" },
                navDeepLink { uriPattern = "https://com.company.app:3513/auth/reset-password?token={token}" },
                navDeepLink { uriPattern = "http://{host}/auth/reset-password?token={token}" },
                navDeepLink { uriPattern = "https://{host}/auth/reset-password?token={token}" }
            )
        ) { backStackEntry ->
            ResetPasswordScreen(
                token = backStackEntry.arguments?.getString("token"),
                onBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate(Destinations.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    }
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

        composable(Destinations.Home.route) {
            HomeScreen(
                onCreateHuntClick = { navController.navigate(Destinations.CreateHunt.route) },
                onActiveHuntClick = { navController.navigate(Destinations.ActiveHunt.route) },
                onHuntsClick = { isOwner ->
                    val tab = if (isOwner) "created" else "joined"
                    navController.navigate("my_hunts?tab=$tab")
                },
                onProfileClick = { navController.navigate(Destinations.Profile.route) },
                onNotificationsClick = { navController.navigate(Destinations.Notifications.route) }
            )
        }

        composable(Destinations.Profile.route) {
            ProfileScreen(
                onHomeClick = { navController.navigate(Destinations.Home.route) },
                onMyHuntsClick = { navController.navigate("my_hunts?tab=joined") },
                onNotificationsClick = { navController.navigate(Destinations.Notifications.route) },
                onEditProfileClick = { navController.navigate(Destinations.EditProfile.route) },
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Destinations.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.Notifications.route) {
            NotificationsScreen(
                onHomeClick = { navController.navigate(Destinations.Home.route) },
                onMyHuntsClick = { navController.navigate("my_hunts?tab=joined") },
                onProfileClick = { navController.navigate(Destinations.Profile.route) }
            )
        }

        composable(
            route = Destinations.MyHunts.route,
            arguments = listOf(
                navArgument("tab") { type = NavType.StringType; defaultValue = "created" }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: "created"
            MyHuntsScreen(
                initialTabIsCreated = tab.equals("created", ignoreCase = true),
                onAddClick = { navController.navigate(Destinations.CreateHunt.route) },
                onOpenHunt = { navController.navigate(Destinations.ActiveHunt.route) },
                onHomeClick = { navController.navigate(Destinations.Home.route) },
                onNotificationsClick = { navController.navigate(Destinations.Notifications.route) },
                onProfileClick = { navController.navigate(Destinations.Profile.route) }
            )
        }

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

        composable(Destinations.EditHunt.route) {
            EditHuntScreen(
                onHuntUpdated = { navController.popBackStack() },
                onHuntDeleted = {
                    navController.navigate("my_hunts?tab=created") {
                        popUpTo(Destinations.ActiveHunt.route) { inclusive = true }
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
                onEditHuntClick = {
                    navController.navigate(Destinations.EditHunt.route)
                },
                onEditLocationClick = { locationId ->
                    navController.navigate(Destinations.EditLocation.route(locationId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.CreateLocation.route) {
            CreateLocationScreen(
                onLocationCreated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.EditLocation.route,
            arguments = listOf(
                navArgument("locationId") { type = NavType.StringType }
            )
        ) {
            EditLocationScreen(
                onLocationUpdated = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
