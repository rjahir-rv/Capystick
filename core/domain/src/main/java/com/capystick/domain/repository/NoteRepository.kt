package com.capystick.domain.repository

import com.capystick.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getNoteById(id: Int): Flow<Note?>
    fun getFavoriteNotes(): Flow<List<Note>>
    fun getFavoriteNoteCount(): Flow<Int>
    suspend fun saveNote(note: Note): Long
    suspend fun deleteNote(note: Note)
    suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean)
    suspend fun updateSecureStatus(noteId: Int, isSecure: Boolean)


    fun getDeletedNotes(): Flow<List<Note>>
    suspend fun softDeleteNote(noteId: Int)
    suspend fun restoreNote(noteId: Int)
    suspend fun restoreAllNotes()
    suspend fun permanentlyDeleteNote(note: Note)
    suspend fun permanentlyDeleteAllTrashed()
}
