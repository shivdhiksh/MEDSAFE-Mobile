package com.shivdhikshith.medsafe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shivdhikshith.medsafe.data.local.entity.AlertHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: AlertHistoryEntity): Long

    @Query("SELECT * FROM alert_history WHERE batchId = :batchId ORDER BY sentAt DESC")
    fun observeAlertsForBatch(batchId: Long): Flow<List<AlertHistoryEntity>>

    @Query("SELECT COUNT(*) FROM alert_history WHERE batchId = :batchId AND alertType = :alertType")
    suspend fun countAlerts(batchId: Long, alertType: String): Int

    @Query("SELECT * FROM alert_history WHERE batchId = :batchId AND alertType = :alertType ORDER BY sentAt DESC LIMIT 1")
    suspend fun getLatestAlert(batchId: Long, alertType: String): AlertHistoryEntity?

    @Query("SELECT * FROM alert_history ORDER BY sentAt DESC")
    suspend fun getAllAlerts(): List<AlertHistoryEntity>

    @Query("DELETE FROM alert_history")
    suspend fun deleteAllAlerts()
}
