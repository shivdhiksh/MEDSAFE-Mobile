package com.shivdhikshith.medsafe.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.shivdhikshith.medsafe.data.local.entity.MedicineEntity
import com.shivdhikshith.medsafe.data.local.relation.MedicineWithBatchesRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {

    @Transaction
    @Query("SELECT * FROM medicines ORDER BY name COLLATE NOCASE ASC")
    fun observeAllMedicinesWithBatches(): Flow<List<MedicineWithBatchesRelation>>

    @Transaction
    @Query("SELECT * FROM medicines ORDER BY name COLLATE NOCASE ASC")
    suspend fun getAllMedicinesWithBatches(): List<MedicineWithBatchesRelation>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    fun observeMedicineWithBatches(medicineId: Long): Flow<MedicineWithBatchesRelation?>

    @Transaction
    @Query("SELECT * FROM medicines WHERE id = :medicineId")
    suspend fun getMedicineWithBatches(medicineId: Long): MedicineWithBatchesRelation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity): Long

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Query("DELETE FROM medicines WHERE id = :medicineId")
    suspend fun deleteMedicineById(medicineId: Long)
}
