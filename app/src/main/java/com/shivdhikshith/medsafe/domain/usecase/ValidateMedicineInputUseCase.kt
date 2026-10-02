package com.shivdhikshith.medsafe.domain.usecase

data class MedicineValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val expiryError: String? = null,
    val quantityError: String? = null
)

class ValidateMedicineInputUseCase {
    operator fun invoke(
        name: String,
        expiryDateEpochMillis: Long?,
        quantity: Int
    ): MedicineValidationResult {
        val nameError = if (name.trim().isBlank()) "Medicine name is required" else null
        val expiryError = if (expiryDateEpochMillis == null || expiryDateEpochMillis <= 0L) {
            "Valid expiry date is required"
        } else null
        val quantityError = if (quantity < 1) "Quantity must be at least 1" else null

        val isValid = nameError == null && expiryError == null && quantityError == null
        return MedicineValidationResult(
            isValid = isValid,
            nameError = nameError,
            expiryError = expiryError,
            quantityError = quantityError
        )
    }
}
