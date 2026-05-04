package com.capystick.notepad.util

import android.text.Html
import com.capystick.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun noteContentToPlainText(content: String): String {
    return Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT).toString().trim()
}

fun formatNoteDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun buildShareNotesText(notes: List<Note>): String {
    return notes.joinToString(separator = "\n\n---\n\n") { note ->
        "${note.title}\n${noteContentToPlainText(note.content)}"
    }
}
