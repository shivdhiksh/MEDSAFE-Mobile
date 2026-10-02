package com.shivdhikshith.medsafe.data.repository

import androidx.room.withTransaction
import com.shivdhikshith.medsafe.core.common.DefaultDispatcherProvider
import com.shivdhikshith.medsafe.core.common.DispatcherProvider
import com.shivdhikshith.medsafe.data.local.database.MedSafeDatabase
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.data.mapper.toEntity
import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MedicineRepositoryImpl(
    private val database: MedSafeDatabase,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : MedicineRepository {

    private val medicineDao = database.medicineDao()
    private val batchDao = database.batchDao()

    override fun observeAllMedicinesWithBatches(): Flow<List<MedicineWithBatches>> {
        return medicineDao.observeAllMedicinesWithBatches()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getAllMedicinesWithBatches(): List<MedicineWithBatches> =
        withContext(dispatchers.io) {
            medicineDao.getAllMedicinesWithBatches().map { it.toDomain() }
        }

    override fun observeMedicineWithBatches(medicineId: Long): Flow<MedicineWithBatches?> {
        return medicineDao.observeMedicineWithBatches(medicineId)
            .map { it?.toDomain() }
            .flowOn(dispatchers.io)
    }

    override suspend fun getMedicineWithBatches(medicineId: Long): MedicineWithBatches? =
        withContext(dispatchers.io) {
            medicineDao.getMedicineWithBatches(medicineId)?.toDomain()
        }

    override suspend fun insertMedicineWithBatch(medicine: Medicine, initialBatch: Batch): Long =
        withContext(dispatchers.io) {
            database.withTransaction {
                val medicineId = medicineDao.insertMedicine(medicine.toEntity())
                val batchWithMedicineId = initialBatch.copy(medicineId = medicineId)
                batchDao.insertBatch(batchWithMedicineId.toEntity())
                medicineId
            }
        }

    override suspend fun insertBatch(batch: Batch): Long =
        withContext(dispatchers.io) {
            batchDao.insertBatch(batch.toEntity())
        }

    override suspend fun updateMedicine(medicine: Medicine) =
        withContext(dispatchers.io) {
            medicineDao.updateMedicine(medicine.toEntity())
        }

    override suspend fun updateBatch(batch: Batch) =
        withContext(dispatchers.io) {
            batchDao.updateBatch(batch.toEntity())
        }

    override suspend fun updateBatchStatus(batchId: Long, status: ExpiryStatus, disposedDate: Long?) =
        withContext(dispatchers.io) {
            batchDao.updateBatchStatus(
                batchId = batchId,
                status = status.name,
                disposedDate = disposedDate,
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteMedicine(medicineId: Long) =
        withContext(dispatchers.io) {
            medicineDao.deleteMedicineById(medicineId)
        }

    override suspend fun deleteBatch(batchId: Long) =
        withContext(dispatchers.io) {
            batchDao.deleteBatchById(batchId)
        }
}
