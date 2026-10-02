package com.shivdhikshith.medsafe.data.repository

import com.shivdhikshith.medsafe.core.common.DefaultDispatcherProvider
import com.shivdhikshith.medsafe.core.common.DispatcherProvider
import com.shivdhikshith.medsafe.data.local.dao.PhotoDao
import com.shivdhikshith.medsafe.data.mapper.toDomain
import com.shivdhikshith.medsafe.data.mapper.toEntity
import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import com.shivdhikshith.medsafe.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PhotoRepositoryImpl(
    private val photoDao: PhotoDao,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : PhotoRepository {

    override fun observePhotosForMedicine(medicineId: Long): Flow<List<MedicinePhoto>> {
        return photoDao.observePhotosForMedicine(medicineId)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)
    }

    override suspend fun getPhotosForMedicine(medicineId: Long): List<MedicinePhoto> =
        withContext(dispatchers.io) {
            photoDao.getPhotosForMedicine(medicineId).map { it.toDomain() }
        }

    override suspend fun insertPhoto(photo: MedicinePhoto): Long =
        withContext(dispatchers.io) {
            photoDao.insertPhoto(photo.toEntity())
        }

    override suspend fun deletePhoto(photoId: Long) =
        withContext(dispatchers.io) {
            photoDao.deletePhotoById(photoId)
        }

    override suspend fun deletePhotosForMedicine(medicineId: Long) =
        withContext(dispatchers.io) {
            photoDao.deletePhotosForMedicine(medicineId)
        }

    override suspend fun getAllPhotos(): List<MedicinePhoto> =
        withContext(dispatchers.io) {
            photoDao.getAllPhotos().map { it.toDomain() }
        }
}
