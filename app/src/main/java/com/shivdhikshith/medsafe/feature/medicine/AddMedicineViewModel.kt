package com.shivdhikshith.medsafe.feature.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.core.common.Resource
import com.shivdhikshith.medsafe.core.utils.DateUtils
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.usecase.AddMedicineWithBatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddMedicineViewModel(
    private val addMedicineWithBatchUseCase: AddMedicineWithBatchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicineUiState())
    val uiState: StateFlow<AddMedicineUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onStrengthChange(strength: String) {
        _uiState.update { it.copy(strength = strength) }
    }

    fun onManufacturerChange(manufacturer: String) {
        _uiState.update { it.copy(manufacturer = manufacturer) }
    }

    fun onMedicineTypeChange(type: String) {
        _uiState.update { it.copy(medicineType = type) }
    }

    fun onBarcodeChange(barcode: String) {
        _uiState.update { it.copy(barcode = barcode) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onExpiryDateChange(dateString: String) {
        _uiState.update { it.copy(expiryDateString = dateString, expiryError = null) }
    }

    fun onBatchNumberChange(batchNumber: String) {
        _uiState.update { it.copy(batchNumber = batchNumber) }
    }

    fun onQuantityChange(quantity: String) {
        _uiState.update { it.copy(quantityString = quantity, quantityError = null) }
    }

    fun onStorageLocationChange(location: String) {
        _uiState.update { it.copy(storageLocation = location) }
    }

    fun prefill(
        name: String = "",
        strength: String = "",
        manufacturer: String = "",
        medicineType: String = "",
        barcode: String = "",
        batchNumber: String = "",
        expiryDateString: String = ""
    ) {
        _uiState.update {
            it.copy(
                name = if (name.isNotBlank()) name else it.name,
                strength = if (strength.isNotBlank()) strength else it.strength,
                manufacturer = if (manufacturer.isNotBlank()) manufacturer else it.manufacturer,
                medicineType = if (medicineType.isNotBlank()) medicineType else it.medicineType,
                barcode = if (barcode.isNotBlank()) barcode else it.barcode,
                batchNumber = if (batchNumber.isNotBlank()) batchNumber else it.batchNumber,
                expiryDateString = if (expiryDateString.isNotBlank()) expiryDateString else it.expiryDateString
            )
        }
    }

    fun saveMedicine() {
        val currentState = _uiState.value
        val parsedDate = DateUtils.parseIsoDate(currentState.expiryDateString)
        val expiryMillis = parsedDate?.let { DateUtils.localDateToEpochMillis(it) } ?: 0L
        val quantity = currentState.quantityString.toIntOrNull() ?: 1

        val medicine = Medicine(
            name = currentState.name.trim(),
            strength = currentState.strength.trim(),
            manufacturer = currentState.manufacturer.trim(),
            medicineType = currentState.medicineType.trim(),
            barcode = currentState.barcode.trim(),
            notes = currentState.notes.trim()
        )

        val batch = Batch(
            medicineId = 0,
            batchNumber = currentState.batchNumber.trim(),
            expiryDate = expiryMillis,
            quantity = quantity,
            storageLocation = currentState.storageLocation.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            when (val result = addMedicineWithBatchUseCase(medicine, batch)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message,
                            nameError = if (currentState.name.isBlank()) "Medicine name is required" else null,
                            expiryError = if (expiryMillis <= 0L) "Please enter a valid date (YYYY-MM-DD)" else null,
                            quantityError = if (quantity < 1) "Quantity must be 1 or more" else null
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    class Factory(
        private val addMedicineWithBatchUseCase: AddMedicineWithBatchUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddMedicineViewModel(addMedicineWithBatchUseCase) as T
        }
    }
}
