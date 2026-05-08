package com.capystick.notepad.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.capystick.model.Note
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState

@Composable
fun rememberNotepadEditorState(): NotepadEditorState {
    val richTextState = rememberRichTextState()
    return remember(richTextState) {
        NotepadEditorState(richTextState)
    }
}

@Stable
class NotepadEditorState(
    val richTextState: RichTextState,
) {
    var title by mutableStateOf("")
    var showStyleMenu by mutableStateOf(false)
    val undoManager = TextUndoManager(richTextState)

    val isNoteEmpty: Boolean
        get() = richTextState.annotatedString.text.isEmpty()

    fun reset() {
        title = ""
        showStyleMenu = false
        richTextState.setHtml("")
        undoManager.clear()
    }

    fun load(note: Note) {
        title = note.title
        showStyleMenu = false
        richTextState.setHtml(note.content)
        undoManager.clear()
    }
}
