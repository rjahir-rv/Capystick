package com.capystick.data.repository

import com.capystick.database.dao.CollectionDao
import com.capystick.database.entities.NoteCollectionCrossRef
import com.capystick.database.entities.toDomain
import com.capystick.database.entities.toEntity
import com.capystick.domain.repository.CollectionRepository
import com.capystick.model.Collection
import com.capystick.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val collectionDao: CollectionDao
) : CollectionRepository {

    override fun getAllCollections(): Flow<List<Collection>> {
        return collectionDao.getAllCollectionsWithNotes().map { entities ->
            entities.map { 
                it.collection.toDomain().copy(noteCount = it.notes.size)
            }
        }
    }

    override fun getNotesInCollection(collectionId: Int): Flow<List<Note>> {
        return collectionDao.getCollectionWithNotes(collectionId).map { collectionWithNotes ->
            collectionWithNotes?.notes?.map { it.toDomain() } ?: emptyList()
        }
    }

    override suspend fun saveCollection(collection: Collection): Long {
        return if (collection.id == 0) {
            collectionDao.insertCollection(collection.toEntity())
        } else {
            collectionDao.updateCollection(collection.toEntity())
            collection.id.toLong()
        }
    }

    override suspend fun deleteCollection(collection: Collection) {
        collectionDao.deleteCollection(collection.toEntity())
    }

    override suspend fun addNoteToCollection(noteId: Int, collectionId: Int) {
        collectionDao.insertNoteCollectionCrossRef(NoteCollectionCrossRef(noteId, collectionId))
    }

    override suspend fun removeNoteFromCollection(noteId: Int, collectionId: Int) {
        collectionDao.deleteNoteCollectionCrossRef(noteId, collectionId)
    }
}
