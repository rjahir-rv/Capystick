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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotepadViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private var currentNoteId: Int? = null
    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()

    fun loadNote(id: Int) {
        currentNoteId = id
        viewModelScope.launch {
            _note.value = repository.getNoteById(id).firstOrNull()
        }
    }

    fun clearNotepad() {
        currentNoteId = null
        _note.value = null
    }

    fun saveNote(title: String, content: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val note = Note(
                id = currentNoteId ?: 0,
                title = title.ifBlank { "Sin título" },
                content = content,
                timestamp = System.currentTimeMillis(), // Updating timestamp on edit
                colorHex = 0xFFFFFFFF // TODO: Add color selection support
            )
            repository.saveNote(note)
            onComplete()
        }
    }
}