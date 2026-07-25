package com.capystick.scan.crop

import org.junit.Assert.assertEquals
import org.junit.Test

class CropGeometryTest {
    @Test
    fun toPixelCropRect_mapsNormalizedCropToBitmapPixels() {
        val cropRect = NormalizedCropRect(
            left = 0.25f,
            top = 0.1f,
            right = 0.75f,
            bottom = 0.9f,
        )

        val result = cropRect.toPixelCropRect(bitmapWidth = 1000, bitmapHeight = 500)

        assertEquals(PixelCropRect(left = 250, top = 50, width = 500, height = 400), result)
    }

    @Test
    fun toPixelCropRect_clampsOutOfBoundsCropToBitmap() {
        val cropRect = NormalizedCropRect(
            left = -0.25f,
            top = -0.1f,
            right = 1.2f,
            bottom = 1.4f,
        )

        val result = cropRect.toPixelCropRect(bitmapWidth = 800, bitmapHeight = 600)

        assertEquals(PixelCropRect(left = 0, top = 0, width = 800, height = 600), result)
    }

    @Test
    fun resize_keepsMinimumSize() {
        val cropRect = NormalizedCropRect(
            left = 0.2f,
            top = 0.2f,
            right = 0.8f,
            bottom = 0.8f,
        )

        val result = cropRect.resize(
            handle = CropHandle.TopLeft,
            deltaX = 0.55f,
            deltaY = 0.55f,
            minSize = 0.2f,
        )

        assertEquals(0.6f, result.left, 0.0001f)
        assertEquals(0.6f, result.top, 0.0001f)
        assertEquals(0.8f, result.right, 0.0001f)
        assertEquals(0.8f, result.bottom, 0.0001f)
    }

    @Test
    fun moveBy_keepsCropInsideImage() {
        val cropRect = NormalizedCropRect(
            left = 0.2f,
            top = 0.3f,
            right = 0.6f,
            bottom = 0.8f,
        )

        val result = cropRect.moveBy(deltaX = 0.8f, deltaY = -0.6f)

        assertEquals(0.6f, result.left, 0.0001f)
        assertEquals(0f, result.top, 0.0001f)
        assertEquals(1f, result.right, 0.0001f)
        assertEquals(0.5f, result.bottom, 0.0001f)
    }

    @Test
    fun calculateFitImageBounds_handlesWideImages() {
        val result = calculateFitImageBounds(
            containerWidth = 1000f,
            containerHeight = 1000f,
            imageWidth = 1600,
            imageHeight = 800,
        )

        assertEquals(0f, result.left, 0.0001f)
        assertEquals(250f, result.top, 0.0001f)
        assertEquals(1000f, result.width, 0.0001f)
        assertEquals(500f, result.height, 0.0001f)
    }

    @Test
    fun calculateFitImageBounds_handlesTallImages() {
        val result = calculateFitImageBounds(
            containerWidth = 1000f,
            containerHeight = 1000f,
            imageWidth = 800,
            imageHeight = 1600,
        )

        assertEquals(250f, result.left, 0.0001f)
        assertEquals(0f, result.top, 0.0001f)
        assertEquals(500f, result.width, 0.0001f)
        assertEquals(1000f, result.height, 0.0001f)
    }
}
