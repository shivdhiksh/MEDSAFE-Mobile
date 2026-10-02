package com.shivdhikshith.medsafe.feature.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.feature.dashboard.components.ExpirySummaryCard
import com.shivdhikshith.medsafe.feature.dashboard.components.UpcomingExpiryItem
import com.shivdhikshith.medsafe.presentation.components.EmptyState
import com.shivdhikshith.medsafe.presentation.components.SectionHeader
import com.shivdhikshith.medsafe.presentation.theme.StatusExpiredRed
import com.shivdhikshith.medsafe.presentation.theme.StatusExpiredRedContainer
import com.shivdhikshith.medsafe.presentation.theme.StatusValidGreen
import com.shivdhikshith.medsafe.presentation.theme.StatusValidGreenContainer
import com.shivdhikshith.medsafe.presentation.theme.StatusWarningAmber
import com.shivdhikshith.medsafe.presentation.theme.StatusWarningAmberContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToMedicineDetail: (Long) -> Unit,
    onNavigateToScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MEDSAFE",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Smart Scan",
                            tint = MaterialTheme.colorScheme.primary
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
                onClick = onNavigateToAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Medicine")
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
        } else {
            val summary = uiState.summary

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Batch Expiry Overview",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 2x2 Grid of metrics
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExpirySummaryCard(
                            count = summary.totalActive,
                            label = "Total Active",
                            icon = Icons.Default.Inventory,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        ExpirySummaryCard(
                            count = summary.validCount,
                            label = "Valid Batches",
                            icon = Icons.Default.CheckCircle,
                            containerColor = StatusValidGreenContainer,
                            contentColor = StatusValidGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExpirySummaryCard(
                            count = summary.expiringSoonCount,
                            label = "Expiring Soon",
                            icon = Icons.Default.Warning,
                            containerColor = StatusWarningAmberContainer,
                            contentColor = StatusWarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        ExpirySummaryCard(
                            count = summary.expiredCount,
                            label = "Expired",
                            icon = Icons.Default.Error,
                            containerColor = StatusExpiredRedContainer,
                            contentColor = StatusExpiredRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = "Urgent & Upcoming Expiries")
                }

                if (summary.urgentBatches.isEmpty()) {
                    item {
                        EmptyState(
                            title = if (summary.totalActive == 0) "No Medications Added" else "All Batches Valid",
                            subtitle = if (summary.totalActive == 0)
                                "Tap + to add your first medicine and batch to track expiry dates."
                            else
                                "No batches are expiring soon or expired. All tracked medications are currently within their expiry date.",
                            icon = Icons.Outlined.CheckCircleOutline,
                            actionButtonText = if (summary.totalActive == 0) "Add Medicine" else null,
                            onActionClick = if (summary.totalActive == 0) onNavigateToAddMedicine else null
                        )
                    }
                } else {
                    items(summary.urgentBatches, key = { it.batch.id }) { item ->
                        UpcomingExpiryItem(
                            item = item,
                            onClick = { onNavigateToMedicineDetail(item.medicine.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
