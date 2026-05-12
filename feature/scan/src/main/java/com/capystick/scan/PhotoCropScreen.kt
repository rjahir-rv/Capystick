package com.capystick.scan

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.capystick.scan.crop.CropHandle
import com.capystick.scan.crop.DisplayedImageBounds
import com.capystick.scan.crop.NormalizedCropRect
import com.capystick.scan.crop.calculateFitImageBounds
import com.capystick.scan.crop.cropTo
import com.capystick.scan.crop.moveBy
import com.capystick.scan.crop.resize
import kotlin.math.roundToInt

@Composable
fun PhotoCropScreen(
    bitmap: Bitmap,
    onScanPhoto: (Bitmap) -> Unit,
    onRetry: () -> Unit,
) {
    var cropRect by remember(bitmap) { mutableStateOf(NormalizedCropRect.default()) }

    Column(modifier = Modifier.fillMaxSize()) {
        CropImagePreview(
            bitmap = bitmap,
            cropRect = cropRect,
            onCropRectChange = { cropRect = it },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
            ) {
                Text("Reintentar")
            }
            Button(
                onClick = { onScanPhoto(bitmap.cropTo(cropRect)) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Escanear")
            }
        }
    }
}

@Composable
private fun CropImagePreview(
    bitmap: Bitmap,
    cropRect: NormalizedCropRect,
    onCropRectChange: (NormalizedCropRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val imageBounds = remember(containerSize, bitmap.width, bitmap.height) {
        calculateFitImageBounds(
            containerWidth = containerSize.width.toFloat(),
            containerHeight = containerSize.height.toFloat(),
            imageWidth = bitmap.width,
            imageHeight = bitmap.height,
        )
    }

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onSizeChanged { containerSize = it },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Foto capturada",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        CropOverlay(
            cropRect = cropRect,
            imageBounds = imageBounds,
            onCropRectChange = onCropRectChange,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun CropOverlay(
    cropRect: NormalizedCropRect,
    imageBounds: DisplayedImageBounds,
    onCropRectChange: (NormalizedCropRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cropLeft = imageBounds.left + (cropRect.left * imageBounds.width)
    val cropTop = imageBounds.top + (cropRect.top * imageBounds.height)
    val cropRight = imageBounds.left + (cropRect.right * imageBounds.width)
    val cropBottom = imageBounds.top + (cropRect.bottom * imageBounds.height)
    val cropWidth = cropRight - cropLeft
    val cropHeight = cropBottom - cropTop
    val maskColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
    val borderColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier) {
        CropMask(
            cropLeft = cropLeft,
            cropTop = cropTop,
            cropWidth = cropWidth,
            cropHeight = cropHeight,
            maskColor = maskColor,
            borderColor = borderColor,
            modifier = Modifier.fillMaxSize(),
        )
        CropDragArea(
            cropLeft = cropLeft,
            cropTop = cropTop,
            cropWidth = cropWidth,
            cropHeight = cropHeight,
            imageBounds = imageBounds,
            onDrag = { deltaX, deltaY ->
                onCropRectChange(
                    cropRect.moveBy(
                        deltaX = deltaX / imageBounds.width,
                        deltaY = deltaY / imageBounds.height,
                    ),
                )
            },
        )
        CropHandle.entries.forEach { handle ->
            CropHandleControl(
                handle = handle,
                cropLeft = cropLeft,
                cropTop = cropTop,
                cropRight = cropRight,
                cropBottom = cropBottom,
                imageBounds = imageBounds,
                onDrag = { deltaX, deltaY ->
                    onCropRectChange(
                        cropRect.resize(
                            handle = handle,
                            deltaX = deltaX / imageBounds.width,
                            deltaY = deltaY / imageBounds.height,
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun CropMask(
    cropLeft: Float,
    cropTop: Float,
    cropWidth: Float,
    cropHeight: Float,
    maskColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawRect(color = maskColor, topLeft = Offset.Zero, size = Size(size.width, cropTop))
        drawRect(
            color = maskColor,
            topLeft = Offset(0f, cropTop),
            size = Size(cropLeft, cropHeight),
        )
        drawRect(
            color = maskColor,
            topLeft = Offset(cropLeft + cropWidth, cropTop),
            size = Size(size.width - cropLeft - cropWidth, cropHeight),
        )
        drawRect(
            color = maskColor,
            topLeft = Offset(0f, cropTop + cropHeight),
            size = Size(size.width, size.height - cropTop - cropHeight),
        )
        drawRect(
            color = borderColor,
            topLeft = Offset(cropLeft, cropTop),
            size = Size(cropWidth, cropHeight),
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}

@Composable
private fun CropDragArea(
    cropLeft: Float,
    cropTop: Float,
    cropWidth: Float,
    cropHeight: Float,
    imageBounds: DisplayedImageBounds,
    onDrag: (Float, Float) -> Unit,
) {
    val density = LocalDensity.current
    val currentOnDrag by rememberUpdatedState(onDrag)
    if (imageBounds.width <= 0f || imageBounds.height <= 0f) return

    Box(
        modifier = Modifier
            .offset { IntOffset(cropLeft.roundToInt(), cropTop.roundToInt()) }
            .size(
                width = with(density) { cropWidth.toDp() },
                height = with(density) { cropHeight.toDp() },
            )
            .pointerInput(imageBounds) {
                detectDragGestures { _, dragAmount ->
                    currentOnDrag(dragAmount.x, dragAmount.y)
                }
            },
    )
}

@Composable
private fun CropHandleControl(
    handle: CropHandle,
    cropLeft: Float,
    cropTop: Float,
    cropRight: Float,
    cropBottom: Float,
    imageBounds: DisplayedImageBounds,
    onDrag: (Float, Float) -> Unit,
) {
    val density = LocalDensity.current
    val currentOnDrag by rememberUpdatedState(onDrag)
    val handleSize = 44.dp
    val visualHandleSize = 18.dp
    if (imageBounds.width <= 0f || imageBounds.height <= 0f) return

    val centerX = when (handle) {
        CropHandle.TopLeft,
        CropHandle.BottomLeft,
        -> cropLeft

        CropHandle.TopRight,
        CropHandle.BottomRight,
        -> cropRight
    }
    val centerY = when (handle) {
        CropHandle.TopLeft,
        CropHandle.TopRight,
        -> cropTop

        CropHandle.BottomLeft,
        CropHandle.BottomRight,
        -> cropBottom
    }

    Box(
        modifier = Modifier
            .offset {
                val halfHandle = with(density) { handleSize.toPx() / 2f }
                IntOffset(
                    x = (centerX - halfHandle).roundToInt(),
                    y = (centerY - halfHandle).roundToInt(),
                )
            }
            .size(handleSize)
            .pointerInput(handle, imageBounds) {
                detectDragGestures { _, dragAmount ->
                    currentOnDrag(dragAmount.x, dragAmount.y)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(visualHandleSize)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.small,
                ),
        )
    }
}
