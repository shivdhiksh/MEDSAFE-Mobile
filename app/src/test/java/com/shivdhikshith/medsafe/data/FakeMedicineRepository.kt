package com.shivdhikshith.medsafe.data

import com.shivdhikshith.medsafe.domain.model.Batch
import com.shivdhikshith.medsafe.domain.model.ExpiryStatus
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches
import com.shivdhikshith.medsafe.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class FakeMedicineRepository : MedicineRepository {

    private val idCounter = AtomicLong(1L)
    private val batchIdCounter = AtomicLong(1L)
    private val medicinesFlow = MutableStateFlow<List<MedicineWithBatches>>(emptyList())

    override fun observeAllMedicinesWithBatches(): Flow<List<MedicineWithBatches>> = medicinesFlow

    override suspend fun getAllMedicinesWithBatches(): List<MedicineWithBatches> = medicinesFlow.value

    override fun observeMedicineWithBatches(medicineId: Long): Flow<MedicineWithBatches?> =
        medicinesFlow.map { list -> list.find { it.medicine.id == medicineId } }

    override suspend fun getMedicineWithBatches(medicineId: Long): MedicineWithBatches? =
        medicinesFlow.value.find { it.medicine.id == medicineId }

    override suspend fun insertMedicineWithBatch(medicine: Medicine, initialBatch: Batch): Long {
        val newMedId = idCounter.getAndIncrement()
        val newBatchId = batchIdCounter.getAndIncrement()

        val savedMed = medicine.copy(id = newMedId)
        val savedBatch = initialBatch.copy(id = newBatchId, medicineId = newMedId)

        val updatedList = medicinesFlow.value + MedicineWithBatches(
            medicine = savedMed,
            batches = listOf(savedBatch)
        )
        medicinesFlow.value = updatedList
        return newMedId
    }

    override suspend fun insertBatch(batch: Batch): Long {
        val newBatchId = batchIdCounter.getAndIncrement()
        val savedBatch = batch.copy(id = newBatchId)

        val updatedList = medicinesFlow.value.map { medWithBatches ->
            if (medWithBatches.medicine.id == batch.medicineId) {
                medWithBatches.copy(batches = medWithBatches.batches + savedBatch)
            } else {
                medWithBatches
            }
        }
        medicinesFlow.value = updatedList
        return newBatchId
    }

    override suspend fun updateMedicine(medicine: Medicine) {
        val updatedList = medicinesFlow.value.map { medWithBatches ->
            if (medWithBatches.medicine.id == medicine.id) {
                medWithBatches.copy(medicine = medicine)
            } else {
                medWithBatches
            }
        }
        medicinesFlow.value = updatedList
    }

    override suspend fun updateBatch(batch: Batch) {
        val updatedList = medicinesFlow.value.map { medWithBatches ->
            val updatedBatches = medWithBatches.batches.map { b ->
                if (b.id == batch.id) batch else b
            }
            medWithBatches.copy(batches = updatedBatches)
        }
        medicinesFlow.value = updatedList
    }

    override suspend fun updateBatchStatus(batchId: Long, status: ExpiryStatus, disposedDate: Long?) {
        val updatedList = medicinesFlow.value.map { medWithBatches ->
            val updatedBatches = medWithBatches.batches.map { batch ->
                if (batch.id == batchId) {
                    batch.copy(status = status, disposedDate = disposedDate)
                } else {
                    batch
                }
            }
            medWithBatches.copy(batches = updatedBatches)
        }
        medicinesFlow.value = updatedList
    }

    override suspend fun deleteMedicine(medicineId: Long) {
        medicinesFlow.value = medicinesFlow.value.filter { it.medicine.id != medicineId }
    }

    override suspend fun deleteBatch(batchId: Long) {
        val updatedList = medicinesFlow.value.map { medWithBatches ->
            medWithBatches.copy(batches = medWithBatches.batches.filter { it.id != batchId })
        }
        medicinesFlow.value = updatedList
    }

    fun setMedicines(list: List<MedicineWithBatches>) {
        medicinesFlow.value = list
    }
}
