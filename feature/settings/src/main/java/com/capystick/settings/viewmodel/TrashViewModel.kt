package com.capystick.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    val deletedNotes: StateFlow<List<Note>> = noteRepository.getDeletedNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = emptyList(),
        )

    fun restoreNote(noteId: Int) {
        viewModelScope.launch {
            noteRepository.restoreNote(noteId)
        }
    }

    fun permanentlyDeleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.permanentlyDeleteNote(note)
        }
    }

    fun restoreAllNotes() {
        viewModelScope.launch {
            noteRepository.restoreAllNotes()
        }
    }

    fun permanentlyDeleteAllTrashed() {
        viewModelScope.launch {
            noteRepository.permanentlyDeleteAllTrashed()
        }
    }
}
