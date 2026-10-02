package com.shivdhikshith.medsafe.feature.medicine

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.core.common.Constants
import com.shivdhikshith.medsafe.core.utils.DateUtils
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.feature.medicine.components.MedicineInfoSection
import com.shivdhikshith.medsafe.presentation.components.BatchCard
import com.shivdhikshith.medsafe.presentation.components.EmptyState
import com.shivdhikshith.medsafe.presentation.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineDetailScreen(
    viewModel: MedicineDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (Long) -> Unit,
    onNavigateToPhotos: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditMedicineDialog by remember { mutableStateOf(false) }
    var showAddBatchDialog by remember { mutableStateOf(false) }
    var batchToEdit by remember { mutableStateOf<Batch?>(null) }
    var batchToDelete by remember { mutableStateOf<Batch?>(null) }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    // Delete Medicine Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Medicine?") },
            text = { Text("This will permanently delete this medicine and all its recorded batches from your local device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteMedicine()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Batch Dialog
    batchToDelete?.let { batch ->
        AlertDialog(
            onDismissRequest = { batchToDelete = null },
            title = { Text("Delete Batch?") },
            text = { Text("Are you sure you want to delete batch \"${if (batch.batchNumber.isNotBlank()) batch.batchNumber else "unspecified"}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBatch(batch.id)
                        batchToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { batchToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Medicine Dialog
    if (showEditMedicineDialog && uiState.medicineWithBatches != null) {
        val currentMed = uiState.medicineWithBatches!!.medicine
        var name by remember { mutableStateOf(currentMed.name) }
        var strength by remember { mutableStateOf(currentMed.strength) }
        var medicineType by remember { mutableStateOf(currentMed.medicineType) }
        var manufacturer by remember { mutableStateOf(currentMed.manufacturer) }
        var barcode by remember { mutableStateOf(currentMed.barcode) }
        var notes by remember { mutableStateOf(currentMed.notes) }

        AlertDialog(
            onDismissRequest = { showEditMedicineDialog = false },
            title = { Text("Edit Medicine Information") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = strength,
                        onValueChange = { strength = it },
                        label = { Text("Strength") },
                        placeholder = { Text("e.g. 500mg") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = medicineType,
                        onValueChange = { medicineType = it },
                        label = { Text("Form / Type") },
                        placeholder = { Text("e.g. Tablet, Syrup") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = manufacturer,
                        onValueChange = { manufacturer = it },
                        label = { Text("Manufacturer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / GTIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.updateMedicine(
                                name = name,
                                strength = strength,
                                medicineType = medicineType,
                                manufacturer = manufacturer,
                                barcode = barcode,
                                notes = notes
                            )
                            showEditMedicineDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditMedicineDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Batch Dialog
    if (showAddBatchDialog) {
        var batchNumber by remember { mutableStateOf("") }
        var expiryDateString by remember { mutableStateOf("") }
        var quantityString by remember { mutableStateOf("1") }
        var storageLocation by remember { mutableStateOf("") }
        var dateError by remember { mutableStateOf<String?>(null) }
        var qtyError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddBatchDialog = false },
            title = { Text("Add Batch") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Batch / Lot #") },
                        placeholder = { Text("e.g. BT-1024") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expiryDateString,
                        onValueChange = {
                            expiryDateString = it
                            dateError = null
                        },
                        label = { Text("Expiry Date * (YYYY-MM-DD)") },
                        placeholder = { Text("e.g. 2026-12-31") },
                        isError = dateError != null,
                        supportingText = dateError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quantityString,
                        onValueChange = {
                            quantityString = it
                            qtyError = null
                        },
                        label = { Text("Quantity *") },
                        isError = qtyError != null,
                        supportingText = qtyError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = storageLocation,
                        onValueChange = { storageLocation = it },
                        label = { Text("Storage Location") },
                        placeholder = { Text("e.g. First Aid Box, Fridge") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedDate = DateUtils.parseIsoDate(expiryDateString)
                        val qty = quantityString.toIntOrNull() ?: 0

                        if (parsedDate == null) {
                            dateError = "Invalid date format. Use YYYY-MM-DD"
                            return@Button
                        }
                        if (qty < 1) {
                            qtyError = "Quantity must be at least 1"
                            return@Button
                        }

                        val epochMillis = DateUtils.localDateToEpochMillis(parsedDate)
                        viewModel.addBatch(
                            batchNumber = batchNumber,
                            expiryDate = epochMillis,
                            quantity = qty,
                            storageLocation = storageLocation
                        )
                        showAddBatchDialog = false
                    }
                ) {
                    Text("Add Batch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBatchDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Batch Dialog
    batchToEdit?.let { batch ->
        var batchNumber by remember { mutableStateOf(batch.batchNumber) }
        var expiryDateString by remember { mutableStateOf(DateUtils.formatToIsoDate(batch.expiryDate)) }
        var quantityString by remember { mutableStateOf(batch.quantity.toString()) }
        var storageLocation by remember { mutableStateOf(batch.storageLocation) }
        var dateError by remember { mutableStateOf<String?>(null) }
        var qtyError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { batchToEdit = null },
            title = { Text("Edit Batch") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = batchNumber,
                        onValueChange = { batchNumber = it },
                        label = { Text("Batch / Lot #") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expiryDateString,
                        onValueChange = {
                            expiryDateString = it
                            dateError = null
                        },
                        label = { Text("Expiry Date * (YYYY-MM-DD)") },
                        isError = dateError != null,
                        supportingText = dateError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quantityString,
                        onValueChange = {
                            quantityString = it
                            qtyError = null
                        },
                        label = { Text("Quantity *") },
                        isError = qtyError != null,
                        supportingText = qtyError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = storageLocation,
                        onValueChange = { storageLocation = it },
                        label = { Text("Storage Location") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedDate = DateUtils.parseIsoDate(expiryDateString)
                        val qty = quantityString.toIntOrNull() ?: 0

                        if (parsedDate == null) {
                            dateError = "Invalid date format. Use YYYY-MM-DD"
                            return@Button
                        }
                        if (qty < 1) {
                            qtyError = "Quantity must be at least 1"
                            return@Button
                        }

                        val epochMillis = DateUtils.localDateToEpochMillis(parsedDate)
                        viewModel.updateBatch(
                            batch.copy(
                                batchNumber = batchNumber.trim(),
                                expiryDate = epochMillis,
                                quantity = qty,
                                storageLocation = storageLocation.trim()
                            )
                        )
                        batchToEdit = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { batchToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medicine Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditMedicineDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit medicine details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete medicine",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                title = "Medicine Not Found",
                subtitle = "This medicine may have been deleted or does not exist.",
                actionButtonText = "Back to Inventory",
                onActionClick = onNavigateBack,
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            val medWithBatches = uiState.medicineWithBatches!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                MedicineInfoSection(medicine = medWithBatches.medicine)

                // Quick feature navigation pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onNavigateToSummary(medWithBatches.medicine.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Summary")
                    }

                    OutlinedButton(
                        onClick = { onNavigateToPhotos(medWithBatches.medicine.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Photos")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Batches (${medWithBatches.batches.size})",
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(onClick = { showAddBatchDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Add Batch")
                    }
                }

                if (medWithBatches.batches.isEmpty()) {
                    EmptyState(
                        title = "No Batches Recorded",
                        subtitle = "Add a batch with an expiry date and quantity for this medicine.",
                        actionButtonText = "Add Batch",
                        onActionClick = { showAddBatchDialog = true }
                    )
                } else {
                    medWithBatches.batches.forEach { batch ->
                        BatchCard(
                            batch = batch,
                            onEditClick = { batchToEdit = batch },
                            onDeleteClick = { batchToDelete = batch },
                            onDisposeClick = { viewModel.disposeBatch(batch.id) },
                            onRestoreClick = { viewModel.restoreBatch(batch.id, batch.expiryDate) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Safety Disclaimer banner
                Text(
                    text = Constants.SAFETY_DISCLAIMER,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
