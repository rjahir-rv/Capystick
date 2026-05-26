package com.capystick.notepad.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capystick.notepad.R
import com.capystick.core.designsystem.R as DesignR

@Composable
internal fun CreateNoteMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddTextNoteClick: () -> Unit,
    onAddChecklistClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .width(224.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            CreateNoteMenuItem(
                text = stringResource(R.string.note_type_text),
                iconRes = DesignR.drawable.ic_new_note,
                onClick = {
                    onExpandedChange(false)
                    onAddTextNoteClick()
                },
            )
            CreateNoteMenuItem(
                text = stringResource(R.string.note_type_checklist),
                iconRes = DesignR.drawable.ic_checklist,
                onClick = {
                    onExpandedChange(false)
                    onAddChecklistClick()
                },
            )
        }
        FloatingActionButton(
            onClick = { onExpandedChange(true) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Icon(
                painter = painterResource(id = DesignR.drawable.ic_add),
                contentDescription = stringResource(R.string.create_note_content_description),
            )
        }
    }
}

@Composable
private fun CreateNoteMenuItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier
                .height(60.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun CreateChecklistDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_checklist_title)) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.checklist_name_label)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onConfirm(title.trim()) },
            ) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Composable
internal fun DeleteNotesDialog(
    selectedCount: Int,
    isRemovingFromCollection: Boolean,
    isRemovingFromFavorites: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    val isRemovingFromList = isRemovingFromCollection || isRemovingFromFavorites
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when {
                    isRemovingFromFavorites -> stringResource(R.string.remove_from_favorites_title)
                    isRemovingFromCollection -> stringResource(R.string.remove_from_collection_title)
                    else -> stringResource(R.string.delete_notes_title)
                },
            )
        },
        text = {
            Text(
                text = when {
                    isRemovingFromFavorites -> stringResource(R.string.remove_from_favorites_message, selectedCount)
                    isRemovingFromCollection -> stringResource(R.string.remove_from_collection_message, selectedCount)
                    else -> stringResource(R.string.delete_notes_message, selectedCount)
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = if (isRemovingFromList) {
                        stringResource(R.string.remove)
                    } else {
                        stringResource(R.string.delete)
                    },
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}
