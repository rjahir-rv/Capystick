package com.capystick.backup

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.backup.viewmodel.BackupViewModel
import com.capystick.core.designsystem.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: BackupViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let {
            context.openOutputStream(uri)?.let { stream ->
                viewModel.exportBackup(stream)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            context.openInputStream(uri)?.let { stream ->
                viewModel.requestImport(stream)
            }
        }
    }

    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            snackbarHostState.showSnackbar(
                message = "Backup exportado correctamente ✓",
                duration = SnackbarDuration.Short,
            )
            viewModel.dismissExportSuccess()
        }
    }

    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess) {
            snackbarHostState.showSnackbar(
                message = "Notas restauradas correctamente ✓",
                duration = SnackbarDuration.Short,
            )
            viewModel.dismissImportSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            snackbarHostState.showSnackbar(
                message = err,
                duration = SnackbarDuration.Long,
            )
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Copia de seguridad",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Volver",
                            modifier = Modifier.size(28.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Gestiona una copia de seguridad de todas tus notas " +
                        "y colecciones en un archivo JSON.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(4.dp))

                BackupCard(
                    iconRes = R.drawable.ic_export_notes,
                    title = "Exportar backup",
                    description = "Guarda todas tus notas y colecciones en un archivo JSON en tu dispositivo.",
                    actionLabel = "Exportar",
                    onClick = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                            .format(Date())
                        exportLauncher.launch("capystick_backup_$timestamp.json")
                    },
                )

                BackupCard(
                    iconRes = R.drawable.ic_backup,
                    title = "Importar backup",
                    description = "Restaura tus notas desde un archivo JSON. Esto reemplazará todos los datos actuales.",
                    actionLabel = "Importar",
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                )
            }

            AnimatedVisibility(
                visible = uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (uiState.showNoNotesWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNoNotesWarning,
            title = {
                Text(
                    text = "Sin notas para respaldar",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "No tienes notas activas. Crea al menos una nota antes de exportar un backup.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissNoNotesWarning) {
                    Text("Entendido")
                }
            },
            shape = RoundedCornerShape(24.dp),
        )
    }

    if (uiState.showImportConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::dismissImportConfirmation,
            title = {
                Text(
                    text = "¿Restaurar backup?",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "Esta acción reemplazará TODAS las notas y colecciones " +
                        "actuales con las del archivo seleccionado. " +
                        "Esta operación no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmImport,
                ) {
                    Text(
                        text = "Restaurar",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissImportConfirmation) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun BackupCard(
    iconRes: Int,
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = actionLabel,
                    modifier = Modifier.padding(vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun Context.openOutputStream(uri: Uri) =
    contentResolver.openOutputStream(uri)

private fun Context.openInputStream(uri: Uri) =
    contentResolver.openInputStream(uri)
