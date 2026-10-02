package com.shivdhikshith.medsafe.data.mapper

import com.shivdhikshith.medsafe.data.local.entity.MedicineEntity
import com.shivdhikshith.medsafe.data.local.relation.MedicineWithBatchesRelation
import com.shivdhikshith.medsafe.domain.model.Medicine
import com.shivdhikshith.medsafe.domain.model.MedicineWithBatches

fun MedicineEntity.toDomain(): Medicine {
    return Medicine(
        id = id,
        name = name,
        strength = strength,
        medicineType = medicineType,
        manufacturer = manufacturer,
        barcode = barcode,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Medicine.toEntity(): MedicineEntity {
    return MedicineEntity(
        id = id,
        name = name,
        strength = strength,
        medicineType = medicineType,
        manufacturer = manufacturer,
        barcode = barcode,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun MedicineWithBatchesRelation.toDomain(): MedicineWithBatches {
    return MedicineWithBatches(
        medicine = medicine.toDomain(),
        batches = batches.map { it.toDomain() }
    )
}
