package com.capystick.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.capystick.database.entities.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY timestamp DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: Int): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteNote(id: Int)

    @Query("UPDATE notes SET isDeleted = 0 WHERE id = :id")
    suspend fun restoreNote(id: Int)

    @Query("UPDATE notes SET isDeleted = 0 WHERE isDeleted = 1")
    suspend fun restoreAllNotes()

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun permanentlyDeleteAllTrashed()

    // ── Backup / restore ─────────────────────────────────────────────────────

    /** Returns ALL notes (including soft-deleted) as a one-shot list for backup export. */
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    suspend fun getAllNotesSnapshot(): List<NoteEntity>

    /** Counts notes that are NOT soft-deleted, used to guard empty-backup exports. */
    @Query("SELECT COUNT(*) FROM notes WHERE isDeleted = 0")
    suspend fun countActiveNotes(): Int

    /** Deletes every row in the notes table — called during backup import (replace strategy). */
    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()
}
