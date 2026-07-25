package com.capystick.scan.crop

import android.graphics.Bitmap

fun Bitmap.cropTo(cropRect: NormalizedCropRect): Bitmap {
    val pixelRect = cropRect.toPixelCropRect(width, height)
    return Bitmap.createBitmap(
        this,
        pixelRect.left,
        pixelRect.top,
        pixelRect.width,
        pixelRect.height,
    )
}
