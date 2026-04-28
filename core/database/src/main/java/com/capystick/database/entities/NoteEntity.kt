package com.capystick.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.capystick.model.Note

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val colorHex: Long
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    colorHex = colorHex
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    colorHex = colorHex
)
