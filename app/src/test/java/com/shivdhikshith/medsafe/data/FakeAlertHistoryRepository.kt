package com.shivdhikshith.medsafe.data

import com.shivdhikshith.medsafe.domain.model.AlertHistory
import com.shivdhikshith.medsafe.domain.repository.AlertHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class FakeAlertHistoryRepository : AlertHistoryRepository {

    private val idCounter = AtomicLong(1L)
    val alerts = mutableListOf<AlertHistory>()
    private val alertsFlow = MutableStateFlow<List<AlertHistory>>(emptyList())

    override suspend fun recordAlert(batchId: Long, alertType: String, sentAt: Long): Long {
        val id = idCounter.getAndIncrement()
        val entry = AlertHistory(id = id, batchId = batchId, alertType = alertType, sentAt = sentAt)
        alerts.add(entry)
        alertsFlow.value = alerts.toList()
        return id
    }

    override suspend fun hasAlertBeenSent(batchId: Long, alertType: String): Boolean {
        return alerts.any { it.batchId == batchId && it.alertType == alertType }
    }

    override suspend fun getAlertCount(batchId: Long, alertType: String): Int {
        return alerts.count { it.batchId == batchId && it.alertType == alertType }
    }

    override suspend fun getLatestAlert(batchId: Long, alertType: String): AlertHistory? {
        return alerts.filter { it.batchId == batchId && it.alertType == alertType }
            .maxByOrNull { it.sentAt }
    }

    override fun observeAlertsForBatch(batchId: Long): Flow<List<AlertHistory>> {
        return alertsFlow.map { list -> list.filter { it.batchId == batchId } }
    }

    override suspend fun getAllAlerts(): List<AlertHistory> {
        return alerts.toList()
    }
}
