package com.capystick.data.repository

import com.capystick.database.dao.NoteDao
import com.capystick.database.entities.toDomain
import com.capystick.database.entities.toEntity
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        return noteDao.getAllNotes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getNoteById(id: Int): Flow<Note?> {
        return noteDao.getNoteById(id).map { it?.toDomain() }
    }

    override suspend fun saveNote(note: Note): Long {
        return noteDao.insertNote(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note.toEntity())
    }
}
