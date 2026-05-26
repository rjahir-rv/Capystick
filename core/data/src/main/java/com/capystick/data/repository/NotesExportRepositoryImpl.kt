package com.capystick.data.repository

import android.content.Context
import android.net.Uri
import android.text.Html
import androidx.documentfile.provider.DocumentFile
import com.capystick.core.data.R
import com.capystick.database.dao.NoteDao
import com.capystick.database.entities.toDomain
import com.capystick.domain.repository.NotesExportDirectoryResult
import com.capystick.domain.repository.NoteTextExport
import com.capystick.domain.repository.NotesExportRepository
import com.capystick.domain.repository.NotesExportResult
import com.capystick.model.ChecklistFormatter
import com.capystick.model.Note
import com.capystick.model.NoteType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class NotesExportRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
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

    override suspend fun exportActiveNotesToDirectory(
        directoryUriString: String,
    ): NotesExportDirectoryResult = withContext(Dispatchers.IO) {
        val notes = exportActiveNotes().notes
        val root = DocumentFile.fromTreeUri(context, Uri.parse(directoryUriString))
            ?: throw IllegalStateException(context.getString(R.string.open_selected_folder_error))

        var exportedCount = 0
        var skippedCount = 0
        notes.forEach { noteExport ->
            try {
                val file = root.createFile("text/plain", noteExport.fileName.removeSuffix(".txt"))
                val fileUri = file?.uri ?: throw IllegalStateException(context.getString(R.string.create_file_error))
                context.contentResolver.openOutputStream(fileUri)?.use { output ->
                    output.write(noteExport.content.toByteArray(Charsets.UTF_8))
                } ?: throw IllegalStateException(context.getString(R.string.open_output_stream_error))
                exportedCount++
            } catch (_: Exception) {
                skippedCount++
            }
        }

        NotesExportDirectoryResult(
            exportedCount = exportedCount,
            skippedCount = skippedCount,
        )
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

        val base = if (sanitizedTitle.isBlank()) {
            context.getString(R.string.export_note_file_name_fallback, noteId)
        } else {
            "${sanitizedTitle}_$noteId"
        }
        return "$base.txt"
    }

    private fun buildTxtContent(note: Note): String {
        val visibleTitle = note.title.trim().ifBlank { context.getString(R.string.export_note_title_fallback) }
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            .format(Date(note.timestamp))
        val plainText = note.toPlainText()
            .replace("\r\n", "\n")
            .replace("\r", "\n")

        return buildString {
            append(context.getString(R.string.export_note_title_label))
            append(visibleTitle)
            append('\n')
            append(context.getString(R.string.export_note_date_label))
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
