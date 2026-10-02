package com.shivdhikshith.medsafe.core.files

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object FileUtils {

    fun createMedicineImageFile(context: Context): File {
        val imagesDir = File(context.filesDir, "medicine_photos")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        return File(imagesDir, "photo_${UUID.randomUUID()}.jpg")
    }

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val destinationFile = createMedicineImageFile(context)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }
            destinationFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
