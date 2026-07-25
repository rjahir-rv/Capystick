package com.capystick.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.capystick.model.Note
import com.capystick.model.NoteType

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val colorHex: Long,
    @ColumnInfo(defaultValue = "'TEXT'")
    val type: String = NoteType.TEXT.name,
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isFavorite: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isSecure: Boolean = false,
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    colorHex = colorHex,
    type = runCatching { NoteType.valueOf(type) }.getOrDefault(NoteType.TEXT),
    isDeleted = isDeleted,
    isFavorite = isFavorite,
    isSecure = isSecure,
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    timestamp = timestamp,
    colorHex = colorHex,
    type = type.name,
    isDeleted = isDeleted,
    isFavorite = isFavorite,
    isSecure = isSecure,
)
