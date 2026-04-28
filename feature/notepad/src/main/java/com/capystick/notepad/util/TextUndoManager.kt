package com.capystick.notepad.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.mohamedrejeb.richeditor.model.RichTextState

class TextUndoManager(private val richTextState: RichTextState) {
    private val history = mutableListOf<String>()
    private var currentIndex by mutableIntStateOf(-1)

    // We use a counter to ignore the next N snapshot attempts if they were triggered by undo/redo
    private var ignoreNextSnapshots = 0

    fun saveSnapshot() {
        if (ignoreNextSnapshots > 0) {
            ignoreNextSnapshots--
            return
        }

        val currentContent = if (richTextState.annotatedString.text.isEmpty()) {
            ""
        } else {
            richTextState.toHtml()
        }

        if (currentIndex >= 0 && history[currentIndex] == currentContent) return
        if (currentIndex < history.size - 1) {
            history.subList(currentIndex + 1, history.size).clear()
        }

        history.add(currentContent)
        currentIndex++
    }

    fun undo() {
        if (canUndo) {
            ignoreNextSnapshots = 1
            currentIndex--
            richTextState.setHtml(history[currentIndex])
        }
    }

    fun redo() {
        if (canRedo) {
            ignoreNextSnapshots = 1
            currentIndex++
            richTextState.setHtml(history[currentIndex])
        }
    }

    fun clear() {
        history.clear()
        currentIndex = -1
    }

    val canUndo: Boolean get() = currentIndex > 0
    val canRedo: Boolean get() = currentIndex < history.size - 1
}