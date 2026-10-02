package com.shivdhikshith.medsafe.feature.medicine

data class AddMedicineUiState(
    // Medicine fields
    val name: String = "",
    val strength: String = "",
    val manufacturer: String = "",
    val medicineType: String = "",
    val barcode: String = "",
    val notes: String = "",

    // Initial batch fields
    val expiryDateString: String = "", // YYYY-MM-DD
    val batchNumber: String = "",
    val quantityString: String = "1",
    val storageLocation: String = "",

    // Validation & State
    val nameError: String? = null,
    val expiryError: String? = null,
    val quantityError: String? = null,
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false,
    val errorMessage: String? = null
)
