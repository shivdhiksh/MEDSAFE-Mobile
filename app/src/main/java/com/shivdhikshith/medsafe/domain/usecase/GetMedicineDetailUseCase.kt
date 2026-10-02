package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class GetMedicineDetailUseCase(
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateExpiryStatusUseCase: CalculateExpiryStatusUseCase = CalculateExpiryStatusUseCase()
) {
    operator fun invoke(medicineId: Long): Flow<MedicineWithBatches?> {
        return combine(
            medicineRepository.observeMedicineWithBatches(medicineId),
            settingsRepository.observeSettings()
        ) { medicineWithBatches, settings ->
            if (medicineWithBatches == null) return@combine null

            val today = LocalDate.now()
            val updatedBatches = medicineWithBatches.batches.map { batch ->
                val isDisposed = batch.status == ExpiryStatus.DISPOSED || batch.disposedDate != null
                val computedStatus = calculateExpiryStatusUseCase(
                    expiryDateEpochMillis = batch.expiryDate,
                    isDisposed = isDisposed,
                    warningThresholdDays = settings.alertDays,
                    currentLocalDate = today
                )
                batch.copy(status = computedStatus)
            }
            medicineWithBatches.copy(batches = updatedBatches)
        }
    }
}
