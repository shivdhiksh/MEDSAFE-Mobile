package com.shivdhikshith.medsafe.feature.medicine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.usecase.AddBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.DeleteBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.DeleteMedicineUseCase
import com.shivdhikshith.medsafe.domain.usecase.GetMedicineDetailUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateBatchStatusUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateBatchUseCase
import com.shivdhikshith.medsafe.domain.usecase.UpdateMedicineUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MedicineDetailViewModel(
    private val medicineId: Long,
    private val getMedicineDetailUseCase: GetMedicineDetailUseCase,
    private val updateBatchStatusUseCase: UpdateBatchStatusUseCase,
    private val deleteMedicineUseCase: DeleteMedicineUseCase,
    private val updateMedicineUseCase: UpdateMedicineUseCase,
    private val addBatchUseCase: AddBatchUseCase,
    private val updateBatchUseCase: UpdateBatchUseCase,
    private val deleteBatchUseCase: DeleteBatchUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineDetailUiState())
    val uiState: StateFlow<MedicineDetailUiState> = _uiState.asStateFlow()

    init {
        observeMedicineDetail()
    }

    private fun observeMedicineDetail() {
        getMedicineDetailUseCase(medicineId)
            .onEach { detail ->
                _uiState.value = MedicineDetailUiState(
                    isLoading = false,
                    medicineWithBatches = detail,
                    errorMessage = if (detail == null) "Medicine not found" else null
                )
            }
            .catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Failed to load medicine"
                )
            }
            .launchIn(viewModelScope)
    }

    fun updateMedicine(
        name: String,
        strength: String,
        medicineType: String,
        manufacturer: String,
        barcode: String,
        notes: String
    ) {
        val currentMedicine = _uiState.value.medicineWithBatches?.medicine ?: return
        viewModelScope.launch {
            try {
                updateMedicineUseCase(
                    currentMedicine.copy(
                        name = name.trim(),
                        strength = strength.trim(),
                        medicineType = medicineType.trim(),
                        manufacturer = manufacturer.trim(),
                        barcode = barcode.trim(),
                        notes = notes.trim()
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to update medicine")
            }
        }
    }

    fun addBatch(
        batchNumber: String,
        expiryDate: Long,
        quantity: Int,
        storageLocation: String
    ) {
        viewModelScope.launch {
            try {
                val batch = Batch(
                    medicineId = medicineId,
                    batchNumber = batchNumber.trim(),
                    expiryDate = expiryDate,
                    quantity = quantity,
                    storageLocation = storageLocation.trim()
                )
                addBatchUseCase(batch)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to add batch")
            }
        }
    }

    fun updateBatch(batch: Batch) {
        viewModelScope.launch {
            try {
                updateBatchUseCase(batch)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to update batch")
            }
        }
    }

    fun deleteBatch(batchId: Long) {
        viewModelScope.launch {
            try {
                deleteBatchUseCase(batchId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message ?: "Failed to delete batch")
            }
        }
    }

    fun disposeBatch(batchId: Long) {
        viewModelScope.launch {
            updateBatchStatusUseCase.markDisposed(batchId)
        }
    }

    fun restoreBatch(batchId: Long, originalExpiryDate: Long) {
        viewModelScope.launch {
            updateBatchStatusUseCase.restoreBatch(batchId, originalExpiryDate)
        }
    }

    fun deleteMedicine() {
        viewModelScope.launch {
            deleteMedicineUseCase(medicineId)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }

    class Factory(
        private val medicineId: Long,
        private val getMedicineDetailUseCase: GetMedicineDetailUseCase,
        private val updateBatchStatusUseCase: UpdateBatchStatusUseCase,
        private val deleteMedicineUseCase: DeleteMedicineUseCase,
        private val updateMedicineUseCase: UpdateMedicineUseCase,
        private val addBatchUseCase: AddBatchUseCase,
        private val updateBatchUseCase: UpdateBatchUseCase,
        private val deleteBatchUseCase: DeleteBatchUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MedicineDetailViewModel(
                medicineId = medicineId,
                getMedicineDetailUseCase = getMedicineDetailUseCase,
                updateBatchStatusUseCase = updateBatchStatusUseCase,
                deleteMedicineUseCase = deleteMedicineUseCase,
                updateMedicineUseCase = updateMedicineUseCase,
                addBatchUseCase = addBatchUseCase,
                updateBatchUseCase = updateBatchUseCase,
                deleteBatchUseCase = deleteBatchUseCase
            ) as T
        }
    }
}
