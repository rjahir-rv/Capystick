package com.capystick.notepad.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.capystick.designsystem.components.CapyNoteCard
import com.capystick.model.Note
import com.capystick.notepad.ui.formatNoteDate
import com.capystick.notepad.ui.noteContentToPlainText

@Composable
internal fun NotesContent(
    notes: List<Note>,
    collectionId: Int?,
    isSelectionMode: Boolean,
    selectedNoteIds: Set<Int>,
    onNoteClick: (Int) -> Unit,
    onNoteLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (notes.isEmpty()) {
        EmptyNotesState(
            isCollectionNotes = collectionId != null,
            modifier = modifier,
        )
        return
    }

    NotesList(
        notes = notes,
        isSelectionMode = isSelectionMode,
        selectedNoteIds = selectedNoteIds,
        onNoteClick = onNoteClick,
        onNoteLongClick = onNoteLongClick,
        modifier = modifier,
    )
}

@Composable
private fun EmptyNotesState(
    isCollectionNotes: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isCollectionNotes) "Aun no hay notas en esta coleccion" else "No hay notas aún",
        )
    }
}

@Composable
internal fun NotesList(
    notes: List<Note>,
    isSelectionMode: Boolean,
    selectedNoteIds: Set<Int>,
    onNoteClick: (Int) -> Unit,
    onNoteLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(notes) { note ->
            val plainText = remember(note.content) {
                noteContentToPlainText(note.content)
            }
            val dateString = remember(note.timestamp) {
                formatNoteDate(note.timestamp)
            }
            val isSelected = selectedNoteIds.contains(note.id)

            CapyNoteCard(
                title = note.title,
                dateString = dateString,
                plainText = plainText,
                isSelected = isSelected,
                onClick = {
                    if (isSelectionMode) {
                        onNoteLongClick(note.id)
                    } else {
                        onNoteClick(note.id)
                    }
                },
                onLongClick = {
                    onNoteLongClick(note.id)
                },
            )
        }
    }
}
