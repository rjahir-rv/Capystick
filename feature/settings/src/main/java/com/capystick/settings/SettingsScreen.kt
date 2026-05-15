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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.designsystem.theme.ColorPaletteOption
import com.capystick.designsystem.theme.ThemeOption
import com.capystick.settings.components.PaletteSelectionDialog
import com.capystick.settings.components.SettingsItem
import com.capystick.settings.components.ThemeSelectionDialog
import com.capystick.settings.components.label
import com.capystick.settings.viewmodel.SettingsViewModel
import com.capystick.core.designsystem.R as DesignR

@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onTrashClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onWidgetsClick: () -> Unit = {},
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
                .padding(scaffoldPadding)
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SettingsItem(
                    icon = painterResource(id = DesignR.drawable.ic_dark_mode),
                    title = stringResource(R.string.settings_theme),
                    subtitle = currentTheme.label(),
                    onClick = { showThemeDialog = true },
                )
                SettingsItem(
                    icon = painterResource(id = DesignR.drawable.ic_palette),
                    title = stringResource(R.string.settings_palette),
                    subtitle = paletteSubtitle(
                        paletteOption = currentPalette,
                        themeOption = currentTheme,
                    ),
                    onClick = { showPaletteDialog = true },
                )
                SettingsItem(
                    icon = painterResource(id = DesignR.drawable.ic_backup),
                    title = stringResource(R.string.settings_backup),
                    onClick = onBackupClick,
                )
                SettingsItem(
                    icon = painterResource(id = DesignR.drawable.ic_widget_dou),
                    title = stringResource(R.string.settings_widgets),
                    subtitle = stringResource(R.string.settings_widgets_subtitle),
                    onClick = onWidgetsClick,
                )
                SettingsItem(
                    icon = painterResource(id = DesignR.drawable.ic_trash),
                    title = stringResource(R.string.settings_trash),
                    onClick = onTrashClick,
                )
                SettingsItem(
                    icon = painterResource(id = DesignR.drawable.ic_export_notes),
                    title = stringResource(R.string.settings_export_notes),
                    subtitle = stringResource(R.string.settings_export_notes_subtitle),
                    onClick = { exportLauncher.launch(null) },
                )
                SettingsItem(
                    icon = painterResource(id = DesignR.drawable.ic_info),
                    title = stringResource(R.string.settings_about),
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
                    text = stringResource(R.string.settings_no_notes_export_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.settings_no_notes_export_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissNoNotesWarning) {
                    Text(stringResource(R.string.understood))
                }
            },
            shape = RoundedCornerShape(24.dp),
        )
    }
}

@Composable
private fun paletteSubtitle(
    paletteOption: ColorPaletteOption,
    themeOption: ThemeOption,
): String {
    val baseLabel = paletteOption.label()
    return if (themeOption == ThemeOption.DYNAMIC) {
        stringResource(R.string.settings_dynamic_palette_subtitle, baseLabel)
    } else {
        baseLabel
    }
}
