package com.shivdhikshith.medsafe.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.shivdhikshith.medsafe.core.files.PhotoStorageManager
import com.shivdhikshith.medsafe.domain.model.MedicinePhoto
import com.shivdhikshith.medsafe.domain.repository.PhotoRepository
import kotlinx.coroutines.flow.Flow

class AddPhotoUseCase(
    private val photoRepository: PhotoRepository,
    private val photoStorageManager: PhotoStorageManager
) {
    suspend fun fromUri(medicineId: Long, uri: Uri, caption: String = ""): Long {
        val savedPath = photoStorageManager.saveImageFromUri(medicineId, uri)
        val photo = MedicinePhoto(
            medicineId = medicineId,
            filePath = savedPath,
            caption = caption.trim()
        )
        return photoRepository.insertPhoto(photo)
    }

    suspend fun fromBitmap(medicineId: Long, bitmap: Bitmap, caption: String = ""): Long {
        val savedPath = photoStorageManager.saveBitmap(medicineId, bitmap)
        val photo = MedicinePhoto(
            medicineId = medicineId,
            filePath = savedPath,
            caption = caption.trim()
        )
        return photoRepository.insertPhoto(photo)
    }
}

class ObservePhotosUseCase(
    private val photoRepository: PhotoRepository
) {
    operator fun invoke(medicineId: Long): Flow<List<MedicinePhoto>> {
        return photoRepository.observePhotosForMedicine(medicineId)
    }
}

class DeletePhotoUseCase(
    private val photoRepository: PhotoRepository,
    private val photoStorageManager: PhotoStorageManager
) {
    suspend operator fun invoke(photo: MedicinePhoto) {
        photoStorageManager.deletePhotoFile(photo.filePath)
        photoRepository.deletePhoto(photo.id)
    }
}
