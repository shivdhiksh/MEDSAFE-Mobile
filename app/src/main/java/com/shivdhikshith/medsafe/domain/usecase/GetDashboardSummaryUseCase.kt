package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.model.BatchWithMedicine
import com.shivdhikshith.medsafe.domain.model.DashboardSummary
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class GetDashboardSummaryUseCase(
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateExpiryStatusUseCase: CalculateExpiryStatusUseCase = CalculateExpiryStatusUseCase()
) {
    operator fun invoke(): Flow<DashboardSummary> {
        return combine(
            medicineRepository.observeAllMedicinesWithBatches(),
            settingsRepository.observeSettings()
        ) { medicines, settings ->
            val today = LocalDate.now()
            var validCount = 0
            var expiringSoonCount = 0
            var expiredCount = 0
            val urgentBatches = mutableListOf<BatchWithMedicine>()

            medicines.forEach { medicineWithBatches ->
                val medicine = medicineWithBatches.medicine
                medicineWithBatches.batches.forEach { batch ->
                    if (batch.status != ExpiryStatus.DISPOSED) {
                        val currentStatus = calculateExpiryStatusUseCase(
                            expiryDateEpochMillis = batch.expiryDate,
                            isDisposed = false,
                            warningThresholdDays = settings.alertDays,
                            currentLocalDate = today
                        )

                        when (currentStatus) {
                            ExpiryStatus.VALID -> validCount++
                            ExpiryStatus.EXPIRING_SOON -> {
                                expiringSoonCount++
                                urgentBatches.add(BatchWithMedicine(batch.copy(status = currentStatus), medicine))
                            }
                            ExpiryStatus.EXPIRED -> {
                                expiredCount++
                                urgentBatches.add(BatchWithMedicine(batch.copy(status = currentStatus), medicine))
                            }
                            ExpiryStatus.DISPOSED -> Unit
                        }
                    }
                }
            }

            val totalActive = validCount + expiringSoonCount + expiredCount
            val sortedUrgent = urgentBatches.sortedBy { it.batch.expiryDate }

            DashboardSummary(
                totalActive = totalActive,
                validCount = validCount,
                expiringSoonCount = expiringSoonCount,
                expiredCount = expiredCount,
                urgentBatches = sortedUrgent
            )
        }
    }
}
