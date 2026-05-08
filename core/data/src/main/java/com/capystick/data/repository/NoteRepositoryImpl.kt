package com.capystick.data.repository

import com.capystick.database.dao.NoteDao
import com.capystick.database.entities.toDomain
import com.capystick.database.entities.toEntity
import com.capystick.data.widget.WidgetRefreshRequester
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
    private val widgetRefreshRequester: WidgetRefreshRequester,
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNoteById(id: Int): Flow<Note?> {
        return noteDao.getNoteById(id).map { it?.toDomain() }
    }

    override fun getFavoriteNotes(): Flow<List<Note>> {
        return noteDao.getFavoriteNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFavoriteNoteCount(): Flow<Int> {
        return noteDao.getFavoriteNoteCount()
    }

    override suspend fun saveNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity()).also {
            widgetRefreshRequester.requestRefresh()
        }
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
        widgetRefreshRequester.requestRefresh()
    }

    // Trash / soft-delete operations

    override fun getDeletedNotes(): Flow<List<Note>> {
        return noteDao.getDeletedNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun softDeleteNote(noteId: Int) {
        noteDao.softDeleteNote(noteId)
        widgetRefreshRequester.requestRefresh()
    }

    override suspend fun restoreNote(noteId: Int) {
        noteDao.restoreNote(noteId)
        widgetRefreshRequester.requestRefresh()
    }

    override suspend fun restoreAllNotes() {
        noteDao.restoreAllNotes()
        widgetRefreshRequester.requestRefresh()
    }

    override suspend fun permanentlyDeleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
        widgetRefreshRequester.requestRefresh()
    }

    override suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean) {
        noteDao.updateFavoriteStatus(noteId, isFavorite)
        widgetRefreshRequester.requestRefresh()
    }

    override suspend fun updateSecureStatus(noteId: Int, isSecure: Boolean) {
        noteDao.updateSecureStatus(noteId, isSecure)
        widgetRefreshRequester.requestRefresh()
    }

    override suspend fun permanentlyDeleteAllTrashed() {
        noteDao.permanentlyDeleteAllTrashed()
        widgetRefreshRequester.requestRefresh()
    }
}
