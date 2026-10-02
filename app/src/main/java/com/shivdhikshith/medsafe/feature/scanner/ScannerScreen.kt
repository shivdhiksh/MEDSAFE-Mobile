package com.shivdhikshith.medsafe.feature.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.shivdhikshith.medsafe.core.common.Constants
import com.shivdhikshith.medsafe.core.scanner.BarcodeScannerManager
import com.shivdhikshith.medsafe.core.scanner.ExtractedPackageData
import com.shivdhikshith.medsafe.core.scanner.TextRecognitionManager
import com.shivdhikshith.medsafe.presentation.components.EmptyState
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMedicineDetail: (Long) -> Unit,
    onNavigateToAddMedicineWithPrefill: (name: String, strength: String, batch: String, expiry: String, mfg: String, barcode: String) -> Unit,
    onNavigateToAddMedicine: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val barcodeScannerManager = remember { BarcodeScannerManager() }
    val textRecognitionManager = remember { TextRecognitionManager() }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var cameraControl by remember { mutableStateOf<Camera?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Toggle torch
    LaunchedEffect(uiState.isTorchEnabled, cameraControl) {
        cameraControl?.cameraControl?.enableTorch(uiState.isTorchEnabled)
    }

    // Barcode result dialog
    if (uiState.isReviewDialogVisible && uiState.scannedBarcode != null) {
        val barcode = uiState.scannedBarcode!!
        val matchedMed = uiState.matchedMedicine

        AlertDialog(
            onDismissRequest = viewModel::dismissReviewDialog,
            title = { Text(if (matchedMed != null) "Medicine Recognized" else "New Barcode Detected") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Barcode: $barcode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (matchedMed != null) {
                        Text(
                            text = "${matchedMed.medicine.name} ${matchedMed.medicine.strength}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Currently recorded with ${matchedMed.batches.size} batch(es).",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "This code does not match any medicine currently in your local inventory. Would you like to create a new medicine with this barcode?",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val med = matchedMed
                        viewModel.dismissReviewDialog()
                        if (med != null) {
                            onNavigateToMedicineDetail(med.medicine.id)
                        } else {
                            onNavigateToAddMedicineWithPrefill("", "", "", "", "", barcode)
                        }
                    }
                ) {
                    Text(if (matchedMed != null) "View Medicine" else "Add New Medicine")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissReviewDialog) {
                    Text("Scan Again")
                }
            }
        )
    }

    // OCR review dialog (Strictly allows manual user review before persisting)
    if (uiState.isReviewDialogVisible && uiState.extractedPackageData != null) {
        val extracted = uiState.extractedPackageData!!
        var editedName by remember { mutableStateOf(extracted.medicineName) }
        var editedStrength by remember { mutableStateOf(extracted.strength) }
        var editedBatch by remember { mutableStateOf(extracted.batchNumber) }
        var editedExpiry by remember { mutableStateOf(extracted.expiryDateString) }
        var editedMfg by remember { mutableStateOf(extracted.manufacturer) }

        AlertDialog(
            onDismissRequest = viewModel::dismissReviewDialog,
            title = { Text("Review Extracted Package Data") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Please verify the information recognized from the package. Values are suggestions only and must be verified by you.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Medicine Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedStrength,
                        onValueChange = { editedStrength = it },
                        label = { Text("Strength") },
                        placeholder = { Text("e.g. 500mg") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedBatch,
                        onValueChange = { editedBatch = it },
                        label = { Text("Batch / Lot #") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedExpiry,
                        onValueChange = { editedExpiry = it },
                        label = { Text("Expiry Date (YYYY-MM-DD)") },
                        placeholder = { Text("e.g. 2026-12-31") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedMfg,
                        onValueChange = { editedMfg = it },
                        label = { Text("Manufacturer") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissReviewDialog()
                        onNavigateToAddMedicineWithPrefill(
                            editedName.trim(),
                            editedStrength.trim(),
                            editedBatch.trim(),
                            editedExpiry.trim(),
                            editedMfg.trim(),
                            ""
                        )
                    },
                    enabled = editedName.isNotBlank()
                ) {
                    Text("Use This Information")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissReviewDialog) {
                    Text("Discard")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Scanner") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                },
                actions = {
                    if (hasCameraPermission) {
                        IconButton(onClick = viewModel::toggleTorch) {
                            Icon(
                                imageVector = if (uiState.isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                contentDescription = "Toggle flashlight"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (!hasCameraPermission) {
            // Permission fallback
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyState(
                    title = "Camera Permission Required",
                    subtitle = "Scanning barcodes and package text requires camera access. All image processing is performed completely offline on your device.",
                    icon = Icons.Outlined.CameraAlt,
                    actionButtonText = "Grant Camera Permission",
                    onActionClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onNavigateToAddMedicine,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Enter Medicine Manually")
                }
            }
        } else {
            // Live Camera View
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                if (uiState.isReviewDialogVisible) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }

                                if (uiState.mode == ScannerMode.BARCODE) {
                                    barcodeScannerManager.processImageProxy(
                                        imageProxy = imageProxy,
                                        onSuccess = { barcode ->
                                            viewModel.onBarcodeDetected(barcode)
                                        },
                                        onComplete = {}
                                    )
                                } else {
                                    textRecognitionManager.processImageProxy(
                                        imageProxy = imageProxy,
                                        onSuccess = { data ->
                                            viewModel.onOcrDetected(data)
                                        },
                                        onComplete = {}
                                    )
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                val cam = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControl = cam
                            } catch (_: Exception) {
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay Viewfinder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .background(Color.Black.copy(alpha = 0.05f))
                    )
                }

                // Controls and Mode Switcher at top
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.mode == ScannerMode.BARCODE,
                                onClick = { viewModel.setMode(ScannerMode.BARCODE) },
                                label = { Text("Barcode / QR") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                                }
                            )
                            FilterChip(
                                selected = uiState.mode == ScannerMode.OCR,
                                onClick = { viewModel.setMode(ScannerMode.OCR) },
                                label = { Text("Package OCR") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.TextFields, contentDescription = null)
                                }
                            )
                        }
                    }
                }

                // Bottom bar with manual fallback and disclaimer
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.mode == ScannerMode.BARCODE)
                            "Center medicine barcode or QR within frame"
                        else
                            "Align medicine packaging text within frame to extract details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToAddMedicine,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Enter Manually")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Constants.SAFETY_DISCLAIMER,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
