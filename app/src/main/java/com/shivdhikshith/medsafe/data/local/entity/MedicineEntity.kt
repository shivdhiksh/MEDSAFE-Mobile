package com.shivdhikshith.medsafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val strength: String,
    val medicineType: String,
    val manufacturer: String,
    val barcode: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)
