package com.capystick.notepad.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.capystick.model.Note

internal fun shareSelectedNotes(
    context: Context,
    selectedNotes: List<Note>,
    lockedNotesCannotBeSharedMessage: String,
    getLockedNotesSkippedMessage: (Int) -> String,
    shareNotesChooserTitle: String,
) {
    val shareableNotes = selectedNotes.filterNot(Note::isSecure)
    val skippedCount = selectedNotes.size - shareableNotes.size

    if (shareableNotes.isEmpty()) {
        Toast.makeText(
            context,
            lockedNotesCannotBeSharedMessage,
            Toast.LENGTH_SHORT,
        ).show()
        return
    }

    if (skippedCount > 0) {
        Toast.makeText(
            context,
            getLockedNotesSkippedMessage(skippedCount),
            Toast.LENGTH_SHORT,
        ).show()
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareNotesText(shareableNotes))
    }
    context.startActivity(Intent.createChooser(intent, shareNotesChooserTitle))
}
