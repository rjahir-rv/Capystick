package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun loadNote(id: Int) {
        _uiState.value = NotePreviewUiState(isLoading = true)
        viewModelScope.launch {
            repository.getNoteById(id).collect {
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
}

data class NotePreviewUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val noteMissing: Boolean = false,
)
