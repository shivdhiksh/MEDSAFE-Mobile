package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository

class UpdateMedicineUseCase(
    private val medicineRepository: MedicineRepository
) {
    suspend operator fun invoke(medicine: Medicine) {
        require(medicine.name.isNotBlank()) { "Medicine name cannot be blank" }
        medicineRepository.updateMedicine(medicine.copy(name = medicine.name.trim()))
    }
}
