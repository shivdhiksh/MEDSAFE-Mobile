package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

enum class InventorySortType {
    NEAREST_EXPIRY,
    FARTHEST_EXPIRY,
    NAME_A_TO_Z,
    NAME_Z_TO_A
}

class ObserveInventoryUseCase(
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateExpiryStatusUseCase: CalculateExpiryStatusUseCase = CalculateExpiryStatusUseCase()
) {
    operator fun invoke(
        searchQuery: String = "",
        statusFilter: ExpiryStatus? = null,
        locationFilter: String? = null,
        sortType: InventorySortType = InventorySortType.NEAREST_EXPIRY
    ): Flow<List<MedicineWithBatches>> {
        return combine(
            medicineRepository.observeAllMedicinesWithBatches(),
            settingsRepository.observeSettings()
        ) { medicines, settings ->
            val today = LocalDate.now()

            // 1. Recalculate dynamic batch statuses based on current threshold
            val evaluatedList = medicines.map { medWithBatches ->
                val updatedBatches = medWithBatches.batches.map { batch ->
                    val isDisposed = batch.status == ExpiryStatus.DISPOSED || batch.disposedDate != null
                    val computedStatus = calculateExpiryStatusUseCase(
                        expiryDateEpochMillis = batch.expiryDate,
                        isDisposed = isDisposed,
                        warningThresholdDays = settings.alertDays,
                        currentLocalDate = today
                    )
                    batch.copy(status = computedStatus)
                }
                medWithBatches.copy(batches = updatedBatches)
            }

            // 2. Apply search filter
            val searchFiltered = if (searchQuery.isBlank()) {
                evaluatedList
            } else {
                val query = searchQuery.trim().lowercase()
                evaluatedList.filter { item ->
                    item.medicine.name.lowercase().contains(query) ||
                            item.medicine.manufacturer.lowercase().contains(query) ||
                            item.medicine.barcode.lowercase().contains(query) ||
                            item.medicine.notes.lowercase().contains(query) ||
                            item.batches.any { it.storageLocation.lowercase().contains(query) || it.batchNumber.lowercase().contains(query) }
                }
            }

            // 3. Apply status filter
            val statusFiltered = if (statusFilter == null) {
                searchFiltered
            } else {
                searchFiltered.filter { item ->
                    item.batches.any { it.status == statusFilter }
                }
            }

            // 4. Apply location filter
            val locationFiltered = if (locationFilter.isNullOrBlank()) {
                statusFiltered
            } else {
                statusFiltered.filter { item ->
                    item.batches.any { it.storageLocation.equals(locationFilter, ignoreCase = true) }
                }
            }

            // 5. Apply sorting
            when (sortType) {
                InventorySortType.NEAREST_EXPIRY -> locationFiltered.sortedBy { item ->
                    item.batches.filter { it.status != ExpiryStatus.DISPOSED }
                        .minByOrNull { it.expiryDate }?.expiryDate ?: Long.MAX_VALUE
                }
                InventorySortType.FARTHEST_EXPIRY -> locationFiltered.sortedByDescending { item ->
                    item.batches.filter { it.status != ExpiryStatus.DISPOSED }
                        .maxByOrNull { it.expiryDate }?.expiryDate ?: Long.MIN_VALUE
                }
                InventorySortType.NAME_A_TO_Z -> locationFiltered.sortedBy { it.medicine.name.lowercase() }
                InventorySortType.NAME_Z_TO_A -> locationFiltered.sortedByDescending { it.medicine.name.lowercase() }
            }
        }
    }
}
