package com.capystick.widget

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.widget.components.WidgetConfigurationContent
import com.capystick.widget.viewmodel.WidgetSettingsViewModel

@Composable
fun WidgetConfigurationScreen(
    modifier: Modifier = Modifier,
    appWidgetId: Int,
    innerPadding: PaddingValues,
    title: String = "Configurar widget",
    onBack: () -> Unit,
    onSaved: () -> Unit = onBack,
    viewModel: WidgetSettingsViewModel = hiltViewModel()
) {
    LaunchedEffect(appWidgetId) {
        viewModel.initialize(appWidgetId)
    }

    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    val selectedCollectionId by viewModel.selectedCollectionId.collectAsStateWithLifecycle()
    val collections by viewModel.availableCollections.collectAsStateWithLifecycle()

    WidgetConfigurationContent(
        title = title,
        innerPadding = innerPadding,
        selectedMode = selectedMode,
        selectedCollectionId = selectedCollectionId,
        collections = collections,
        canSave = viewModel.canSave(collections),
        onBackClick = onBack,
        onModeSelected = viewModel::selectMode,
        onCollectionSelected = viewModel::selectCollection,
        onSaveClick = {
            viewModel.saveConfiguration(onSaved = onSaved)
        },
        modifier = modifier,
    )
}
