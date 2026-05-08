package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotepadViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val collectionRepository: com.capystick.domain.repository.CollectionRepository
) : ViewModel() {

    private var currentNoteId: Int? = null
    private val _editorState = MutableStateFlow(NotepadEditorUiState())
    val editorState: StateFlow<NotepadEditorUiState> = _editorState.asStateFlow()

    fun loadNote(id: Int) {
        currentNoteId = id
        _editorState.value = NotepadEditorUiState(isLoading = true)
        viewModelScope.launch {
            val loadedNote = repository.getNoteById(id).firstOrNull()
            _editorState.update {
                it.copy(
                    note = loadedNote,
                    isLoading = false,
                    noteMissing = loadedNote == null,
                )
            }
        }
    }

    fun clearNotepad() {
        currentNoteId = null
        _editorState.value = NotepadEditorUiState()
    }

    fun saveNote(title: String, content: String, collectionId: Int? = null, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val note = Note(
                id = currentNoteId ?: 0,
                title = title.ifBlank { "Sin título" },
                content = content,
                timestamp = System.currentTimeMillis(), // Updating timestamp on edit
                colorHex = 0xFFFFFFFF, // TODO: Add color selection support
                isFavorite = _editorState.value.note?.isFavorite ?: false,
                isSecure = _editorState.value.note?.isSecure ?: false,
            )
            val noteId = repository.saveNote(note).toInt()
            
            if (collectionId != null && currentNoteId == null) {
                collectionRepository.addNoteToCollection(noteId, collectionId)
            }
            
            onComplete()
        }
    }

    fun updateSecureStatus(noteId: Int, isSecure: Boolean) {
        viewModelScope.launch {
            repository.updateSecureStatus(noteId, isSecure)
            _editorState.update { state ->
                state.copy(note = state.note?.copy(isSecure = isSecure))
            }
        }
    }
}

data class NotepadEditorUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val noteMissing: Boolean = false,
)
