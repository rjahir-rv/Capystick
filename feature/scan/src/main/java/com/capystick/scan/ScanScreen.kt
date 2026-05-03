package com.capystick.scan

import android.Manifest
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.designsystem.components.CapyTopAppBar
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    innerPadding: PaddingValues,
    onMenuClick: () -> Unit,
    onNoteSaved: (Int) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasCameraPermission by remember { 
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) 
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            CapyTopAppBar(
                title = "Escanear nota",
                onMenuClick = onMenuClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(
                start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                bottom = innerPadding.calculateBottomPadding()
            )
            .consumeWindowInsets(paddingValues)
            .imePadding()
        ) {
            if (hasCameraPermission) {
                when (val state = uiState) {
                    is ScanUiState.Idle -> {
                        CameraPreviewView(
                            onPhotoCaptured = viewModel::onPhotoCaptured
                        )
                    }
                    is ScanUiState.PhotoPreview -> {
                        PhotoPreviewScreen(
                            bitmap = state.bitmap,
                            onUsePhoto = { viewModel.onUsePhoto(state.bitmap) },
                            onRetry = viewModel::onRetry
                        )
                    }
                    is ScanUiState.Processing -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ScanUiState.TextExtracted -> {
                        TextEditorScreen(
                            initialText = state.text,
                            onSave = { title, content -> 
                                viewModel.onSaveNote(title, content) { noteId ->
                                    viewModel.onRetry()
                                    onNoteSaved(noteId)
                                }
                            },
                            onCancel = viewModel::onRetry
                        )
                    }
                    is ScanUiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                            Button(onClick = viewModel::onRetry) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Se requiere permiso de cámara")
                }
            }
        }
    }
}

@Composable
fun CameraPreviewView(onPhotoCaptured: (Bitmap) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Log.e("ScanScreen", "Binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        
        Button(
            onClick = {
                imageCapture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            val rotationDegrees = image.imageInfo.rotationDegrees
                            val finalBitmap = if (rotationDegrees != 0) {
                                val matrix = android.graphics.Matrix()
                                matrix.postRotate(rotationDegrees.toFloat())
                                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                            } else {
                                bitmap
                            }
                            image.close()
                            onPhotoCaptured(finalBitmap)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.e("ScanScreen", "Capture failed", exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text("Tomar foto")
        }
    }
}

@Composable
fun PhotoPreviewScreen(bitmap: Bitmap, onUsePhoto: () -> Unit, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Preview",
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onRetry) { Text("Reintentar") }
            Button(onClick = onUsePhoto) { Text("Usar imagen") }
        }
    }
}

@Composable
fun TextEditorScreen(initialText: String, onSave: (String, String) -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf("Nota escaneada") }
    var content by remember { mutableStateOf(initialText) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Contenido") },
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = onCancel) { Text("Cancelar") }
            Button(onClick = { onSave(title, content) }) { Text("Guardar") }
        }
    }
}
