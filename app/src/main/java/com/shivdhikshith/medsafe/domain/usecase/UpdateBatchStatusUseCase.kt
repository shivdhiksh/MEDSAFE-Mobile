package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository

class UpdateBatchStatusUseCase(
    private val medicineRepository: MedicineRepository
) {
    suspend fun markDisposed(batchId: Long) {
        medicineRepository.updateBatchStatus(
            batchId = batchId,
            status = ExpiryStatus.DISPOSED,
            disposedDate = System.currentTimeMillis()
        )
    }

    suspend fun restoreBatch(batchId: Long, originalExpiryDate: Long) {
        val status = CalculateExpiryStatusUseCase()(originalExpiryDate)
        medicineRepository.updateBatchStatus(
            batchId = batchId,
            status = status,
            disposedDate = null
        )
    }
}
