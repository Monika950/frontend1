package com.example.treasurehuntapp.features.active_hunt

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import com.example.treasurehuntapp.data.source.remote.dto.hunt.ParticipantDto
import com.example.treasurehuntapp.data.source.remote.dto.locations.LocationDto
import com.example.treasurehuntapp.data.source.remote.dto.progress.UserProgressDto
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.treasurehuntapp.ui.map.AubergineMapStyle
import kotlinx.coroutines.delay

@Composable
fun OwnerActiveHuntTopBar(
    title: String,
    onBack: () -> Unit
) {
    Surface(color = Color(0xFF120A1C)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .appPageHeaderPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
fun OwnerActiveHuntScreen(
    state: ActiveHuntState,
    onCreateLocationClick: () -> Unit,
    onEditHuntClick: () -> Unit,
    onEditLocationClick: (String) -> Unit,
    onDeleteLocationClick: (String) -> Unit,
    onRemoveParticipantClick: (String) -> Unit,
    onChangeParticipantRoleClick: (String, String) -> Unit,
    onLoadParticipantProgressClick: (String) -> Unit,
    onDismissFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF120A1C), Color(0xFF1E0F2B), Color(0xFF2A1336))
    )
    val participants = state.participants.filter { !it.role.equals("owner", ignoreCase = true) }
    val totalLocations = state.locations.size
    var pendingDeleteLocation by remember { mutableStateOf<LocationDto?>(null) }
    var pendingRemoveParticipant by remember { mutableStateOf<ParticipantDto?>(null) }
    var pendingRoleParticipant by remember { mutableStateOf<ParticipantDto?>(null) }
    var trackedParticipantId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.successMessage, state.error) {
        if (!state.successMessage.isNullOrBlank() || !state.error.isNullOrBlank()) {
            delay(2200)
            onDismissFeedback()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        state.hunt?.let { hunt ->
            HuntInfoCard(
                hunt = hunt,
                participantsCount = participants.size,
                locationsCount = totalLocations
            )
        }

        state.successMessage?.let { message ->
            Surface(
                color = Color(0xFF163A2E),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = message,
                    color = Color(0xFFB8F7C3),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        state.error?.let { message ->
            Surface(
                color = Color(0xFF3D1B2A),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = message,
                    color = Color(0xFFFFA6C5),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        OwnerMapArea(
            locations = state.locations,
            participants = participants,
            livePositions = state.participantLivePositions,
            progressByUserId = state.participantProgressByUserId
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Participants",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = Color(0xFF2F1E40),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "LIVE PROGRESS",
                    color = Color(0xFFB44BF3),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (participants.isEmpty()) {
            EmptyParticipantsCard()
        } else {
            participants.forEach { participant ->
                val live = participantLivePositionFor(participant, state.participantLivePositions)
                val cachedProgress = participant.user?.id
                    ?.let { state.participantProgressByUserId[it] }
                val completedCount = cachedProgress?.completedLocations?.size ?: inferredCompletedCount(
                    currentLocationId = live?.currentLocationId,
                    locations = state.locations
                )
                ParticipantProgressCard(
                    name = participantDisplayName(participant),
                    progress = if (totalLocations > 0) {
                        (completedCount.toFloat() / totalLocations.toFloat()).coerceIn(0f, 1f)
                    } else 0f,
                    progressLabel = "$completedCount/$totalLocations LOCATIONS",
                    onTrackClick = {
                        val trackingId = participantTrackingPrimaryId(participant)
                        trackedParticipantId = trackingId
                        val userId = participant.user?.id?.takeIf { it.isNotBlank() }
                        if (!userId.isNullOrBlank()) {
                            onLoadParticipantProgressClick(userId)
                        }
                    },
                    onRemoveClick = { pendingRemoveParticipant = participant },
                    onRoleClick = { pendingRoleParticipant = participant }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hunt Locations",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = Color(0xFF2F1E40),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "$totalLocations Added",
                    color = Color(0xFFB44BF3),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (state.locations.isEmpty()) {
            EmptyLocationsCard()
        } else {
            state.locations.forEachIndexed { index, location ->
                OwnerLocationCard(
                    index = index + 1,
                    total = totalLocations,
                    location = location,
                    onEditClick = { onEditLocationClick(location.id) },
                    onDeleteClick = { pendingDeleteLocation = location }
                )
            }
        }

        Button(
            onClick = onCreateLocationClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB44BF3),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Location", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onEditHuntClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFD0BCFF)
            )
        ) {
            Text("Edit Hunt", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))
    }

    pendingDeleteLocation?.let { location ->
        AlertDialog(
            onDismissRequest = { pendingDeleteLocation = null },
            title = { Text("Delete location") },
            text = { Text("Delete \"${location.name}\" from this hunt?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteLocationClick(location.id)
                        pendingDeleteLocation = null
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteLocation = null }) { Text("Cancel") }
            }
        )
    }

    pendingRemoveParticipant?.let { participant ->
        val userId = participant.user?.id?.takeIf { it.isNotBlank() } ?: participant.id
        AlertDialog(
            onDismissRequest = { pendingRemoveParticipant = null },
            title = { Text("Remove participant") },
            text = { Text("Remove \"${participantDisplayName(participant)}\" from this hunt?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemoveParticipantClick(userId)
                        pendingRemoveParticipant = null
                    }
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingRemoveParticipant = null }) { Text("Cancel") }
            }
        )
    }

    pendingRoleParticipant?.let { participant ->
        val userId = participant.user?.id?.takeIf { it.isNotBlank() } ?: participant.id
        AlertDialog(
            onDismissRequest = { pendingRoleParticipant = null },
            title = { Text("Change role") },
            text = { Text("Change role for \"${participantDisplayName(participant)}\".") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        onChangeParticipantRoleClick(userId, "participant")
                        pendingRoleParticipant = null
                    }) { Text("Participant") }
                    TextButton(onClick = {
                        onChangeParticipantRoleClick(userId, "owner")
                        pendingRoleParticipant = null
                    }) { Text("Owner") }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRoleParticipant = null }) { Text("Cancel") }
            }
        )
    }

    trackedParticipantId?.let { participantId ->
        val participant = participants.firstOrNull { participantMatchesTrackingId(it, participantId) }
        val participantUserId = participant?.user?.id?.takeIf { it.isNotBlank() }
        ParticipantTrackingDialog(
            participantName = participant?.let(::participantDisplayName) ?: "Player",
            live = participant?.let { participantLivePositionFor(it, state.participantLivePositions) }
                ?: state.participantLivePositions[participantId],
            progress = participantUserId?.let { state.participantProgressByUserId[it] },
            isProgressLoading = state.participantProgressLoadingUserId == participantUserId,
            progressError = state.participantProgressError,
            locations = state.locations,
            onDismiss = { trackedParticipantId = null }
        )
    }

}

