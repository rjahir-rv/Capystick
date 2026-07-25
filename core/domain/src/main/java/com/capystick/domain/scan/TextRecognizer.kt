package com.capystick.domain.scan

import android.graphics.Bitmap

interface TextRecognizer {
    suspend fun extractText(bitmap: Bitmap): Result<String>
}
