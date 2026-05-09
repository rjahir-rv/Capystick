package com.capystick.data.repository

import android.text.Html
import com.capystick.database.dao.NoteDao
import com.capystick.database.entities.toDomain
import com.capystick.domain.repository.NoteTextExport
import com.capystick.domain.repository.NotesExportRepository
import com.capystick.domain.repository.NotesExportResult
import com.capystick.model.ChecklistFormatter
import com.capystick.model.Note
import com.capystick.model.NoteType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class NotesExportRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao,
) : NotesExportRepository {

    override suspend fun hasActiveNotes(): Boolean = withContext(Dispatchers.IO) {
        noteDao.countActiveNotes() > 0
    }

    override suspend fun exportActiveNotes(): NotesExportResult = withContext(Dispatchers.IO) {
        val notes = noteDao.getActiveNotesSnapshot().map { it.toDomain() }
        val exports = notes.map { note ->
            NoteTextExport(
                noteId = note.id,
                fileName = sanitizeFileName(note.title, note.id),
                content = buildTxtContent(note),
            )
        }
        NotesExportResult(notes = exports)
    }

    private fun htmlToPlainText(html: String): String =
        Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT).toString().trim()

    private fun sanitizeFileName(title: String, noteId: Int): String {
        val invalidCharsRegex = Regex("[\\\\/:*?\"<>|]")
        val collapsedWhitespaceRegex = Regex("\\s+")
        val sanitizedTitle = title
            .replace(invalidCharsRegex, "_")
            .replace("\n", " ")
            .replace("\t", " ")
            .replace(collapsedWhitespaceRegex, " ")
            .trim()
            .take(60)

        val base = if (sanitizedTitle.isBlank()) "nota_$noteId" else "${sanitizedTitle}_$noteId"
        return "$base.txt"
    }

    private fun buildTxtContent(note: Note): String {
        val visibleTitle = note.title.trim().ifBlank { "Sin titulo" }
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(note.timestamp))
        val plainText = note.toPlainText()
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        return buildString {
            append("Titulo: ")
            append(visibleTitle)
            append('\n')
            append("Fecha: ")
            append(dateString)
            append("\n\n")
            append(plainText)
        }
    }

    private fun Note.toPlainText(): String {
        return when (type) {
            NoteType.TEXT -> htmlToPlainText(content)
            NoteType.CHECKLIST -> ChecklistFormatter.plainTextFromJson(content)
        }
    }
}
