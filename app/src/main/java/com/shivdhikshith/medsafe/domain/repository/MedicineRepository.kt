package com.shivdhikshith.medsafe.domain.repository

import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    fun observeAllMedicinesWithBatches(): Flow<List<MedicineWithBatches>>
    suspend fun getAllMedicinesWithBatches(): List<MedicineWithBatches>
    fun observeMedicineWithBatches(medicineId: Long): Flow<MedicineWithBatches?>
    suspend fun getMedicineWithBatches(medicineId: Long): MedicineWithBatches?
    suspend fun insertMedicineWithBatch(medicine: Medicine, initialBatch: Batch): Long
    suspend fun insertBatch(batch: Batch): Long
    suspend fun updateMedicine(medicine: Medicine)
    suspend fun updateBatch(batch: Batch)
    suspend fun updateBatchStatus(batchId: Long, status: ExpiryStatus, disposedDate: Long?)
    suspend fun deleteMedicine(medicineId: Long)
    suspend fun deleteBatch(batchId: Long)
}
