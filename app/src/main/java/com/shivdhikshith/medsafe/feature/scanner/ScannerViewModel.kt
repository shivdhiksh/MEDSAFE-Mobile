package com.shivdhikshith.medsafe.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shivdhikshith.medsafe.core.scanner.ExtractedPackageData
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val medicineRepository: MedicineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    fun setMode(mode: ScannerMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            scannedBarcode = null,
            matchedMedicine = null,
            extractedPackageData = null,
            isReviewDialogVisible = false
        )
    }

    fun toggleTorch() {
        _uiState.value = _uiState.value.copy(isTorchEnabled = !_uiState.value.isTorchEnabled)
    }

    fun onBarcodeDetected(barcode: String) {
        if (_uiState.value.scannedBarcode == barcode || _uiState.value.isReviewDialogVisible) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            val medicines = medicineRepository.getAllMedicinesWithBatches()
            val matched = medicines.firstOrNull { it.medicine.barcode.equals(barcode, ignoreCase = true) }

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                scannedBarcode = barcode,
                matchedMedicine = matched,
                isReviewDialogVisible = true
            )
        }
    }

    fun onOcrDetected(data: ExtractedPackageData) {
        if (_uiState.value.isReviewDialogVisible || _uiState.value.extractedPackageData != null) {
            return
        }
        _uiState.value = _uiState.value.copy(
            extractedPackageData = data,
            isReviewDialogVisible = true
        )
    }

    fun dismissReviewDialog() {
        _uiState.value = _uiState.value.copy(
            isReviewDialogVisible = false,
            scannedBarcode = null,
            matchedMedicine = null,
            extractedPackageData = null
        )
    }

    class Factory(
        private val medicineRepository: MedicineRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScannerViewModel(medicineRepository) as T
        }
    }
}