@Composable
private fun OwnerMapArea(
    locations: List<LocationDto>,
    participants: List<ParticipantDto>,
    livePositions: Map<String, ParticipantLivePosition>,
    progressByUserId: Map<String, UserProgressDto>
) {
    val context = LocalContext.current
    val participantPoints = participants.mapNotNull { participant ->
        val live = participantLivePositionFor(participant, livePositions)
        if (live != null) {
            participant to LatLng(live.lat, live.lng)
        } else {
            val userId = participant.user?.id?.takeIf { it.isNotBlank() }
            val coords = userId?.let { progressByUserId[it] }?.currentCoordinates
            coordinatesFromProgressMap(coords)?.let { latLng ->
                participant to latLng
            }
        }
    }
    val allPoints = locations.map { LatLng(it.coordinates.lat, it.coordinates.lng) } + participantPoints.map { it.second }

    val center = remember(locations, participantPoints) {
        if (allPoints.isNotEmpty()) {
            val avgLat = allPoints.map { it.latitude }.average()
            val avgLng = allPoints.map { it.longitude }.average()
            LatLng(avgLat, avgLng)
        } else {
            LatLng(0.0, 0.0)
        }
    }
    val zoom = remember(locations, participantPoints) {
        when {
            allPoints.isEmpty() -> 1.5f
            allPoints.size == 1 -> 14f
            else -> 11.5f
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    LaunchedEffect(locations, participantPoints) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = false,
            mapStyleOptions = AubergineMapStyle
        )
    ) {
        locations.forEach { location ->
            Marker(
                state = MarkerState(
                    position = LatLng(location.coordinates.lat, location.coordinates.lng)
                ),
                title = location.name
            )
        }
        participantPoints.forEach { (participant, latLng) ->
            val icon = rememberParticipantMarkerIcon(
                context = context,
                initials = initials(participantDisplayName(participant))
            )
            Marker(
                state = MarkerState(position = latLng),
                title = participantDisplayName(participant),
                snippet = "Participant position",
                icon = icon
            )
        }
    }
}

