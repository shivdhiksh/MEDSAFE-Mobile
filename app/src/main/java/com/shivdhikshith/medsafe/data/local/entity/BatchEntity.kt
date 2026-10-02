package com.shivdhikshith.medsafe.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "batches",
    foreignKeys = [
        ForeignKey(
            entity = MedicineEntity::class,
            parentColumns = ["id"],
            childColumns = ["medicineId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["medicineId"]),
        Index(value = ["expiryDate"]),
        Index(value = ["status"])
    ]
)
data class BatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long,
    val batchNumber: String,
    val expiryDate: Long,
    val quantity: Int,
    val storageLocation: String,
    val status: String,
    val openedDate: Long? = null,
    val disposedDate: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
