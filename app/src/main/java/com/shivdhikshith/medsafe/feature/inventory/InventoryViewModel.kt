package com.shivdhikshith.medsafe.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.usecase.InventorySortType
import com.shivdhikshith.medsafe.domain.usecase.ObserveInventoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(
    private val observeInventoryUseCase: ObserveInventoryUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<ExpiryStatus?>(null)
    private val _locationFilter = MutableStateFlow<String?>(null)
    private val _sortType = MutableStateFlow(InventorySortType.NEAREST_EXPIRY)

    val uiState: StateFlow<InventoryUiState> = combine(
        _searchQuery,
        _statusFilter,
        _locationFilter,
        _sortType
    ) { query, status, location, sort ->
        FilterParams(query, status, location, sort)
    }.flatMapLatest { params ->
        observeInventoryUseCase(
            searchQuery = params.query,
            statusFilter = params.status,
            locationFilter = params.location,
            sortType = params.sort
        ).combine(MutableStateFlow(params)) { medicines, p ->
            val locations = medicines.flatMap { it.batches.map { b -> b.storageLocation } }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()

            InventoryUiState(
                isLoading = false,
                searchQuery = p.query,
                selectedStatusFilter = p.status,
                selectedLocationFilter = p.location,
                selectedSortType = p.sort,
                medicines = medicines,
                availableLocations = locations,
                errorMessage = null
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterSelected(status: ExpiryStatus?) {
        _statusFilter.value = status
    }

    fun onLocationFilterSelected(location: String?) {
        _locationFilter.value = location
    }

    fun onSortTypeSelected(sortType: InventorySortType) {
        _sortType.value = sortType
    }

    private data class FilterParams(
        val query: String,
        val status: ExpiryStatus?,
        val location: String?,
        val sort: InventorySortType
    )

    class Factory(
        private val observeInventoryUseCase: ObserveInventoryUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(observeInventoryUseCase) as T
        }
    }
}
