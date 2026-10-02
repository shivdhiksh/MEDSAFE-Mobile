package com.shivdhikshith.medsafe.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.shivdhikshith.medsafe.data.local.entity.BatchEntity
import com.shivdhikshith.medsafe.data.local.entity.MedicineEntity

data class MedicineWithBatchesRelation(
    @Embedded
    val medicine: MedicineEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "medicineId"
    )
    val batches: List<BatchEntity>
)
