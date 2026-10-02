package com.shivdhikshith.medsafe.core.scanner

import androidx.annotation.OptIn
import android.graphics.Bitmap
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionManager {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    fun processImageProxy(
        imageProxy: ImageProxy,
        onSuccess: (ExtractedPackageData) -> Unit,
        onComplete: () -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extracted = OcrPackageParser.parse(visionText.text)
                    if (extracted.medicineName.isNotBlank() || extracted.batchNumber.isNotBlank() || extracted.expiryDateString.isNotBlank()) {
                        onSuccess(extracted)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                    onComplete()
                }
        } else {
            imageProxy.close()
            onComplete()
        }
    }

    fun processBitmap(
        bitmap: Bitmap,
        onSuccess: (ExtractedPackageData) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extracted = OcrPackageParser.parse(visionText.text)
                onSuccess(extracted)
            }
            .addOnFailureListener(onFailure)
    }
}
