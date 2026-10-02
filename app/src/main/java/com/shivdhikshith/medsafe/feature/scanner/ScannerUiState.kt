package com.shivdhikshith.medsafe.feature.scanner

import com.shivdhikshith.medsafe.core.scanner.ExtractedPackageData
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches

enum class ScannerMode {
    BARCODE,
    OCR
}

data class ScannerUiState(
    val mode: ScannerMode = ScannerMode.BARCODE,
    val isTorchEnabled: Boolean = false,
    val isProcessing: Boolean = false,
    val scannedBarcode: String? = null,
    val matchedMedicine: MedicineWithBatches? = null,
    val extractedPackageData: ExtractedPackageData? = null,
    val isReviewDialogVisible: Boolean = false,
    val errorMessage: String? = null
)
