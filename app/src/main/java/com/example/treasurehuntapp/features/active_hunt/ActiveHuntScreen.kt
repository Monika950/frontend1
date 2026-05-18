package com.example.treasurehuntapp.features.active_hunt

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.treasurehuntapp.ui.AppTopBar

@Composable
fun ActiveHuntScreen(
    onCreateLocationClick: () -> Unit,
    onEditHuntClick: () -> Unit,
    onEditLocationClick: (String) -> Unit,
    onBack: () -> Unit,
    vm: ActiveHuntViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        vm.onLocationPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val granted = fineGranted || coarseGranted
        hasLocationPermission = granted
        if (granted) {
            vm.onLocationPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        vm.load()
    }

    Scaffold(
        topBar = {
            if (state.isOwner) {
                OwnerActiveHuntTopBar(
                    title = "Track Participants",
                    onBack = onBack
                )
            } else {
                AppTopBar(
                    title = state.hunt?.name ?: "Active Hunt",
                    onBack = onBack
                )
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (state.isOwner) {
            OwnerActiveHuntScreen(
                state = state,
                onCreateLocationClick = onCreateLocationClick,
                onEditHuntClick = onEditHuntClick,
                onEditLocationClick = onEditLocationClick,
                onDeleteLocationClick = { vm.deleteLocation(it) },
                onRemoveParticipantClick = { vm.removeParticipant(it) },
                onChangeParticipantRoleClick = { userId, role -> vm.updateParticipantRole(userId, role) },
                onLoadParticipantProgressClick = { vm.loadParticipantProgressForOwner(it) },
                onDismissFeedback = { vm.clearFeedback() },
                modifier = Modifier.padding(padding)
            )
        } else {
            ParticipantActiveHuntScreen(
                state = state,
                hasLocationPermission = hasLocationPermission,
                onSubmitAnswer = { locationId, answer, onResult ->
                    vm.submitAnswerForLocation(locationId, answer, onResult)
                },
                onDismissMissionAccomplished = { vm.dismissMissionAccomplished() },
                onAbandonHunt = { vm.abandonHunt(onBack) },
                onBackToHome = onBack,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
