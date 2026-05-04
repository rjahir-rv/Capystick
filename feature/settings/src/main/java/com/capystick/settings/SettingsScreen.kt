package com.capystick.settings

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.core.designsystem.R
import com.capystick.designsystem.theme.ThemeOption
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
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

@Composable
private fun ThemeSelectionDialog(
    currentTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val options = buildList {
        add(ThemeOption.SYSTEM)
        add(ThemeOption.LIGHT)
        add(ThemeOption.DARK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(ThemeOption.DYNAMIC)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleccionar tema",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                options.forEach { option ->
                    ThemeOptionRow(
                        option = option,
                        isSelected = option == currentTheme,
                        onSelect = { onThemeSelected(option) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}

@Composable
private fun ThemeOptionRow(
    option: ThemeOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 200),
        label = "theme_row_bg",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = bgColor, shape = RoundedCornerShape(size = 12.dp))
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = option.label(),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (option == ThemeOption.DYNAMIC) {
                Text(
                    text = "Colores extraidos del fondo de pantalla",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun ThemeOption.label(): String = when (this) {
    ThemeOption.SYSTEM -> "Predeterminado del sistema"
    ThemeOption.LIGHT -> "Claro"
    ThemeOption.DARK -> "Oscuro"
    ThemeOption.DYNAMIC -> "Tema dinamico"
}

@Composable
private fun SettingsItem(
    icon: Painter,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
