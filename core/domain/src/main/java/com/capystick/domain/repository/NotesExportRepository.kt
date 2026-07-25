package com.capystick.domain.repository

data class NotesExportResult(
    val notes: List<NoteTextExport>,
)

data class NotesExportDirectoryResult(
    val exportedCount: Int,
    val skippedCount: Int,
)

data class NoteTextExport(
    val noteId: Int,
    val fileName: String,
    val content: String,
)

interface NotesExportRepository {
    suspend fun hasActiveNotes(): Boolean

    suspend fun exportActiveNotes(): NotesExportResult

    suspend fun exportActiveNotesToDirectory(directoryUriString: String): NotesExportDirectoryResult
}
