package com.shivdhikshith.medsafe.data.repository

import com.shivdhikshith.medsafe.core.common.DefaultDispatcherProvider
import com.shivdhikshith.medsafe.core.common.DispatcherProvider
import com.shivdhikshith.medsafe.data.local.dao.AlertHistoryDao
import com.shivdhikshith.medsafe.data.local.entity.AlertHistoryEntity
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.domain.model.AlertHistory
import com.shivdhikshith.medsafe.domain.repository.AlertHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AlertHistoryRepositoryImpl(
    private val alertHistoryDao: AlertHistoryDao,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AlertHistoryRepository {

    override suspend fun recordAlert(batchId: Long, alertType: String, sentAt: Long): Long =
        withContext(dispatchers.io) {
            alertHistoryDao.insertAlert(
                AlertHistoryEntity(
                    batchId = batchId,
                    alertType = alertType,
                    sentAt = sentAt
                )
            )
        }

    override suspend fun hasAlertBeenSent(batchId: Long, alertType: String): Boolean =
        withContext(dispatchers.io) {
            alertHistoryDao.countAlerts(batchId, alertType) > 0
        }

    override suspend fun getAlertCount(batchId: Long, alertType: String): Int =
        withContext(dispatchers.io) {
            alertHistoryDao.countAlerts(batchId, alertType)
        }

    override suspend fun getLatestAlert(batchId: Long, alertType: String): AlertHistory? =
        withContext(dispatchers.io) {
            alertHistoryDao.getLatestAlert(batchId, alertType)?.toDomain()
        }

    override fun observeAlertsForBatch(batchId: Long): Flow<List<AlertHistory>> {
        return alertHistoryDao.observeAlertsForBatch(batchId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getAllAlerts(): List<AlertHistory> = withContext(dispatchers.io) {
        alertHistoryDao.getAllAlerts().map { it.toDomain() }
    }
}
