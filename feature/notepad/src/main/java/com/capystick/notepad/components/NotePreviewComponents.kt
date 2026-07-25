package com.capystick.notepad.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capystick.core.designsystem.R as DesignR
import com.capystick.model.ChecklistContentSerializer
import com.capystick.model.ChecklistItem
import com.capystick.model.Note
import com.capystick.model.NoteType
import com.capystick.notepad.R
import com.capystick.notepad.util.noteContentToPlainText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotePreviewTopBar(
    title: String,
    isFavorite: Boolean,
    isSecure: Boolean,
    onBack: () -> Unit,
    onToggleFavoriteClick: () -> Unit,
    onToggleSecureClick: () -> Unit,
    onAddToCollectionClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.back_content_description),
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleFavoriteClick) {
                Icon(
                    painter = painterResource(
                        id = if (isFavorite) DesignR.drawable.ic_favorite_filled else DesignR.drawable.ic_favorite
                    ),
                    contentDescription = if (isFavorite) {
                        stringResource(R.string.remove_from_favorites)
                    } else {
                        stringResource(R.string.add_to_favorites)
                    },
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            IconButton(onClick = onToggleSecureClick) {
                Icon(
                    painter = painterResource(
                        id = if (isSecure) DesignR.drawable.ic_lock else DesignR.drawable.ic_lock_open,
                    ),
                    contentDescription = if (isSecure) {
                        stringResource(R.string.remove_protection)
                    } else {
                        stringResource(R.string.protect_note)
                    },
                    tint = if (isSecure) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.ic_more_vert),
                        contentDescription = stringResource(R.string.more_options),
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.add_to_collection)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = DesignR.drawable.ic_add_collection),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onAddToCollectionClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.share)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = DesignR.drawable.ic_share),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onShareClick()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = DesignR.drawable.ic_delete),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        },
                    )
                }
            }
        },
    )
}

@Composable
internal fun NotePreviewContent(
    note: Note,
    modifier: Modifier = Modifier,
) {
    when (note.type) {
        NoteType.TEXT -> {
            val plainText = remember(note.content) {
                noteContentToPlainText(note.content)
            }
            Text(
                text = plainText,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 26.sp,
            )
        }

        NoteType.CHECKLIST -> {
            val items = remember(note.content) {
                ChecklistContentSerializer.fromJson(note.content).items
            }
            ChecklistPreviewItems(
                items = items,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            )
        }
    }
}

@Composable
internal fun ChecklistPreviewItems(
    items: List<ChecklistItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (items.isEmpty()) {
            Text(
                text = stringResource(R.string.checklist_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = null,
                    )
                    Text(
                        text = item.text.ifBlank { stringResource(R.string.item_no_text) },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
internal fun LockedNotePrompt(
    message: String,
    actionLabel: String,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = DesignR.drawable.ic_lock),
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onUnlock) {
                Text(actionLabel)
            }
        }
    }
}
