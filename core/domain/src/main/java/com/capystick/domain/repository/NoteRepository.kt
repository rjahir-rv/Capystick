package com.capystick.domain.repository

import com.capystick.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteById(id: Int): Flow<Note?>
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(note: Note)
}
