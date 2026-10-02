package com.shivdhikshith.medsafe.domain.model

data class MedicinePhoto(
    val id: Long = 0,
    val medicineId: Long,
    val filePath: String,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
