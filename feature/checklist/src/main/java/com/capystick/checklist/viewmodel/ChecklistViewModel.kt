package com.capystick.checklist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.ChecklistContent
import com.capystick.model.ChecklistContentSerializer
import com.capystick.model.ChecklistItem
import com.capystick.model.Note
import com.capystick.model.NoteType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChecklistViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {

    private var currentNoteId: Int? = null
    private val _uiState = MutableStateFlow(ChecklistEditorUiState())
    val uiState: StateFlow<ChecklistEditorUiState> = _uiState.asStateFlow()

    fun loadNote(id: Int) {
        currentNoteId = id
        _uiState.value = ChecklistEditorUiState(isLoading = true)
        viewModelScope.launch {
            val note = noteRepository.getNoteById(id).firstOrNull()
            val loadedItems = note?.let { loadedNote ->
                ChecklistContentSerializer.fromJson(loadedNote.content).items
            }.orEmpty().ifEmpty { listOf(ChecklistItem()) }
            _uiState.update {
                it.copy(
                    note = note,
                    title = note?.title.orEmpty(),
                    items = loadedItems,
                    savedTitle = note?.title.orEmpty(),
                    savedItems = loadedItems,
                    isLoading = false,
                    noteMissing = note == null,
                )
            }
        }
    }

    fun clearChecklist(initialTitle: String = "") {
        currentNoteId = null
        _uiState.value = ChecklistEditorUiState(
            title = initialTitle,
            items = listOf(ChecklistItem()),
            savedTitle = "",
            savedItems = emptyList(),
        )
    }

    fun onItemTextChange(itemId: String, text: String) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == itemId) item.copy(text = text) else item
                },
            )
        }
    }

    fun onItemCheckedChange(itemId: String, checked: Boolean) {
        _uiState.update { state ->
            state.copy(
                items = state.items.map { item ->
                    if (item.id == itemId) item.copy(checked = checked) else item
                },
            )
        }
    }

    fun addItem(): String {
        val newItem = ChecklistItem()
        _uiState.update { state ->
            state.copy(items = state.items + newItem)
        }
        return newItem.id
    }

    fun removeItem(itemId: String) {
        _uiState.update { state ->
            val updatedItems = state.items.filterNot { it.id == itemId }
            state.copy(items = updatedItems.ifEmpty { listOf(ChecklistItem()) })
        }
    }

    fun saveChecklist(collectionId: Int? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val state = _uiState.value
            val items = state.items
                .filter { it.text.isNotBlank() }
                .ifEmpty { listOf(ChecklistItem()) }
            val note = Note(
                id = currentNoteId ?: 0,
                title = state.title.ifBlank { "Checklist sin titulo" },
                content = ChecklistContentSerializer.toJson(ChecklistContent(items)),
                timestamp = System.currentTimeMillis(),
                colorHex = 0xFFFFFFFF,
                type = NoteType.CHECKLIST,
                isFavorite = state.note?.isFavorite ?: false,
                isSecure = state.note?.isSecure ?: false,
            )
            val noteId = noteRepository.saveNote(note).toInt()
            _uiState.update {
                it.copy(
                    note = note.copy(id = noteId),
                    savedTitle = note.title,
                    savedItems = items,
                )
            }

            if (collectionId != null && currentNoteId == null) {
                collectionRepository.addNoteToCollection(noteId, collectionId)
            }

            onComplete()
        }
    }

    fun updateSecureStatus(noteId: Int, isSecure: Boolean) {
        viewModelScope.launch {
            noteRepository.updateSecureStatus(noteId, isSecure)
            _uiState.update { state ->
                state.copy(note = state.note?.copy(isSecure = isSecure))
            }
        }
    }
}

data class ChecklistEditorUiState(
    val note: Note? = null,
    val title: String = "",
    val items: List<ChecklistItem> = listOf(ChecklistItem()),
    val savedTitle: String = "",
    val savedItems: List<ChecklistItem> = emptyList(),
    val isLoading: Boolean = false,
    val noteMissing: Boolean = false,
) {
    val hasUnsavedChanges: Boolean
        get() = normalizedTitle != savedTitle.trim() || normalizedItems != normalizedSavedItems

    private val normalizedTitle: String
        get() = title.trim()

    private val normalizedItems: List<ChecklistItem>
        get() = items.filter { it.text.isNotBlank() }

    private val normalizedSavedItems: List<ChecklistItem>
        get() = savedItems.filter { it.text.isNotBlank() }
}
