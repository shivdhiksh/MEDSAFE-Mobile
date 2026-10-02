package com.shivdhikshith.medsafe.domain.usecase

import com.shivdhikshith.medsafe.domain.repository.MedicineRepository

class DeleteMedicineUseCase(
    private val medicineRepository: MedicineRepository
) {
    suspend operator fun invoke(medicineId: Long) {
        medicineRepository.deleteMedicine(medicineId)
    }
}
