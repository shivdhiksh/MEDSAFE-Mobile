package com.shivdhikshith.medsafe.domain.model

data class MedicineWithBatches(
    val medicine: Medicine,
    val batches: List<Batch>
) {
    val totalQuantity: Int get() = batches.filter { it.status != ExpiryStatus.DISPOSED }.sumOf { it.quantity }
    val primaryStatus: ExpiryStatus
        get() {
            val activeBatches = batches.filter { it.status != ExpiryStatus.DISPOSED }
            return when {
                activeBatches.isEmpty() -> ExpiryStatus.DISPOSED
                activeBatches.any { it.status == ExpiryStatus.EXPIRED } -> ExpiryStatus.EXPIRED
                activeBatches.any { it.status == ExpiryStatus.EXPIRING_SOON } -> ExpiryStatus.EXPIRING_SOON
                else -> ExpiryStatus.VALID
            }
        }
    val nearestExpiryDate: Long?
        get() = batches
            .filter { it.status != ExpiryStatus.DISPOSED }
            .minByOrNull { it.expiryDate }?.expiryDate
}

data class BatchWithMedicine(
    val batch: Batch,
    val medicine: Medicine
)
