package com.shivdhikshith.medsafe.domain.model

data class AlertHistory(
    val id: Long = 0,
    val batchId: Long,
    val alertType: String,
    val sentAt: Long = System.currentTimeMillis()
)
