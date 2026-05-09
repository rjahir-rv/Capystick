package com.capystick.model

data class Note(
    val id: Int = 0,
    val title: String,
    val content: String,
    val timestamp: Long,
    val colorHex: Long,
    val type: NoteType = NoteType.TEXT,
    val isDeleted: Boolean = false,
    val isFavorite: Boolean = false,
    val isSecure: Boolean = false,
)

enum class NoteType {
    TEXT,
    CHECKLIST,
}
