package com.capystick.domain.repository

data class NotesExportResult(
    val notes: List<NoteTextExport>,
)

data class NoteTextExport(
    val noteId: Int,
    val fileName: String,
    val content: String,
)

interface NotesExportRepository {
    suspend fun hasActiveNotes(): Boolean

    suspend fun exportActiveNotes(): NotesExportResult
}
