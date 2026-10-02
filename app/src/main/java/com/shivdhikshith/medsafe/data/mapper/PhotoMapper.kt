package com.shivdhikshith.medsafe.data.mapper

import com.shivdhikshith.medsafe.data.local.entity.PhotoEntity
import com.shivdhikshith.medsafe.domain.model.MedicinePhoto

fun PhotoEntity.toDomain(): MedicinePhoto {
    return MedicinePhoto(
        id = id,
        medicineId = medicineId,
        filePath = filePath,
        caption = caption,
        createdAt = createdAt
    )
}

fun MedicinePhoto.toEntity(): PhotoEntity {
    return PhotoEntity(
        id = id,
        medicineId = medicineId,
        filePath = filePath,
        caption = caption,
        createdAt = createdAt
    )
}
