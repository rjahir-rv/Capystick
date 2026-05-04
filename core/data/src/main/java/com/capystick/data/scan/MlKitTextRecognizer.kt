package com.capystick.data.scan

import android.graphics.Bitmap
import com.capystick.domain.scan.TextRecognizer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class MlKitTextRecognizer @Inject constructor() : TextRecognizer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(bitmap: Bitmap): Result<String> = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(Result.success(visionText.text))
            }
            .addOnFailureListener { e ->
                continuation.resume(Result.failure(e))
            }
    }
}
