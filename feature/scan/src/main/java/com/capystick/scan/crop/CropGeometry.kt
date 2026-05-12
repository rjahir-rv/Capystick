package com.capystick.scan.crop

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val DEFAULT_CROP_MARGIN = 0.08f
private const val MIN_CROP_FRACTION = 0.12f

data class NormalizedCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float = right - left
    val height: Float = bottom - top

    companion object {
        fun default(): NormalizedCropRect = NormalizedCropRect(
            left = DEFAULT_CROP_MARGIN,
            top = DEFAULT_CROP_MARGIN,
            right = 1f - DEFAULT_CROP_MARGIN,
            bottom = 1f - DEFAULT_CROP_MARGIN,
        )
    }
}

data class PixelCropRect(
    val left: Int,
    val top: Int,
    val width: Int,
    val height: Int,
)

data class DisplayedImageBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
) {
    val right: Float = left + width
    val bottom: Float = top + height
}

enum class CropHandle {
    TopLeft,
    TopRight,
    BottomLeft,
    BottomRight,
}

fun calculateFitImageBounds(
    containerWidth: Float,
    containerHeight: Float,
    imageWidth: Int,
    imageHeight: Int,
): DisplayedImageBounds {
    if (containerWidth <= 0f || containerHeight <= 0f || imageWidth <= 0 || imageHeight <= 0) {
        return DisplayedImageBounds(0f, 0f, 0f, 0f)
    }

    val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
    val containerAspectRatio = containerWidth / containerHeight
    val displayedWidth: Float
    val displayedHeight: Float

    if (imageAspectRatio > containerAspectRatio) {
        displayedWidth = containerWidth
        displayedHeight = containerWidth / imageAspectRatio
    } else {
        displayedHeight = containerHeight
        displayedWidth = containerHeight * imageAspectRatio
    }

    return DisplayedImageBounds(
        left = (containerWidth - displayedWidth) / 2f,
        top = (containerHeight - displayedHeight) / 2f,
        width = displayedWidth,
        height = displayedHeight,
    )
}

fun NormalizedCropRect.moveBy(
    deltaX: Float,
    deltaY: Float,
): NormalizedCropRect {
    val boundedDeltaX = deltaX.coerceIn(-left, 1f - right)
    val boundedDeltaY = deltaY.coerceIn(-top, 1f - bottom)
    return copy(
        left = left + boundedDeltaX,
        top = top + boundedDeltaY,
        right = right + boundedDeltaX,
        bottom = bottom + boundedDeltaY,
    )
}

fun NormalizedCropRect.resize(
    handle: CropHandle,
    deltaX: Float,
    deltaY: Float,
    minSize: Float = MIN_CROP_FRACTION,
): NormalizedCropRect {
    val minimumSize = minSize.coerceIn(0f, 1f)
    return when (handle) {
        CropHandle.TopLeft -> copy(
            left = (left + deltaX).coerceIn(0f, right - minimumSize),
            top = (top + deltaY).coerceIn(0f, bottom - minimumSize),
        )

        CropHandle.TopRight -> copy(
            right = (right + deltaX).coerceIn(left + minimumSize, 1f),
            top = (top + deltaY).coerceIn(0f, bottom - minimumSize),
        )

        CropHandle.BottomLeft -> copy(
            left = (left + deltaX).coerceIn(0f, right - minimumSize),
            bottom = (bottom + deltaY).coerceIn(top + minimumSize, 1f),
        )

        CropHandle.BottomRight -> copy(
            right = (right + deltaX).coerceIn(left + minimumSize, 1f),
            bottom = (bottom + deltaY).coerceIn(top + minimumSize, 1f),
        )
    }.normalized()
}

fun NormalizedCropRect.toPixelCropRect(
    bitmapWidth: Int,
    bitmapHeight: Int,
): PixelCropRect {
    val normalized = normalized()
    val left = (normalized.left * bitmapWidth).roundToInt().coerceIn(0, bitmapWidth - 1)
    val top = (normalized.top * bitmapHeight).roundToInt().coerceIn(0, bitmapHeight - 1)
    val right = (normalized.right * bitmapWidth).roundToInt().coerceIn(left + 1, bitmapWidth)
    val bottom = (normalized.bottom * bitmapHeight).roundToInt().coerceIn(top + 1, bitmapHeight)
    return PixelCropRect(
        left = left,
        top = top,
        width = right - left,
        height = bottom - top,
    )
}

fun NormalizedCropRect.normalized(): NormalizedCropRect {
    val clampedLeft = left.coerceIn(0f, 1f)
    val clampedTop = top.coerceIn(0f, 1f)
    val clampedRight = right.coerceIn(0f, 1f)
    val clampedBottom = bottom.coerceIn(0f, 1f)
    return NormalizedCropRect(
        left = min(clampedLeft, clampedRight),
        top = min(clampedTop, clampedBottom),
        right = max(clampedLeft, clampedRight),
        bottom = max(clampedTop, clampedBottom),
    )
}
