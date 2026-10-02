package com.shivdhikshith.medsafe.feature.inventory

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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shivdhikshith.medsafe.domain.usecase.InventorySortType
import com.shivdhikshith.medsafe.feature.inventory.components.InventoryFilterChips
import com.shivdhikshith.medsafe.presentation.components.EmptyState
import com.shivdhikshith.medsafe.presentation.components.MedicineCard
import com.shivdhikshith.medsafe.presentation.components.SearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToMedicineDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Medicine Inventory",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort Inventory"
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Nearest Expiry") },
                                onClick = {
                                    viewModel.onSortTypeSelected(InventorySortType.NEAREST_EXPIRY)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Farthest Expiry") },
                                onClick = {
                                    viewModel.onSortTypeSelected(InventorySortType.FARTHEST_EXPIRY)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Name (A to Z)") },
                                onClick = {
                                    viewModel.onSortTypeSelected(InventorySortType.NAME_A_TO_Z)
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Name (Z to A)") },
                                onClick = {
                                    viewModel.onSortTypeSelected(InventorySortType.NAME_Z_TO_A)
                                    showSortMenu = false
                                }
                            )
                        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            SearchField(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholderText = "Search by name, brand, location…"
            )

            Spacer(modifier = Modifier.height(8.dp))

            InventoryFilterChips(
                selectedStatus = uiState.selectedStatusFilter,
                onStatusSelected = viewModel::onStatusFilterSelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.medicines.isEmpty()) {
                val isFiltering = uiState.searchQuery.isNotBlank() || uiState.selectedStatusFilter != null
                EmptyState(
                    title = if (isFiltering) "No matching medications" else "Inventory is Empty",
                    subtitle = if (isFiltering)
                        "Try adjusting your search terms or filter selection."
                    else
                        "You haven't added any medicines yet. Tap 'Add Medicine' to get started.",
                    actionButtonText = if (!isFiltering) "Add Medicine" else null,
                    onActionClick = if (!isFiltering) onNavigateToAddMedicine else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.medicines, key = { it.medicine.id }) { item ->
                        MedicineCard(
                            medicineWithBatches = item,
                            onClick = { onNavigateToMedicineDetail(item.medicine.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
