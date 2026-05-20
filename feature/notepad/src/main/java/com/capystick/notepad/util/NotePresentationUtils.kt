package com.capystick.notepad.util

import android.text.Html
import com.capystick.model.ChecklistFormatter
import com.capystick.model.Note
import com.capystick.model.NoteType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun noteContentToPlainText(content: String): String {
    return Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT).toString().trim()
}

fun noteContentToPlainText(note: Note): String {
    return when (note.type) {
        NoteType.TEXT -> noteContentToPlainText(note.content)
        NoteType.CHECKLIST -> ChecklistFormatter.plainTextFromJson(note.content)
    }
}

fun noteSupportingText(
    note: Note,
    checklistProgressText: (completed: Int, total: Int) -> String,
): String {
    return when (note.type) {
        NoteType.TEXT -> noteContentToPlainText(note.content)
        NoteType.CHECKLIST -> buildString {
            val progress = ChecklistFormatter.progressFromJson(note.content)
            append(checklistProgressText(progress.completed, progress.total))
            val items = ChecklistFormatter.plainTextFromJson(note.content)
            if (items.isNotBlank()) {
                append('\n')
                append(items)
            }
        }
    }
}

fun formatNoteDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun buildShareNotesText(notes: List<Note>): String {
    return notes.joinToString(separator = "\n\n---\n\n") { note ->
        "${note.title}\n${noteContentToPlainText(note)}"
    }
}
