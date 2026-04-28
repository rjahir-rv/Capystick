package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotePreviewViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()

    fun loadNote(id: Int) {
        viewModelScope.launch {
            repository.getNoteById(id).collect {
                _note.value = it
            }
        }
    }

    fun deleteNote(note: Note, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteNote(note)
            onComplete()
        }
    }
}
