package com.example.treasurehuntapp.features.create_location

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import com.example.treasurehuntapp.ui.theme.AppGradients
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.LaunchedEffect
import com.example.treasurehuntapp.ui.map.AubergineMapStyle

@Composable
fun CreateLocationScreen(
    onLocationCreated: () -> Unit,
    onBack: () -> Unit,
    vm: CreateLocationViewModel = hiltViewModel()
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

    LaunchedEffect(selectedLat, selectedLng) {
        cameraState.position = CameraPosition.fromLatLngZoom(LatLng(selectedLat, selectedLng), cameraState.position.zoom)
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
                    "Create Location",
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
                .background(AppGradients.ScreenBackground)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scroll)
        ) {

        OutlinedTextField(
            value = state.name,
            onValueChange = vm::onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Text("Location image", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Surface(
            onClick = { imagePickerLauncher.launch("image/*") },
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = "Location image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(8.dp))
                } else {
                    Text("+", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))
                }
                Text(state.imageLabel)
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.question,
            onValueChange = vm::onQuestionChange,
            label = { Text("Question") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.correctAnswer,
            onValueChange = vm::onCorrectAnswerChange,
            label = { Text("Correct answer") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = state.hint,
            onValueChange = vm::onHintChange,
            label = { Text("Hint (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text("Pick on Map", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(6.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
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
                        .height(220.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.lat,
                onValueChange = vm::onLatChange,
                label = { Text("Lat") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = state.lng,
                onValueChange = vm::onLngChange,
                label = { Text("Lng") },
                modifier = Modifier.weight(1f)
            )
        }

        if (state.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { vm.save(onLocationCreated) },
            enabled = !state.saving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.saving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Text("Save")
            }
        }

        if (state.allowSaveWithoutImage) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { vm.saveWithoutImage(onLocationCreated) },
                enabled = !state.saving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save without image")
            }
        }

        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Text("Cancel")
        }
        }
    }
}