@Composable
private fun ParticipantProgressCard(
    name: String,
    progress: Float,
    progressLabel: String,
    onTrackClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onRoleClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    progressLabel,
                    color = Color(0xFF8C7AA8),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                color = Color(0xFFB44BF3),
                trackColor = Color(0xFF3A2C4A),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4ADE80))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Active now",
                        color = Color(0xFF8C7AA8),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onRoleClick,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD0BCFF)
                        )
                    ) {
                        Text(
                            "ROLE",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    OutlinedButton(
                        onClick = onRemoveClick,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFFA6C5)
                        )
                    ) {
                        Text(
                            "REMOVE",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Button(
                        onClick = onTrackClick,
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F1E40),
                            contentColor = Color(0xFFB44BF3)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            "TRACK",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyParticipantsCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("No participants yet.", color = Color.White)
            Text(
                "Invite players to start tracking.",
                color = Color(0xFF8C7AA8),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EmptyLocationsCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("No locations added yet.", color = Color.White)
            Text(
                "Use Add Location to build the hunt route.",
                color = Color(0xFF8C7AA8),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun OwnerLocationCard(
    index: Int,
    total: Int,
    location: LocationDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF20162A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = location.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Location $index / $total",
                        color = Color(0xFF8C7AA8),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF2A1C38))
                        .border(1.dp, Color(0xFF3A2C4A), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${"%.4f".format(location.coordinates.lat)}, ${"%.4f".format(location.coordinates.lng)}",
                        color = Color(0xFFBFA8D6),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Text(
                text = location.question,
                color = Color(0xFFE5DDF1),
                style = MaterialTheme.typography.bodySmall
            )

            if (!location.hint.isNullOrBlank()) {
                Text(
                    text = "Hint: ${location.hint}",
                    color = Color(0xFF8C7AA8),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 0.dp
                    )
                ) {
                    Text("Edit", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D1B2A),
                        contentColor = Color(0xFFFFA6C5)
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 0.dp
                    )
                ) {
                    Text(
                        "Delete",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ParticipantTrackingDialog(
    participantName: String,
    live: ParticipantLivePosition?,
    progress: UserProgressDto?,
    isProgressLoading: Boolean,
    progressError: String?,
    locations: List<LocationDto>,
    onDismiss: () -> Unit
) {
    val completedLocations = progress?.completedLocations
        ?.mapNotNull { completedId -> locations.firstOrNull { it.id == completedId } }
        ?: run {
            val currentIndex = live?.currentLocationId?.let { id ->
                locations.indexOfFirst { it.id == id }.takeIf { it != null && it >= 0 }
            }
            when {
                live == null -> emptyList()
                currentIndex == null -> emptyList()
                else -> locations.take(currentIndex)
            }
        }
    val progressCoordinates = coordinatesFromProgressMap(progress?.currentCoordinates)
    val effectiveLat = live?.lat ?: progressCoordinates?.latitude
    val effectiveLng = live?.lng ?: progressCoordinates?.longitude

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .heightIn(max = 560.dp),
            color = Color(0xFF1A1224),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = participantName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Participant Tracking",
                            color = Color(0xFF8C7AA8),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = Color(0xFFB44BF3))
                    }
                }

                Surface(
                    color = Color(0xFF241832),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Last Coordinates",
                            color = Color(0xFFB44BF3),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (effectiveLat != null && effectiveLng != null) {
                                "${String.format("%.5f", effectiveLat)}, ${String.format("%.5f", effectiveLng)}"
                            } else {
                                "No live coordinates received yet."
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (isProgressLoading) {
                    Text(
                        text = "Loading progress...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB44BF3)
                    )
                }
                if (!progressError.isNullOrBlank()) {
                    Surface(
                        color = Color(0x33FFA6C5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = progressError,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFA6C5),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF241832),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Completed Locations (${completedLocations.size})",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (completedLocations.isEmpty()) {
                            Text(
                                "No completed locations yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8C7AA8)
                            )
                        } else {
                            completedLocations.forEachIndexed { index, location ->
                                Surface(
                                    color = Color(0xFF2D1F3B),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}. ${location.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun participantDisplayName(participant: ParticipantDto): String {
    return participant.user?.username
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: participant.username
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: participant.user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: participant.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        ?: "Player"
}

private fun participantTrackingPrimaryId(participant: ParticipantDto): String {
    return participant.user?.id?.takeIf { it.isNotBlank() } ?: participant.id
}

private fun participantMatchesTrackingId(participant: ParticipantDto, trackingId: String): Boolean {
    if (trackingId.isBlank()) return false
    val userId = participant.user?.id
    return trackingId == participant.id || (!userId.isNullOrBlank() && trackingId == userId)
}

private fun participantLivePositionFor(
    participant: ParticipantDto,
    livePositions: Map<String, ParticipantLivePosition>
): ParticipantLivePosition? {
    val userId = participant.user?.id
    return when {
        !userId.isNullOrBlank() && livePositions.containsKey(userId) -> livePositions[userId]
        livePositions.containsKey(participant.id) -> livePositions[participant.id]
        else -> null
    }
}

private fun coordinatesFromProgressMap(raw: Map<String, Any?>?): LatLng? {
    if (raw.isNullOrEmpty()) return null
    val lat = (raw["lat"] ?: raw["latitude"] ?: raw["y"]).toDoubleOrNullSafe()
    val lng = (raw["lng"] ?: raw["lon"] ?: raw["longitude"] ?: raw["x"]).toDoubleOrNullSafe()
    return if (lat != null && lng != null) LatLng(lat, lng) else null
}

private fun Any?.toDoubleOrNullSafe(): Double? = when (this) {
    is Number -> this.toDouble()
    is String -> this.toDoubleOrNull()
    else -> null
}

private fun inferredCompletedCount(
    currentLocationId: String?,
    locations: List<LocationDto>
): Int {
    if (currentLocationId == null) return 0
    val index = locations.indexOfFirst { it.id == currentLocationId }
    return if (index >= 0) index else 0
}

private fun initials(fullName: String): String {
    val parts = fullName
        .split(" ", ".", "_", "-")
        .map { it.trim() }
        .filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "P"
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

@Composable
private fun rememberParticipantMarkerIcon(
    context: android.content.Context,
    initials: String
): BitmapDescriptor {
    return remember(initials) {
        val density = context.resources.displayMetrics.density
        val sizePx = (40f * density).toInt().coerceAtLeast(40)
        val bitmap: Bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFFB44BF3).toArgb()
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color(0xFF1E0F2B).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 2f * density
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.White.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val radius = (sizePx / 2f) - strokePaint.strokeWidth
        canvas.drawCircle(cx, cy, radius, fillPaint)
        canvas.drawCircle(cx, cy, radius, strokePaint)
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(initials.take(2), cx, textY, textPaint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
