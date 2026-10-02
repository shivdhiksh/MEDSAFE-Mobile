package com.shivdhikshith.medsafe.data

import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import com.shivdhikshith.medsafe.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.atomic.AtomicLong

class FakePhotoRepository : PhotoRepository {
    private val idCounter = AtomicLong(1L)
    private val photosFlow = MutableStateFlow<List<MedicinePhoto>>(emptyList())

    override fun observePhotosForMedicine(medicineId: Long): Flow<List<MedicinePhoto>> {
        return photosFlow.map { list -> list.filter { it.medicineId == medicineId } }
    }

    override suspend fun getPhotosForMedicine(medicineId: Long): List<MedicinePhoto> {
        return photosFlow.value.filter { it.medicineId == medicineId }
    }

    override suspend fun insertPhoto(photo: MedicinePhoto): Long {
        val newId = idCounter.getAndIncrement()
        val saved = photo.copy(id = newId)
        photosFlow.value = photosFlow.value + saved
        return newId
    }

    override suspend fun deletePhoto(photoId: Long) {
        photosFlow.value = photosFlow.value.filter { it.id != photoId }
    }

    override suspend fun deletePhotosForMedicine(medicineId: Long) {
        photosFlow.value = photosFlow.value.filter { it.medicineId != medicineId }
    }

    override suspend fun getAllPhotos(): List<MedicinePhoto> {
        return photosFlow.value
    }
}
