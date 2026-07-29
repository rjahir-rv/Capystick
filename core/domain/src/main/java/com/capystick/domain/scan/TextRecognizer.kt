package com.capystick.domain.scan

import android.graphics.Bitmap

/** Extracts text from images and owns the resources used by the recognizer. */
interface TextRecognizer : AutoCloseable {
    suspend fun extractText(bitmap: Bitmap): Result<String>
}
