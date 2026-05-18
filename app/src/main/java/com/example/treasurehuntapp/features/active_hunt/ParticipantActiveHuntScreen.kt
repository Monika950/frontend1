package com.example.treasurehuntapp.features.active_hunt

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.treasurehuntapp.ui.map.AubergineMapStyle

@Composable
fun ParticipantActiveHuntScreen(
    state: ActiveHuntState,
    hasLocationPermission: Boolean,
    onSubmitAnswer: (locationId: String, answer: String, onResult: (Boolean) -> Unit) -> Unit,
    onDismissMissionAccomplished: () -> Unit,
    onAbandonHunt: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF120A1C), Color(0xFF1E0F2B), Color(0xFF2A1336))
    )
    val hunt = state.hunt
    val locations = state.locations
    val totalLocations = locations.size
    val target = remember(locations, state.currentLocationId, state.completedLocationIds) {
        when {
            locations.isEmpty() -> null
            !state.currentLocationId.isNullOrBlank() ->
                locations.firstOrNull { it.id == state.currentLocationId }
                    ?: locations.firstOrNull { it.id !in state.completedLocationIds }
            state.completedLocationIds.isNotEmpty() ->
                locations.firstOrNull { it.id !in state.completedLocationIds }
            else -> locations.firstOrNull()
        }
    }
    val myLat = state.myLat
    val myLng = state.myLng

    val distanceMeters = if (myLat != null && myLng != null && target != null) {
        haversineMeters(myLat, myLng, target.coordinates.lat, target.coordinates.lng)
    } else {
        null
    }
    val arrived = distanceMeters != null && distanceMeters <= 50.0
    var showAnswerDialog by remember(target?.id) { mutableStateOf(false) }
    var answerInput by remember(target?.id) { mutableStateOf("") }
    var showHint by remember(target?.id) { mutableStateOf(false) }
    var showAbandonDialog by remember { mutableStateOf(false) }

    val center = remember(myLat, myLng, target) {
        when {
            myLat != null && myLng != null && target != null -> {
                LatLng(
                    (myLat + target.coordinates.lat) / 2.0,
                    (myLng + target.coordinates.lng) / 2.0
                )
            }
            myLat != null && myLng != null -> LatLng(myLat, myLng)
            target != null -> LatLng(target.coordinates.lat, target.coordinates.lng)
            else -> LatLng(0.0, 0.0)
        }
    }
    val zoom = remember(myLat, myLng, target) {
        when {
            target == null -> 1.5f
            myLat != null && myLng != null && target != null -> 12.5f
            myLat == null || myLng == null -> 13f
            else -> 14.5f
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    LaunchedEffect(center, zoom) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(center, zoom)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.showMissionAccomplished) {
            MissionAccomplishedScreen(
                questionsAnswered = totalLocations,
                onReturnHome = {
                    onDismissMissionAccomplished()
                    onBackToHome()
                }
            )
            return
        }

        if (state.isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFB44BF3))
            }
            return
        }

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        if (hunt == null) return

        HuntInfoCard(
            hunt = hunt,
            participantsCount = state.participantsCount,
            locationsCount = totalLocations
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = { showAbandonDialog = true }) {
                Text("Abandon Hunt", color = Color(0xFFFFA6C5))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(390.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = hasLocationPermission,
                    mapStyleOptions = AubergineMapStyle
                )
            ) {
                if (myLat != null && myLng != null) {
                    Marker(
                        state = MarkerState(position = LatLng(myLat, myLng)),
                        title = "You"
                    )
                }
                target?.let {
                    Marker(
                        state = MarkerState(position = LatLng(it.coordinates.lat, it.coordinates.lng)),
                        title = it.name
                    )
                }
            }

            Surface(
                color = Color(0xFF2F1E40),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Text(
                    text = "LIVE HUNT",
                    color = Color(0xFFB44BF3),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (distanceMeters != null) {
                Surface(
                    color = Color(0xFF1F1628),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "DISTANCE\n${distanceMeters.toInt()} m",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Surface(
            color = Color(0xFF20162A),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TARGET REACHED",
                        color = Color(0xFF8C7AA8),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Surface(
                        color = if (arrived) Color(0xFF163A2E) else Color(0xFF2F1E40),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (arrived) "ARRIVED" else "EN ROUTE",
                            color = if (arrived) Color(0xFF4ADE80) else Color(0xFFB44BF3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                Text(
                    text = target?.name ?: "Next location",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showAnswerDialog = true },
                    enabled = arrived && target != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C4DFF),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF3A2C4A),
                        disabledContentColor = Color(0xFF8C7AA8)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ANSWER TASK", fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = Color(0xFF1A1323),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (myLat != null && myLng != null) {
                                "Your location: ${"%.5f".format(myLat)}, ${"%.5f".format(myLng)}"
                            } else {
                                "Your location: waiting for GPS..."
                            },
                            color = Color(0xFFBFA8D6),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = when {
                                target == null -> "Distance to target: no target location"
                                distanceMeters == null -> "Distance to target: waiting for your location..."
                                else -> "Distance to target: ${distanceMeters.toInt()} m"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
    }

    if (showAnswerDialog && target != null) {
        AnswerTaskDialog(
            questionIndex = currentQuestionIndex(locations, target),
            totalQuestions = totalLocations.coerceAtLeast(1),
            question = target.question,
            hint = target.hint,
            answer = answerInput,
            onAnswerChange = { answerInput = it },
            showHint = showHint,
            onToggleHint = { showHint = !showHint },
            isSubmitting = state.isSubmittingAnswer,
            submitError = state.answerError,
            onDismiss = {
                if (!state.isSubmittingAnswer) showAnswerDialog = false
            },
            onSubmit = {
                onSubmitAnswer(target.id, answerInput) { isLastLocation ->
                    showAnswerDialog = false
                    if (!isLastLocation) {
                        answerInput = ""
                        showHint = false
                    }
                }
            }
        )
    }

    if (showAbandonDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAbandonDialog = false },
            containerColor = Color(0xFF1B1125),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0A8C6),
            title = { Text("Abandon hunt?") },
            text = { Text("Your current progress for this hunt will be abandoned.") },
            confirmButton = {
                Button(
                    onClick = {
                        showAbandonDialog = false
                        onAbandonHunt()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D1B2A),
                        contentColor = Color(0xFFFFA6C5)
                    )
                ) { Text("Abandon") }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AnswerTaskDialog(
    questionIndex: Int,
    totalQuestions: Int,
    question: String,
    hint: String?,
    answer: String,
    onAnswerChange: (String) -> Unit,
    showHint: Boolean,
    onToggleHint: () -> Unit,
    isSubmitting: Boolean,
    submitError: String?,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF140B1E),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUESTION $questionIndex",
                        color = Color(0xFFB44BF3),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(totalQuestions.coerceAtMost(4)) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (idx == (questionIndex - 1).coerceAtLeast(0)) Color(0xFFB44BF3)
                                        else Color(0xFF4A3A57)
                                    )
                            )
                        }
                    }
                }

                Text(
                    text = question,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = answer,
                    onValueChange = onAnswerChange,
                    placeholder = { Text("Type your answer here...", color = Color(0xFF8C7AA8)) },
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (submitError.isNullOrBlank()) Color(0xFFB44BF3) else Color(0xFFFF4D6D),
                        unfocusedBorderColor = if (submitError.isNullOrBlank()) Color(0xFF4A3A57) else Color(0xFFFF4D6D),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFB44BF3)
                    )
                )

                if (!submitError.isNullOrBlank()) {
                    Text(
                        text = submitError,
                        color = Color(0xFFFF4D6D),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Surface(
                    color = Color(0xFF1A1323),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onToggleHint,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showHint) "Hide hint" else "Get a hint to help you out",
                            color = Color(0xFFB44BF3)
                        )
                    }
                }

                if (showHint && !hint.isNullOrBlank()) {
                    Surface(
                        color = Color(0xFF1F1628),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = hint,
                            color = Color(0xFFBFA8D6),
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                        Text("Cancel", color = Color(0xFFBFA8D6))
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onSubmit,
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C4DFF),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Submit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionAccomplishedScreen(
    questionsAnswered: Int,
    onReturnHome: () -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF1A0C26), Color(0xFF2A0F3A), Color(0xFF35124A))
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF4C1D95)),
            contentAlignment = Alignment.Center
        ) {
            Text("?", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Mission\nAccomplished!",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "You've successfully completed the hunt.",
            color = Color(0xFFBFA8D6),
            style = MaterialTheme.typography.bodyLarge
        )

        Surface(
            color = Color(0x332B2930),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL TIME", color = Color(0xFF8C7AA8), style = MaterialTheme.typography.labelSmall)
                    Text("--:--", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("QUESTIONS ANSWERED", color = Color(0xFF8C7AA8), style = MaterialTheme.typography.labelSmall)
                    Text("$questionsAnswered/$questionsAnswered", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Button(
            onClick = onReturnHome,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFB44BF3),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Home", fontWeight = FontWeight.Bold)
        }
    }
}

private fun currentQuestionIndex(
    locations: List<com.example.treasurehuntapp.data.source.remote.dto.locations.LocationDto>,
    target: com.example.treasurehuntapp.data.source.remote.dto.locations.LocationDto?
): Int {
    if (target == null) return 1
    val idx = locations.indexOfFirst { it.id == target.id }
    return if (idx >= 0) idx + 1 else 1
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}
