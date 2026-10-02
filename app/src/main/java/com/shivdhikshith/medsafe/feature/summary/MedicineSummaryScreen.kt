package com.shivdhikshith.medsafe.feature.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.core.common.Constants
import com.shivdhikshith.medsafe.core.extensions.toDisplayDate
import com.shivdhikshith.medsafe.presentation.components.EmptyState
import com.shivdhikshith.medsafe.presentation.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineSummaryScreen(
    viewModel: MedicineSummaryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showEditNotesDialog by remember { mutableStateOf(false) }

    if (showEditNotesDialog && uiState.medicineWithBatches != null) {
        var editedNotes by remember { mutableStateOf(uiState.medicineWithBatches!!.medicine.notes) }

        AlertDialog(
            onDismissRequest = { showEditNotesDialog = false },
            title = { Text("Edit Storage & Personal Notes") },
            text = {
                OutlinedTextField(
                    value = editedNotes,
                    onValueChange = { editedNotes = it },
                    label = { Text("Personal Notes & Storage Guidelines") },
                    placeholder = { Text("e.g. Keep in a cool dry place away from sunlight.") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateNotes(editedNotes)
                        showEditNotesDialog = false
                    }
                ) {
                    Text("Save Notes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNotesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Summary") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
        } else if (uiState.medicineWithBatches == null) {
            EmptyState(
                title = "Summary Unavailable",
                subtitle = "Could not load medication summary.",
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            val med = uiState.medicineWithBatches!!.medicine
            val batches = uiState.medicineWithBatches!!.batches

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Non-medical guidance banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "User-Entered Record",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This summary displays information you recorded for personal organization. It contains no clinical dosage or medical recommendation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                SectionHeader(title = "Medication Overview")
                SummaryCard(
                    title = med.name,
                    subtitle = listOf(med.strength, med.medicineType, med.manufacturer)
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Personal & Storage Notes",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showEditNotesDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit notes",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                SummaryCard(
                    title = "User Notes",
                    subtitle = if (med.notes.isNotBlank()) med.notes else "No personal notes entered."
                )

                SectionHeader(title = "Recorded Batch History (${batches.size})")
                if (batches.isEmpty()) {
                    Text(
                        text = "No batches recorded for this medicine.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    batches.forEach { batch ->
                        SummaryCard(
                            title = "Batch ${if (batch.batchNumber.isNotBlank()) batch.batchNumber else "N/A"} (${batch.status.label})",
                            subtitle = "Expiry Date: ${batch.expiryDate.toDisplayDate()} • Qty: ${batch.quantity} • Location: ${if (batch.storageLocation.isNotBlank()) batch.storageLocation else "Unassigned"}"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = Constants.SAFETY_DISCLAIMER,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
