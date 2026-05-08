package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotePreviewViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotePreviewUiState())
    val uiState: StateFlow<NotePreviewUiState> = _uiState.asStateFlow()
    private var loadNoteJob: Job? = null
    private var currentNoteId: Int? = null

    fun loadNote(id: Int) {
        currentNoteId = id
        loadNoteJob?.cancel()
        _uiState.value = NotePreviewUiState(isLoading = true)
        loadNoteJob = viewModelScope.launch {
            repository.getNoteById(id).collect {
                if (currentNoteId != id) return@collect
                _uiState.update { state ->
                    state.copy(
                        note = it,
                        isLoading = false,
                        noteMissing = it == null,
                    )
                }
            }
        }
    }

    fun deleteNote(note: Note, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.softDeleteNote(note.id)
            onComplete()
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.updateFavoriteStatus(note.id, !note.isFavorite)
        }
    }

    fun toggleSecure(note: Note) {
        viewModelScope.launch {
            repository.updateSecureStatus(note.id, !note.isSecure)
        }
    }
}

data class NotePreviewUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val noteMissing: Boolean = false,
)
