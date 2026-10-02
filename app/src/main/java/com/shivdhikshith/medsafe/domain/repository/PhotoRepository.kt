package com.shivdhikshith.medsafe.domain.repository

import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    fun observePhotosForMedicine(medicineId: Long): Flow<List<MedicinePhoto>>
    suspend fun getPhotosForMedicine(medicineId: Long): List<MedicinePhoto>
    suspend fun insertPhoto(photo: MedicinePhoto): Long
    suspend fun deletePhoto(photoId: Long)
    suspend fun deletePhotosForMedicine(medicineId: Long)
    suspend fun getAllPhotos(): List<MedicinePhoto>
}
