package com.capystick.widget.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.domain.repository.WidgetRepository
import com.capystick.model.Collection
import com.capystick.model.WidgetConfiguration
import com.capystick.model.WidgetMode
import com.capystick.widget.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetSettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val widgetRepository: WidgetRepository,
    collectionRepository: CollectionRepository,
    noteRepository: NoteRepository,
) : ViewModel() {
    private val baseCollections = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = emptyList(),
        )

    private val _selectedMode = MutableStateFlow(WidgetMode.RECENT_NOTES)
    val selectedMode: StateFlow<WidgetMode> = _selectedMode.asStateFlow()

    private val _selectedCollectionId = MutableStateFlow<Int?>(null)
    val selectedCollectionId: StateFlow<Int?> = _selectedCollectionId.asStateFlow()

    private val _currentAppWidgetId = MutableStateFlow<Int?>(null)
    private val _isInitialized = MutableStateFlow(false)

    private val favoriteNoteCount = noteRepository.getFavoriteNoteCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = 0,
        )

    val availableCollections: StateFlow<List<Collection>> = combine(
        baseCollections,
        favoriteNoteCount,
    ) { cols, favCount ->
        if (favCount > 0) {
            listOf(
                Collection(
                    id = -1,
                    name = context.getString(R.string.widget_favorites_collection_name),
                    noteCount = favCount,
                ),
            ) + cols
        } else {
            cols
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = emptyList(),
    )

    val widgetItems: StateFlow<List<WidgetListItemUiState>> = combine(
        widgetRepository.getWidgetConfigurations(),
        availableCollections,
    ) { configurations, availableCollections ->
        configurations.map { configuration ->
            val collection = availableCollections.firstOrNull { it.id == configuration.collectionId }
            WidgetListItemUiState(
                appWidgetId = configuration.appWidgetId,
                mode = configuration.mode,
                collectionName = collection?.name ?: configuration.collectionName,
                isCollectionDeleted = configuration.mode == WidgetMode.SELECTED_COLLECTION && collection == null,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = emptyList(),
    )

    fun initialize(appWidgetId: Int) {
        if (_currentAppWidgetId.value == appWidgetId && _isInitialized.value) {
            return
        }
        _currentAppWidgetId.value = appWidgetId
        _isInitialized.value = true

        viewModelScope.launch {
            val configuration = widgetRepository.getWidgetConfiguration(appWidgetId)
            _selectedMode.value = configuration?.mode ?: WidgetMode.RECENT_NOTES
            _selectedCollectionId.value = configuration?.collectionId
        }
    }

    fun selectMode(mode: WidgetMode) {
        _selectedMode.value = mode
        if (mode == WidgetMode.RECENT_NOTES) {
            _selectedCollectionId.value = null
        }
    }

    fun selectCollection(collectionId: Int) {
        _selectedMode.value = WidgetMode.SELECTED_COLLECTION
        _selectedCollectionId.value = collectionId
    }

    fun canSave(collections: List<Collection>): Boolean = when (_selectedMode.value) {
        WidgetMode.RECENT_NOTES -> true
        WidgetMode.SELECTED_COLLECTION -> collections.isNotEmpty() && _selectedCollectionId.value != null
    }

    fun saveConfiguration(onSaved: () -> Unit) {
        val appWidgetId = _currentAppWidgetId.value ?: return
        viewModelScope.launch {
            val collection = availableCollections.value.firstOrNull { it.id == _selectedCollectionId.value }
            widgetRepository.saveWidgetConfiguration(
                WidgetConfiguration(
                    appWidgetId = appWidgetId,
                    mode = _selectedMode.value,
                    collectionId = collection?.id,
                    collectionName = collection?.name,
                ),
            )
            onSaved()
        }
    }
}

data class WidgetListItemUiState(
    val appWidgetId: Int,
    val mode: WidgetMode,
    val collectionName: String?,
    val isCollectionDeleted: Boolean,
)
