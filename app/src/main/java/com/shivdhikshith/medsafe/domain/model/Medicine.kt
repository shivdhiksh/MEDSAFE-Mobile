package com.shivdhikshith.medsafe.domain.model

data class Medicine(
    val id: Long = 0,
    val name: String,
    val strength: String = "",
    val medicineType: String = "",
    val manufacturer: String = "",
    val barcode: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
