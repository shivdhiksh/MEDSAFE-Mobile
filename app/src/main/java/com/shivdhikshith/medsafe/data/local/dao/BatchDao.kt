package com.shivdhikshith.medsafe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shivdhikshith.medsafe.data.local.entity.BatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<BatchEntity>): List<Long>

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("UPDATE batches SET status = :status, disposedDate = :disposedDate, updatedAt = :updatedAt WHERE id = :batchId")
    suspend fun updateBatchStatus(batchId: Long, status: String, disposedDate: Long?, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM batches WHERE medicineId = :medicineId ORDER BY expiryDate ASC")
    fun observeBatchesForMedicine(medicineId: Long): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :batchId")
    suspend fun getBatchById(batchId: Long): BatchEntity?

    @Query("DELETE FROM batches WHERE id = :batchId")
    suspend fun deleteBatchById(batchId: Long)
}
