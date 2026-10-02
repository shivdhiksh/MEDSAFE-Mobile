package com.shivdhikshith.medsafe.domain.repository

import com.shivdhikshith.medsafe.domain.model.AlertHistory
import kotlinx.coroutines.flow.Flow

interface AlertHistoryRepository {
    suspend fun recordAlert(batchId: Long, alertType: String, sentAt: Long = System.currentTimeMillis()): Long
    suspend fun hasAlertBeenSent(batchId: Long, alertType: String): Boolean
    suspend fun getAlertCount(batchId: Long, alertType: String): Int
    suspend fun getLatestAlert(batchId: Long, alertType: String): AlertHistory?
    fun observeAlertsForBatch(batchId: Long): Flow<List<AlertHistory>>
    suspend fun getAllAlerts(): List<AlertHistory>
}
