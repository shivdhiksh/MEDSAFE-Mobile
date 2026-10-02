package com.shivdhikshith.medsafe.domain.model

data class Reminder(
    val id: Long = 0,
    val medicineId: Long,
    val title: String,
    val timeInMillis: Long,
    val recurrence: String = "DAILY", // ONCE, DAILY, WEEKDAYS
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
