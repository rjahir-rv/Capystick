package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Collection
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotePreviewViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val collectionRepository: CollectionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotePreviewUiState())
    val uiState: StateFlow<NotePreviewUiState> = _uiState.asStateFlow()
    private var loadNoteJob: Job? = null
    private var currentNoteId: Int? = null
    val collections: StateFlow<List<Collection>> = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = emptyList(),
        )

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

    fun restoreNote(noteId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.restoreNote(noteId)
            onComplete()
        }
    }

    fun toggleFavorite(note: Note, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val isFavorite = !note.isFavorite
            repository.updateFavoriteStatus(note.id, isFavorite)
            onComplete(isFavorite)
        }
    }

    fun toggleSecure(note: Note, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val isSecure = !note.isSecure
            repository.updateSecureStatus(note.id, isSecure)
            onComplete(isSecure)
        }
    }

    fun addNoteToCollection(noteId: Int, collectionId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            collectionRepository.addNoteToCollection(noteId, collectionId)
            onComplete()
        }
    }

    fun createCollectionAndAddNote(noteId: Int, name: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val collectionId = collectionRepository.saveCollection(Collection(name = name)).toInt()
            collectionRepository.addNoteToCollection(noteId, collectionId)
            onComplete()
        }
    }
}

data class NotePreviewUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val noteMissing: Boolean = false,
)
