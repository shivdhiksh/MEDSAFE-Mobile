package com.shivdhikshith.medsafe.feature.medicine

import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches

data class MedicineDetailUiState(
    val isLoading: Boolean = true,
    val medicineWithBatches: MedicineWithBatches? = null,
    val isDeleted: Boolean = false,
    val errorMessage: String? = null
)
