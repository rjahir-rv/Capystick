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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capystick.designsystem.components.CapyNoteCard
import com.capystick.designsystem.components.rememberBiometricAuthenticator
import com.capystick.model.Note
import com.capystick.notepad.R
import com.capystick.notepad.util.formatNoteDate
import com.capystick.notepad.util.noteSupportingText

@Composable
internal fun NotesContent(
    notes: List<Note>,
    collectionId: Int?,
    isSelectionMode: Boolean,
    selectedNoteIds: Set<Int>,
    onNoteClick: (Int) -> Unit,
    onNoteLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
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
        contentPadding = contentPadding,
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
            text = if (isCollectionNotes) {
                stringResource(R.string.no_notes_in_collection)
            } else {
                stringResource(R.string.no_notes_yet)
            },
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
    contentPadding: PaddingValues = PaddingValues(16.dp),
) {
    val authenticator = rememberBiometricAuthenticator()
    val secureNoteTitle = stringResource(R.string.unlock_note_title)
    val authenticateToViewContentSubtitle = stringResource(R.string.authenticate_to_view_content)
    val noteNoTitle = stringResource(R.string.note_no_title)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(notes) { note ->
            val plainText = remember(note.content, note.type) {
                noteSupportingText(note)
            }
            val dateString = remember(note.timestamp) {
                formatNoteDate(note.timestamp)
            }
            val isSelected = selectedNoteIds.contains(note.id)

            CapyNoteCard(
                title = note.title.ifBlank { noteNoTitle },
                dateString = dateString,
                plainText = plainText,
                isSelected = isSelected,
                isSecure = note.isSecure,
                onClick = {
                    if (isSelectionMode) {
                        onNoteLongClick(note.id)
                    } else if (note.isSecure) {
                        if (authenticator.isDeviceSecure()) {
                            authenticator.authenticate(
                                title = secureNoteTitle,
                                subtitle = authenticateToViewContentSubtitle,
                                onSuccess = { onNoteClick(note.id) },
                                onError = { /* Optional error surface. */ },
                            )
                        } else {
                            onNoteClick(note.id)
                        }
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
