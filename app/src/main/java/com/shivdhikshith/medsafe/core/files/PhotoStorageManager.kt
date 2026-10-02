package com.shivdhikshith.medsafe.core.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.shivdhikshith.medsafe.core.common.DefaultDispatcherProvider
import com.shivdhikshith.medsafe.core.common.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PhotoStorageManager(
    private val context: Context,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {
    private val photosDirectory: File by lazy {
        File(context.filesDir, "medicine_photos").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    suspend fun saveImageFromUri(medicineId: Long, sourceUri: Uri): String = withContext(dispatchers.io) {
        val fileName = "photo_${medicineId}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        val destinationFile = File(photosDirectory, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            FileOutputStream(destinationFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw IllegalArgumentException("Could not open input stream for URI: $sourceUri")

        destinationFile.absolutePath
    }

    suspend fun saveBitmap(medicineId: Long, bitmap: Bitmap): String = withContext(dispatchers.io) {
        val fileName = "photo_${medicineId}_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}.jpg"
        val destinationFile = File(photosDirectory, fileName)

        FileOutputStream(destinationFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }

        destinationFile.absolutePath
    }

    fun doesPhotoExist(filePath: String): Boolean {
        return File(filePath).exists()
    }

    suspend fun deletePhotoFile(filePath: String): Boolean = withContext(dispatchers.io) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
