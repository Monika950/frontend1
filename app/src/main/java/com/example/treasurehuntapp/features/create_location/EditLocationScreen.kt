package com.example.treasurehuntapp.features.create_location

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import com.example.treasurehuntapp.ui.map.AubergineMapStyle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun EditLocationScreen(
    onLocationUpdated: () -> Unit,
    onBack: () -> Unit,
    vm: EditLocationViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val scroll = rememberScrollState()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.onImagePicked(it.toString()) }
    }

    val selectedLat = state.lat.toDoubleOrNull() ?: 42.1234
    val selectedLng = state.lng.toDoubleOrNull() ?: 23.4567
    val selectedPoint = remember(selectedLat, selectedLng) { LatLng(selectedLat, selectedLng) }
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedPoint, 15f)
    }
    val bg = Brush.verticalGradient(
        listOf(Color(0xFF120A1C), Color(0xFF1E0F2B), Color(0xFF2A1336))
    )

    LaunchedEffect(selectedLat, selectedLng) {
        cameraState.position = CameraPosition.fromLatLngZoom(
            LatLng(selectedLat, selectedLng),
            cameraState.position.zoom
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .appPageHeaderPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Edit Location",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scroll)
        ) {
            if (state.isLoading) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFFB44BF3))
                }
            } else {
                Spacer(Modifier.height(8.dp))

                StyledEditLocationField(
                    value = state.name,
                    onValueChange = vm::onNameChange,
                    label = "Name"
                )

                Spacer(Modifier.height(12.dp))

                SectionLabel("Location Image")
                Spacer(Modifier.height(6.dp))
                Surface(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                    tonalElevation = 0.dp,
                    color = Color(0xFF1D1426),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.imageUri.isNotBlank()) {
                            AsyncImage(
                                model = state.imageUri,
                                contentDescription = "Location image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.height(8.dp))
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF3A2350)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "+",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color(0xFFB44BF3)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(state.imageLabel, color = Color(0xFFBFA8D6))
                    }
                }
                Spacer(Modifier.height(16.dp))

                StyledEditLocationField(
                    value = state.question,
                    onValueChange = vm::onQuestionChange,
                    label = "Question"
                )

                Spacer(Modifier.height(12.dp))

                StyledEditLocationField(
                    value = state.correctAnswer,
                    onValueChange = vm::onCorrectAnswerChange,
                    label = "Correct answer"
                )

                Spacer(Modifier.height(12.dp))

                StyledEditLocationField(
                    value = state.hint,
                    onValueChange = vm::onHintChange,
                    label = "Hint (optional)"
                )

                Spacer(Modifier.height(16.dp))

                SectionLabel("Pick On Map")
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    tonalElevation = 0.dp,
                    color = Color(0xFF1D1426),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GoogleMap(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            cameraPositionState = cameraState,
                            properties = MapProperties(
                                isMyLocationEnabled = false,
                                mapStyleOptions = AubergineMapStyle
                            ),
                            onMapClick = { latLng ->
                                vm.onMapPicked(latLng.latitude, latLng.longitude)
                            }
                        ) {
                            Marker(
                                state = MarkerState(position = LatLng(selectedLat, selectedLng)),
                                title = state.name.ifBlank { "Selected location" }
                            )
                        }
                        Text(
                            text = "Tap map to set coordinates",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8C7AA8)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) {
                        StyledEditLocationField(
                            value = state.lat,
                            onValueChange = vm::onLatChange,
                            label = "Lat"
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f)) {
                        StyledEditLocationField(
                            value = state.lng,
                            onValueChange = vm::onLngChange,
                            label = "Lng"
                        )
                    }
                }

                if (state.error != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = state.error!!,
                        color = Color(0xFFFF6E8A)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { vm.save(onLocationUpdated) },
                    enabled = !state.saving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB44BF3),
                        contentColor = Color.White
                    )
                ) {
                    if (state.saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Update Location", fontWeight = FontWeight.Bold)
                    }
                }

                if (state.allowSaveWithoutImage) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { vm.saveWithoutImage(onLocationUpdated) },
                        enabled = !state.saving,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFD0BCFF)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4D257B))
                    ) {
                        Text("Save without new image")
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFD0BCFF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4D257B))
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0xFF8E63FF),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StyledEditLocationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1B1125),
            unfocusedContainerColor = Color(0xFF1B1125),
            focusedBorderColor = Color(0xFF8E63FF),
            unfocusedBorderColor = Color(0xFF3A2B4C),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFFBFA8D6),
            unfocusedLabelColor = Color(0xFF8C7AA8),
            cursorColor = Color(0xFF8E63FF)
        )
    )
}
