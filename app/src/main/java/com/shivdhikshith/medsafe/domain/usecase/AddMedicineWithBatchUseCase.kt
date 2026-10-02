package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.core.common.Resource
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import com.shivdhikshith.medsafe.domain.repository.SettingsRepository

class AddMedicineWithBatchUseCase(
    private val medicineRepository: MedicineRepository,
    private val settingsRepository: SettingsRepository,
    private val validateInputUseCase: ValidateMedicineInputUseCase = ValidateMedicineInputUseCase(),
    private val calculateExpiryStatusUseCase: CalculateExpiryStatusUseCase = CalculateExpiryStatusUseCase()
) {
    suspend operator fun invoke(
        medicine: Medicine,
        batch: Batch
    ): Resource<Long> {
        val validation = validateInputUseCase(
            name = medicine.name,
            expiryDateEpochMillis = batch.expiryDate,
            quantity = batch.quantity
        )

        if (!validation.isValid) {
            val errorMessage = listOfNotNull(validation.nameError, validation.expiryError, validation.quantityError)
                .joinToString(", ")
            return Resource.Error(errorMessage)
        }

        return try {
            val settings = settingsRepository.getSettings()
            val computedStatus = calculateExpiryStatusUseCase(
                expiryDateEpochMillis = batch.expiryDate,
                isDisposed = false,
                warningThresholdDays = settings.alertDays
            )
            val preparedBatch = batch.copy(status = computedStatus)
            val newMedicineId = medicineRepository.insertMedicineWithBatch(medicine, preparedBatch)
            Resource.Success(newMedicineId)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save medicine and initial batch", e)
        }
    }
}
