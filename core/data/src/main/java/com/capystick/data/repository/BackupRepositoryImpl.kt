package com.capystick.data.repository

import com.capystick.database.dao.CollectionDao
import com.capystick.database.dao.NoteDao
import com.capystick.database.db.CapystickDB
import com.capystick.database.entities.NoteCollectionCrossRef
import com.capystick.database.entities.toDomain
import com.capystick.database.entities.toEntity
import androidx.room.withTransaction
import com.capystick.domain.repository.BackupRepository
import com.capystick.model.BackupData
import com.capystick.model.NoteCollectionRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val db: CapystickDB,
    private val noteDao: NoteDao,
    private val collectionDao: CollectionDao,
) : BackupRepository {

    override suspend fun exportBackup(): BackupData = withContext(Dispatchers.IO) {
        val notes = noteDao.getAllNotesSnapshot().map { it.toDomain() }
        val collections = collectionDao.getAllCollectionsSnapshot().map { it.toDomain() }
        val refs =
            collectionDao.getAllCrossRefs().map { NoteCollectionRef(it.noteId, it.collectionId) }

        BackupData(
            notes = notes,
            collections = collections,
            noteCollectionRefs = refs,
        )
    }

    override suspend fun importBackup(backupData: BackupData): Unit = withContext(Dispatchers.IO) {
        db.withTransaction {
            // Clear all existing data
            collectionDao.deleteAllCrossRefs()
            collectionDao.deleteAllCollections()
            noteDao.deleteAllNotes()

            // Insert backup data
            backupData.notes.forEach { note ->
                noteDao.insertNote(note.toEntity())
            }
            backupData.collections.forEach { collection ->
                collectionDao.insertCollection(collection.toEntity())
            }
            backupData.noteCollectionRefs.forEach { ref ->
                collectionDao.insertNoteCollectionCrossRef(
                    NoteCollectionCrossRef(
                        noteId = ref.noteId,
                        collectionId = ref.collectionId,
                    ),
                )
            }
        }
    }

    override suspend fun hasActiveNotes(): Boolean = withContext(Dispatchers.IO) {
        noteDao.countActiveNotes() > 0
    }
}
