package com.capystick.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.capystick.database.entities.CollectionEntity
import com.capystick.database.entities.CollectionWithNotes
import com.capystick.database.entities.NoteCollectionCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Transaction
    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollectionsWithNotes(): Flow<List<CollectionWithNotes>>

    @Transaction
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    fun getCollectionWithNotes(collectionId: Int): Flow<CollectionWithNotes?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Update
    suspend fun updateCollection(collection: CollectionEntity)

    @Delete
    suspend fun deleteCollection(collection: CollectionEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNoteCollectionCrossRef(crossRef: NoteCollectionCrossRef)

    @Query("DELETE FROM note_collection_cross_ref WHERE noteId = :noteId AND collectionId = :collectionId")
    suspend fun deleteNoteCollectionCrossRef(noteId: Int, collectionId: Int)
}
