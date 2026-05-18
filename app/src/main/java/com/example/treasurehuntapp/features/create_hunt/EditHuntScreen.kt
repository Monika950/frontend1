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
import androidx.compose.runtime.LaunchedEffect
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
import coil.compose.AsyncImage
import com.example.treasurehuntapp.ui.appPageHeaderPadding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHuntScreen(
    onHuntUpdated: () -> Unit,
    onHuntDeleted: () -> Unit,
    onBack: () -> Unit,
    vm: EditHuntViewModel = hiltViewModel()
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
    var syncedStartIso by remember { mutableStateOf<String?>(null) }
    var syncedEndIso by remember { mutableStateOf<String?>(null) }
    var showAddOwnerDialog by remember { mutableStateOf(false) }
    var showDeleteHuntDialog by remember { mutableStateOf(false) }
    var ownerUserIdInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.load() }

    LaunchedEffect(state.startIso) {
        val iso = state.startIso
        if (iso.isBlank() || iso == syncedStartIso) return@LaunchedEffect
        parseIsoToLocalParts(iso)?.let { (millis, hour, minute) ->
            startDateMillis = millis
            startHour = hour
            startMinute = minute
            syncedStartIso = iso
        }
    }
    LaunchedEffect(state.endIso) {
        val iso = state.endIso
        if (iso.isBlank() || iso == syncedEndIso) return@LaunchedEffect
        parseIsoToLocalParts(iso)?.let { (millis, hour, minute) ->
            endDateMillis = millis
            endHour = hour
            endMinute = minute
            syncedEndIso = iso
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.onImageChange(it.toString()) }
    }

    fun buildIsoUtc(dateMillis: Long, hour: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(cal.timeInMillis))
    }
    fun updateStartIso() { startDateMillis?.let { vm.onStartChange(buildIsoUtc(it, startHour, startMinute)) } }
    fun updateEndIso() { endDateMillis?.let { vm.onEndChange(buildIsoUtc(it, endHour, endMinute)) } }

    val bg = Brush.verticalGradient(listOf(Color(0xFF120A1C), Color(0xFF1E0F2B), Color(0xFF2A1336)))

    Scaffold(
        topBar = { EditHuntTopBar(onBack = onBack) },
        bottomBar = {
            EditHuntBottomBar(
                onSave = { vm.update(onHuntUpdated) },
                enabled = !state.isLoading && !state.isSaving,
                isSaving = state.isSaving
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFB44BF3))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bg)
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EditUploadCoverCard(
                    imageUri = state.image,
                    onClick = { imagePickerLauncher.launch("image/*") }
                )

                EditSectionLabel("Hunt Details")
                EditStyledTextField(
                    value = state.name,
                    onValueChange = vm::onNameChange,
                    placeholder = "Hunt Name"
                )
                EditStyledTextField(
                    value = state.description,
                    onValueChange = vm::onDescriptionChange,
                    placeholder = "Tell participants what to expect...",
                    minLines = 3
                )

                EditSectionLabel("Schedule")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EditDateTimePill(
                        title = "Starts",
                        icon = Icons.Rounded.CalendarMonth,
                        label = formatDateTimeLabel(startDateMillis, startHour, startMinute, is24Hour),
                        onClick = { showStartDatePicker = true }
                    )
                    EditDateTimePill(
                        title = "Ends",
                        icon = Icons.Rounded.Schedule,
                        label = formatDateTimeLabel(endDateMillis, endHour, endMinute, is24Hour),
                        onClick = { showEndDatePicker = true }
                    )
                }

                state.successMessage?.let {
                    Surface(
                        color = Color(0xFF163A2E),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = it,
                            color = Color(0xFFB8F7C3),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                EditSectionLabel("Owner & Hunt")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            vm.clearFeedback()
                            showAddOwnerDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2F1E40),
                            contentColor = Color(0xFFD0BCFF)
                        )
                    ) {
                        Text("Add Owner", fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            vm.clearFeedback()
                            showDeleteHuntDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3D1B2A),
                            contentColor = Color(0xFFFFA6C5)
                        )
                    ) {
                        Text("Delete Hunt", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showStartDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDateMillis = dateState.selectedDateMillis
                    updateStartIso()
                    showStartDatePicker = false
                    if (startDateMillis != null) showStartTimePicker = true
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }

    if (showEndDatePicker) {
        val dateState = rememberDatePickerState(initialSelectedDateMillis = endDateMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDateMillis = dateState.selectedDateMillis
                    updateEndIso()
                    showEndDatePicker = false
                    if (endDateMillis != null) showEndTimePicker = true
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }

    if (showStartTimePicker) {
        val timeState = rememberTimePickerState(startHour, startMinute, is24Hour)
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startHour = timeState.hour
                    startMinute = timeState.minute
                    updateStartIso()
                    showStartTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showEndTimePicker) {
        val timeState = rememberTimePickerState(endHour, endMinute, is24Hour)
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endHour = timeState.hour
                    endMinute = timeState.minute
                    updateEndIso()
                    showEndTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timeState) }
        )
    }

    if (showAddOwnerDialog) {
        AlertDialog(
            onDismissRequest = { showAddOwnerDialog = false },
            containerColor = Color(0xFF1B1125),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0A8C6),
            title = { Text("Add owner") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Enter the user ID of the participant you want to promote.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = ownerUserIdInput,
                        onValueChange = { ownerUserIdInput = it },
                        label = { Text("User ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
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
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.addOwner(ownerUserIdInput)
                        ownerUserIdInput = ""
                        showAddOwnerDialog = false
                    },
                    enabled = ownerUserIdInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB44BF3),
                        contentColor = Color.White
                    )
                ) { Text("Add Owner", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showAddOwnerDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteHuntDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteHuntDialog = false },
            containerColor = Color(0xFF1B1125),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFB0A8C6),
            title = { Text("Delete hunt?") },
            text = { Text("This will permanently delete the hunt and its data.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteHuntDialog = false
                        vm.deleteHunt(onHuntDeleted)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFB4233D),
                        contentColor = Color.White
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteHuntDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private data class LocalDateTimeParts(val dateMillis: Long, val hour: Int, val minute: Int)

private fun parseIsoToLocalParts(iso: String): LocalDateTimeParts? {
    val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'")
    for (pattern in patterns) {
        val parsed = runCatching {
            SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.parse(iso)
        }.getOrNull() ?: continue
        val cal = Calendar.getInstance().apply { time = parsed }
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return LocalDateTimeParts(
            dateMillis = cal.timeInMillis,
            hour = cal.get(Calendar.HOUR_OF_DAY),
            minute = cal.get(Calendar.MINUTE)
        )
    }
    return null
}

private fun formatDateTimeLabel(
    millis: Long?,
    hour: Int,
    minute: Int,
    is24Hour: Boolean
): String {
    if (millis == null) return "Pick date"
    val date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(millis))
    val time = if (is24Hour) {
        String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    } else {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }
    return "$date $time"
}

@Composable
private fun EditHuntTopBar(onBack: () -> Unit) {
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
                "Edit Hunt",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(48.dp))
        }
    }
}

@Composable
private fun EditHuntBottomBar(onSave: () -> Unit, enabled: Boolean, isSaving: Boolean) {
    Surface(
        color = Color(0xFF120A1C).copy(alpha = 0.96f),
        modifier = Modifier.navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = onSave,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB44BF3),
                    contentColor = Color.White
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EditUploadCoverCard(imageUri: String, onClick: () -> Unit) {
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
                    contentDescription = "Hunt cover image",
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
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color(0xFFB44BF3))
                }
            }
            Text(
                text = if (imageUri.isBlank()) "Upload Cover Image" else "Change Cover Image",
                color = Color(0xFFB44BF3),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "PNG or JPG, max 2MB",
                color = Color(0xFF9A8CB4),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EditSectionLabel(text: String) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        color = Color(0xFF8C7AA8),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun EditStyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFF8C7AA8)) },
        singleLine = minLines == 1,
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
private fun RowScope.EditDateTimePill(
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
