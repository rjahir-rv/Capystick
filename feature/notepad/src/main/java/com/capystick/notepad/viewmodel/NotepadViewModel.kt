package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotepadViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    fun saveNote(title: String, content: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val note = Note(
                title = title.ifBlank { "Sin título" },
                content = content,
                timestamp = System.currentTimeMillis(),
                colorHex = 0xFFFFFFFF // TODO: Add color selection support
            )
            repository.saveNote(note)
            onComplete()
        }
    }
}