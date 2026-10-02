package com.shivdhikshith.medsafe.domain.model

data class Batch(
    val id: Long = 0,
    val medicineId: Long,
    val batchNumber: String = "",
    val expiryDate: Long,
    val quantity: Int = 1,
    val storageLocation: String = "",
    val status: ExpiryStatus = ExpiryStatus.VALID,
    val openedDate: Long? = null,
    val disposedDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
