package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository

class AddBatchUseCase(
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateExpiryStatusUseCase: CalculateExpiryStatusUseCase = CalculateExpiryStatusUseCase()
) {
    suspend operator fun invoke(batch: Batch): Long {
        require(batch.expiryDate > 0) { "Valid expiry date required" }
        require(batch.quantity >= 1) { "Quantity must be at least 1" }

        val settings = settingsRepository.getSettings()
        val status = calculateExpiryStatusUseCase(
            expiryDateEpochMillis = batch.expiryDate,
            isDisposed = false,
            warningThresholdDays = settings.alertDays
        )
        return medicineRepository.insertBatch(batch.copy(status = status))
    }
}

class UpdateBatchUseCase(
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository,
    private val calculateExpiryStatusUseCase: CalculateExpiryStatusUseCase = CalculateExpiryStatusUseCase()
) {
    suspend operator fun invoke(batch: Batch) {
        require(batch.expiryDate > 0) { "Valid expiry date required" }
        require(batch.quantity >= 1) { "Quantity must be at least 1" }

        val status = if (batch.status == ExpiryStatus.DISPOSED || batch.disposedDate != null) {
            ExpiryStatus.DISPOSED
        } else {
            val settings = settingsRepository.getSettings()
            calculateExpiryStatusUseCase(
                expiryDateEpochMillis = batch.expiryDate,
                isDisposed = false,
                warningThresholdDays = settings.alertDays
            )
        }

        medicineRepository.updateBatch(
            batch.copy(
                status = status,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}

class DeleteBatchUseCase(
    private val medicineRepository: MedicineRepository
) {
    suspend operator fun invoke(batchId: Long) {
        medicineRepository.deleteBatch(batchId)
    }
}
