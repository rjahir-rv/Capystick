package com.capystick.scan

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.designsystem.components.CapyTopAppBar
import com.capystick.scan.components.CameraPreview
import com.capystick.scan.viewmodel.ScanUiState
import com.capystick.scan.viewmodel.ScanViewModel

@Composable
fun ScanScreen(
    innerPadding: PaddingValues,
    onMenuClick: () -> Unit,
    showNavigationIcon: Boolean = true,
    onNoteSaved: (Int) -> Unit,
    viewModel: ScanViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var shouldOpenAppSettings by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
        shouldOpenAppSettings = !isGranted &&
            context.findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == false
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        hasCameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            CapyTopAppBar(
                title = stringResource(R.string.scan_note_title),
                onMenuClick = onMenuClick,
                showNavigationIcon = showNavigationIcon,
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding(),
                )
                .consumeWindowInsets(paddingValues)
                .imePadding(),
        ) {
            if (hasCameraPermission) {
                ScanContent(
                    uiState = uiState,
                    onPhotoCaptured = viewModel::onPhotoCaptured,
                    onUsePhoto = viewModel::onUsePhoto,
                    onRetry = viewModel::onRetry,
                    onSaveNote = { title, content ->
                        viewModel.onSaveNote(title, content) { noteId ->
                            viewModel.onRetry()
                            onNoteSaved(noteId)
                        }
                    },
                )
            } else {
                CameraPermissionDeniedContent(
                    shouldOpenAppSettings = shouldOpenAppSettings,
                    onRequestPermission = {
                        if (shouldOpenAppSettings) {
                            settingsLauncher.launch(context.appSettingsIntent())
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionDeniedContent(
    shouldOpenAppSettings: Boolean,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.camera_permission_required),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRequestPermission) {
            Text(
                text = stringResource(
                    if (shouldOpenAppSettings) {
                        R.string.open_app_settings
                    } else {
                        R.string.allow_camera_permission
                    },
                ),
            )
        }
    }
}

@Composable
private fun ScanContent(
    uiState: ScanUiState,
    onPhotoCaptured: (Bitmap) -> Unit,
    onUsePhoto: (Bitmap) -> Unit,
    onRetry: () -> Unit,
    onSaveNote: (String, String) -> Unit,
) {
    when (uiState) {
        is ScanUiState.Idle -> {
            CameraPreview(onPhotoCaptured = onPhotoCaptured)
        }

        is ScanUiState.PhotoPreview -> {
            PhotoCropScreen(
                bitmap = uiState.bitmap,
                onScanPhoto = onUsePhoto,
                onRetry = onRetry,
            )
        }

        is ScanUiState.Processing -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is ScanUiState.TextExtracted -> {
            TextEditorScreen(
                initialText = uiState.text,
                onSave = onSaveNote,
                onCancel = onRetry,
            )
        }

        is ScanUiState.Error -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.retry))
                }
            }
        }
    }
}

private fun Context.appSettingsIntent(): Intent =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", packageName, null),
    )

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
