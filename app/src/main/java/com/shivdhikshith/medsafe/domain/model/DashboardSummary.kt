package com.shivdhikshith.medsafe.domain.model

data class DashboardSummary(
    val totalActive: Int = 0,
    val validCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
    val urgentBatches: List<BatchWithMedicine> = emptyList()
)
