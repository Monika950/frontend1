package com.example.treasurehuntapp.features.create_hunt

import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHuntScreen(
    onHuntCreated: () -> Unit,
    onBack: () -> Unit,
    vm: CreateHuntViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val is24Hour = DateFormat.is24HourFormat(context)

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    var startDateMillis by remember { mutableStateOf<Long?>(null) }
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }

    var endDateMillis by remember { mutableStateOf<Long?>(null) }
    var endHour by remember { mutableStateOf(11) }
    var endMinute by remember { mutableStateOf(0) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { vm.onImageChange(it.toString()) }
    }

    fun buildIsoUtc(dateMillis: Long, hour: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(cal.timeInMillis))
    }

    fun formatDateLocal(millis: Long?): String {
        if (millis == null) return "Pick date"
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatTimeLocal(hour: Int, minute: Int): String {
        return if (is24Hour) {
            String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        } else {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(cal.time)
        }
    }

    fun updateStartIso() {
        val millis = startDateMillis ?: return
        vm.onStartChange(buildIsoUtc(millis, startHour, startMinute))
    }

    fun updateEndIso() {
        val millis = endDateMillis ?: return
        vm.onEndChange(buildIsoUtc(millis, endHour, endMinute))
    }

    val bg = Brush.verticalGradient(
        listOf(Color(0xFF120A1C), Color(0xFF1E0F2B), Color(0xFF2A1336))
    )

    Scaffold(
        topBar = {
            CreateHuntTopBar(onBack = onBack)
        },
        bottomBar = {
            CreateHuntBottomBar(
                onSave = { vm.create(onHuntCreated) },
                isSaving = state.isLoading
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            UploadCoverCard(
                imageUri = state.image,
                onClick = { imagePickerLauncher.launch("image/*") }
            )

            SectionLabel("Hunt Details")

            StyledTextField(
                value = state.name,
                onValueChange = vm::onNameChange,
                placeholder = "Hunt Name (e.g. Purple City Secrets)",
                singleLine = true
            )

            StyledTextField(
                value = state.description,
                onValueChange = vm::onDescriptionChange,
                placeholder = "Tell participants what to expect...",
                minLines = 3
            )

            SectionLabel("Schedule")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateTimePill(
                    title = "Starts",
                    icon = Icons.Rounded.CalendarMonth,
                    label = "${formatDateLocal(startDateMillis)} ${formatTimeLocal(startHour, startMinute)}",
                    onClick = { showStartDatePicker = true }
                )
                DateTimePill(
                    title = "Ends",
                    icon = Icons.Rounded.Schedule,
                    label = "${formatDateLocal(endDateMillis)} ${formatTimeLocal(endHour, endMinute)}",
                    onClick = { showEndDatePicker = true }
                )
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showStartDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startDateMillis = dateState.selectedDateMillis
                        updateStartIso()
                        showStartDatePicker = false
                        if (startDateMillis != null) showStartTimePicker = true
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showEndDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = endDateMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endDateMillis = dateState.selectedDateMillis
                        updateEndIso()
                        showEndDatePicker = false
                        if (endDateMillis != null) showEndTimePicker = true
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showStartTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = startHour,
            initialMinute = startMinute,
            is24Hour = is24Hour
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        startHour = timeState.hour
                        startMinute = timeState.minute
                        updateStartIso()
                        showStartTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showEndTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = endHour,
            initialMinute = endMinute,
            is24Hour = is24Hour
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endHour = timeState.hour
                        endMinute = timeState.minute
                        updateEndIso()
                        showEndTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") }
            },
            text = { TimePicker(state = timeState) }
        )
    }
}

@Composable
private fun CreateHuntTopBar(
    onBack: () -> Unit
) {
    Surface(color = Color.Transparent) {
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
                text = "New Hunt",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun CreateHuntBottomBar(
    onSave: () -> Unit,
    isSaving: Boolean
) {
    Surface(
        color = Color(0xFF120A1C).copy(alpha = 0.96f),
        modifier = Modifier.navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB44BF3),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Save Hunt", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun UploadCoverCard(
    imageUri: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1426)),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (imageUri.isNotBlank()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Selected cover image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(horizontal = 14.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A2350)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFFB44BF3)
                    )
                }
            }
            Text(
                text = if (imageUri.isBlank()) "Upload Cover Image" else "Change Cover Image",
                color = Color(0xFFB44BF3),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (imageUri.isBlank()) "PNG or JPG, max 5MB" else "Tap to choose another image",
                color = Color(0xFF9A8CB4),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        color = Color(0xFF8C7AA8),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, color = Color(0xFF8C7AA8))
        },
        singleLine = singleLine,
        minLines = minLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFB44BF3),
            unfocusedBorderColor = Color(0xFF3A2C4A),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFFB44BF3)
        )
    )
}

@Composable
private fun RowScope.DateTimePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title.uppercase(Locale.getDefault()),
            color = Color(0xFF8C7AA8),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF22172B))
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFFB44BF3))
                Text(label, color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
