package com.shivdhikshith.medsafe.feature.reminders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.Reminder
import com.shivdhikshith.medsafe.presentation.components.EmptyState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: RemindersViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<Reminder?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Reminders") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Reminder"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Reminder"
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.reminders.isEmpty()) {
            EmptyState(
                title = "No Reminders Configured",
                subtitle = "Personal reminders for checking batches or taking user-specified medications will appear here.",
                icon = Icons.Outlined.Alarm,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                items(uiState.reminders, key = { it.id }) { reminder ->
                    val medicine = uiState.medicines.find { it.id == reminder.medicineId }
                    ReminderItem(
                        reminder = reminder,
                        medicineName = medicine?.name ?: "Unknown Medicine",
                        onToggle = { viewModel.toggleReminder(reminder) },
                        onDelete = { reminderToDelete = reminder }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddReminderDialog(
            medicines = uiState.medicines,
            onDismiss = { showAddDialog = false },
            onConfirm = { medicineId, title, timeInMillis, recurrence ->
                viewModel.addReminder(medicineId, title, timeInMillis, recurrence)
                showAddDialog = false
            }
        )
    }

    reminderToDelete?.let { reminder ->
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Delete Reminder") },
            text = { Text("Are you sure you want to delete '${reminder.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteReminder(reminder.id)
                        reminderToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReminderItem(
    reminder: Reminder,
    medicineName: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(reminder.timeInMillis) {
        timeFormat.format(reminder.timeInMillis)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = medicineName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = reminder.recurrence,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Reminder",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddReminderDialog(
    medicines: List<Medicine>,
    onDismiss: () -> Unit,
    onConfirm: (medicineId: Long, title: String, timeInMillis: Long, recurrence: String) -> Unit
) {
    if (medicines.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("No Medicines Found") },
            text = { Text("Please add at least one medicine to your inventory before creating personal reminders.") },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
        return
    }

    var selectedMedicineId by remember { mutableLongStateOf(medicines.first().id) }
    var title by remember { mutableStateOf("") }
    var selectedHour by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var recurrence by remember { mutableStateOf("DAILY") }
    var expandedMedicineDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Medicine Selector
                ExposedDropdownMenuBox(
                    expanded = expandedMedicineDropdown,
                    onExpandedChange = { expandedMedicineDropdown = !expandedMedicineDropdown }
                ) {
                    val currentMedicine = medicines.find { it.id == selectedMedicineId } ?: medicines.first()
                    OutlinedTextField(
                        value = "${currentMedicine.name} (${currentMedicine.strength})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Medicine") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMedicineDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMedicineDropdown,
                        onDismissRequest = { expandedMedicineDropdown = false }
                    ) {
                        medicines.forEach { medicine ->
                            DropdownMenuItem(
                                text = { Text("${medicine.name} (${medicine.strength})") },
                                onClick = {
                                    selectedMedicineId = medicine.id
                                    expandedMedicineDropdown = false
                                }
                            )
                        }
                    }
                }

                // Reminder Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reminder Label") },
                    placeholder = { Text("e.g., Morning Dose, Check stock") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Time selector
                Text(
                    text = "Reminder Time",
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = String.format("%02d", selectedHour),
                        onValueChange = {
                            val parsed = it.toIntOrNull()
                            if (parsed != null && parsed in 0..23) selectedHour = parsed
                        },
                        label = { Text("Hour (0-23)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Text(":", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(
                        value = String.format("%02d", selectedMinute),
                        onValueChange = {
                            val parsed = it.toIntOrNull()
                            if (parsed != null && parsed in 0..59) selectedMinute = parsed
                        },
                        label = { Text("Minute (0-59)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Recurrence
                Text(
                    text = "Recurrence",
                    style = MaterialTheme.typography.titleSmall
                )
                listOf("DAILY" to "Daily", "WEEKDAYS" to "Weekdays (Mon-Fri)", "ONCE" to "Once").forEach { (key, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { recurrence = key },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = recurrence == key,
                            onClick = { recurrence = key }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cal = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, selectedHour)
                        set(Calendar.MINUTE, selectedMinute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val finalTitle = title.ifBlank { "Medicine Reminder" }
                    onConfirm(selectedMedicineId, finalTitle, cal.timeInMillis, recurrence)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
