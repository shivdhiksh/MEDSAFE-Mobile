package com.shivdhikshith.medsafe.feature.inventory

import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.usecase.InventorySortType

data class InventoryUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val selectedStatusFilter: ExpiryStatus? = null,
    val selectedLocationFilter: String? = null,
    val selectedSortType: InventorySortType = InventorySortType.NEAREST_EXPIRY,
    val medicines: List<MedicineWithBatches> = emptyList(),
    val availableLocations: List<String> = emptyList(),
    val errorMessage: String? = null
)
