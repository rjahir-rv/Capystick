package com.capystick.scan

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capystick.scan.components.CropImagePreview
import com.capystick.scan.crop.NormalizedCropRect
import com.capystick.scan.crop.cropTo

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
                Text(stringResource(R.string.retry))
            }
            Button(
                onClick = { onScanPhoto(bitmap.cropTo(cropRect)) },
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.scan))
            }
        }
    }
}
