package com.capystick.domain.repository

import com.capystick.model.Collection
import com.capystick.model.Note
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getAllCollections(): Flow<List<Collection>>
    fun getNotesInCollection(collectionId: Int): Flow<List<Note>>
    suspend fun saveCollection(collection: Collection): Long
    suspend fun deleteCollection(collection: Collection)
    suspend fun addNoteToCollection(noteId: Int, collectionId: Int)
    suspend fun removeNoteFromCollection(noteId: Int, collectionId: Int)
}
