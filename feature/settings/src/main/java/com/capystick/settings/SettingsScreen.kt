@file:Suppress("AssignedValueIsNeverRead")

package com.capystick.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.core.designsystem.R
import com.capystick.designsystem.theme.ColorPaletteOption
import com.capystick.designsystem.theme.ThemeOption
import com.capystick.settings.components.PaletteSelectionDialog
import com.capystick.settings.components.SettingsItem
import com.capystick.settings.components.ThemeSelectionDialog
import com.capystick.settings.components.label
import com.capystick.settings.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onTrashClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val currentTheme by viewModel.themeOption.collectAsStateWithLifecycle()
    val currentPalette by viewModel.paletteOption.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPaletteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        uri?.let(viewModel::exportNotesToDirectory)
    }

    LaunchedEffect(uiState.exportSuccessMessage) {
        uiState.exportSuccessMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short,
            )
            viewModel.dismissExportSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
            )
            viewModel.dismissError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = scaffoldPadding.calculateTopPadding() + 14.dp)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_dark_mode),
                    title = "Tema",
                    subtitle = currentTheme.label(),
                    onClick = { showThemeDialog = true },
                )
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_palette),
                    title = "Paleta",
                    subtitle = paletteSubtitle(
                        paletteOption = currentPalette,
                        themeOption = currentTheme,
                    ),
                    onClick = { showPaletteDialog = true },
                )
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_backup),
                    title = "Copia de seguridad",
                    onClick = onBackupClick,
                )
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_trash),
                    title = "Papelera",
                    onClick = onTrashClick,
                )
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_export_notes),
                    title = "Exportar notas",
                    subtitle = "Guarda todas tus notas activas como archivos .txt",
                    onClick = { exportLauncher.launch(null) },
                )
                SettingsItem(
                    icon = painterResource(id = R.drawable.ic_info),
                    title = "Acerca de",
                    onClick = {},
                )
            }

            if (uiState.isExporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { option ->
                viewModel.setTheme(option)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false },
        )
    }

    if (showPaletteDialog) {
        PaletteSelectionDialog(
            currentPalette = currentPalette,
            currentTheme = currentTheme,
            onPaletteSelected = { option ->
                viewModel.setPalette(option)
                showPaletteDialog = false
            },
            onDismiss = { showPaletteDialog = false },
        )
    }

    if (uiState.showNoNotesWarning) {
        AlertDialog(
            onDismissRequest = viewModel::dismissNoNotesWarning,
            title = {
                Text(
                    text = "Sin notas para exportar",
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = "No tienes notas activas. Crea o restaura al menos una nota antes de exportarlas.",
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
}

private fun paletteSubtitle(
    paletteOption: ColorPaletteOption,
    themeOption: ThemeOption,
): String {
    val baseLabel = paletteOption.label()
    return if (themeOption == ThemeOption.DYNAMIC) {
        "$baseLabel - Se aplica en claro/oscuro/sistema"
    } else {
        baseLabel
    }
}
