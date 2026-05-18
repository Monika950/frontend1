package com.example.treasurehuntapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.treasurehuntapp.ui.theme.AppTheme
import com.example.treasurehuntapp.navigation.AppNavGraph
import com.example.treasurehuntapp.navigation.Destinations
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val vm: MainViewModel = hiltViewModel()
                val state by vm.state.collectAsState()
                val accessToken by vm.accessToken.collectAsState()
                val navController = rememberNavController()

                if (state.resolving) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Restoring session…",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LaunchedEffect(accessToken) {
                        if (accessToken.isNullOrBlank() &&
                            navController.currentDestination?.route != Destinations.Login.route
                        ) {
                            navController.navigate(Destinations.Login.route) {
                                popUpTo(Destinations.Login.route) { inclusive = true }
                            }
                        }
                    }

                    AppNavGraph(
                        navController = navController,
                        startDestination = state.startDestination
                    )
                }
            }
        }
    }
}
