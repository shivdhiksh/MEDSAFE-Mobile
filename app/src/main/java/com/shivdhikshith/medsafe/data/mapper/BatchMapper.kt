package com.shivdhikshith.medsafe.data.mapper

import com.shivdhikshith.medsafe.data.local.entity.BatchEntity
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus

fun BatchEntity.toDomain(): Batch {
    val expiryStatus = try {
        ExpiryStatus.valueOf(status)
    } catch (e: Exception) {
        ExpiryStatus.VALID
    }

    return Batch(
        id = id,
        medicineId = medicineId,
        batchNumber = batchNumber,
        expiryDate = expiryDate,
        quantity = quantity,
        storageLocation = storageLocation,
        status = expiryStatus,
        openedDate = openedDate,
        disposedDate = disposedDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Batch.toEntity(): BatchEntity {
    return BatchEntity(
        id = id,
        medicineId = medicineId,
        batchNumber = batchNumber,
        expiryDate = expiryDate,
        quantity = quantity,
        storageLocation = storageLocation,
        status = status.name,
        openedDate = openedDate,
        disposedDate = disposedDate,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
