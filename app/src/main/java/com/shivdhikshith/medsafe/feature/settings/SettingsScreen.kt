package com.shivdhikshith.medsafe.feature.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.shivdhikshith.medsafe.presentation.components.SectionHeader
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showBackupChoiceDialog by remember { mutableStateOf(false) }
    var pendingBackupJson by remember { mutableStateOf<String?>(null) }
    var pendingBackupFileName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    // Document creator for explicitly saving JSON backup file to device storage (Downloads, Documents, etc.)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null && pendingBackupJson != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(pendingBackupJson!!.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                viewModel.onBackupFileSaved(pendingBackupFileName ?: "MEDSAFE_Backup.json")
            } catch (e: Exception) {
                // Handled via action message or snackbar
            }
        }
    }

    fun shareBackupFile(backupJson: String, fileName: String) {
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()
            val exportFile = File(exportDir, fileName)
            exportFile.writeText(backupJson, Charsets.UTF_8)
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                exportFile
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, backupJson)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share MEDSAFE Backup"))
        } catch (e: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, fileName)
                putExtra(Intent.EXTRA_TEXT, backupJson)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share MEDSAFE Backup"))
        }
    }

    // Backup export destination chooser dialog
    if (showBackupChoiceDialog && pendingBackupJson != null) {
        val fileName = pendingBackupFileName ?: viewModel.getBackupFileName()
        AlertDialog(
            onDismissRequest = { showBackupChoiceDialog = false },
            title = { Text("Export MEDSAFE Backup") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Backup ready: $fileName",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Choose how to export your backup file:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackupChoiceDialog = false
                        createDocumentLauncher.launch(fileName)
                    }
                ) {
                    Text("Save to Device")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showBackupChoiceDialog = false
                            shareBackupFile(pendingBackupJson!!, fileName)
                        }
                    ) {
                        Text("Share File")
                    }
                    TextButton(onClick = { showBackupChoiceDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Document picker for restoring JSON backup
    val restoreFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                if (jsonString != null) {
                    viewModel.onRestoreFileSelected(jsonString)
                }
            } catch (e: Exception) {
                // File read error handled via validation
            }
        }
    }

    // Restore confirmation dialog with Replace vs Merge options
    uiState.pendingRestoreValidation?.let { validation ->
        AlertDialog(
            onDismissRequest = viewModel::dismissRestoreDialog,
            title = { Text("Restore MEDSAFE Data") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Backup contains ${validation.medicineCount} medicines, ${validation.batchCount} batches, and ${validation.reminderCount} personal reminders.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose how to apply this backup:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.confirmRestore(replaceAll = false) }) {
                        Text("Merge Data")
                    }
                    Button(
                        onClick = { viewModel.confirmRestore(replaceAll = true) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Replace All")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRestoreDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SectionHeader(title = "Alerts & Notifications")

            Card(
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
                        Text(text = "Expiry Notifications", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Receive local alerts on this device when medicines approach expiry.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.notificationsEnabled,
                        onCheckedChange = viewModel::toggleNotifications
                    )
                }
            }

            SectionHeader(title = "Expiry Alert Thresholds")

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Organizational expiry reminders triggered before or upon batch expiry:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )

                    listOf(
                        30 to "30 days before expiry",
                        7 to "7 days before expiry",
                        1 to "1 day before expiry"
                    ).forEach { (days, label) ->
                        val isChecked = settings.alertThresholdDays.contains(days)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleThreshold(days, !isChecked) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleThreshold(days, it) }
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleNotifyOnExpired(!settings.notifyOnExpired) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = settings.notifyOnExpired,
                            onCheckedChange = { viewModel.toggleNotifyOnExpired(it) }
                        )
                        Text(
                            text = "Expired (when expiry date is reached)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            SectionHeader(title = "Data Management & Backup")

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Offline Export & Backup",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Export your inventory as spreadsheets or create a full device backup to restore anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.exportCsv { csvText ->
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_SUBJECT, "MEDSAFE Inventory Export")
                                        putExtra(Intent.EXTRA_TEXT, csvText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Inventory (CSV)"))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text("Export CSV")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.exportJson { jsonText ->
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_SUBJECT, "MEDSAFE Inventory JSON")
                                        putExtra(Intent.EXTRA_TEXT, jsonText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Inventory (JSON)"))
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Outlined.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text("Export JSON")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.createBackup { backupJson ->
                                    val fileName = viewModel.getBackupFileName()
                                    pendingBackupJson = backupJson
                                    pendingBackupFileName = fileName
                                    showBackupChoiceDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Outlined.Backup, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text("Create Backup")
                        }

                        OutlinedButton(
                            onClick = {
                                restoreFileLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Outlined.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                            Text("Restore")
                        }
                    }
                }
            }

            SectionHeader(title = "Theme")

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    listOf(
                        "SYSTEM" to "System Default",
                        "LIGHT" to "Light",
                        "DARK" to "Dark"
                    ).forEach { (themeKey, themeLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateTheme(themeKey) }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.theme == themeKey,
                                onClick = { viewModel.updateTheme(themeKey) }
                            )
                            Text(
                                text = themeLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            SectionHeader(title = "Privacy & Data")

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "100% Offline & Private",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MEDSAFE stores all information locally in your device's encrypted SQLite Room storage. No account is required and no telemetries or data leave your phone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
