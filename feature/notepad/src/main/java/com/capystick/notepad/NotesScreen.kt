@file:Suppress("AssignedValueIsNeverRead")

package com.capystick.notepad

import android.content.res.Configuration
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.model.Note
import com.capystick.notepad.components.CollectionSheet
import com.capystick.notepad.components.NotesContent
import com.capystick.notepad.components.NotesTopBar
import com.capystick.notepad.components.SelectionTopBar
import com.capystick.notepad.util.buildShareNotesText
import com.capystick.notepad.viewmodel.NotesViewModel
import kotlinx.coroutines.launch
import com.capystick.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    collectionId: Int? = null,
    collectionName: String? = null,
    favoriteOnly: Boolean = false,
    recentlyDeletedNoteIds: Set<Int> = emptySet(),
    onRecentlyDeletedHandled: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    showNavigationIcon: Boolean = true,
    onNoteClick: (Int) -> Unit = {},
    onAddTextNoteClick: () -> Unit = {},
    onAddChecklistClick: (String) -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel(),
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val selectedNotes = rememberSelectedNotes(notes, selectedNoteIds)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCreateMenu by remember { mutableStateOf(false) }
    var showChecklistNameDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val noteMovedToTrashMessage = stringResource(R.string.note_moved_to_trash)
    val notesMovedToTrashMessage = stringResource(R.string.notes_moved_to_trash)
    val undoLabel = stringResource(R.string.undo)
    val noteRestoredMessage = stringResource(R.string.note_restored)
    val notesRestoredMessage = stringResource(R.string.notes_restored)
    val lockedNotesCannotBeSharedMessage = stringResource(R.string.locked_notes_cannot_be_shared)
    val shareNotesChooserTitle = stringResource(R.string.share_notes_chooser_title)
    
    val getLockedNotesSkippedMessage: (Int) -> String = { count ->
        context.resources.getString(R.string.locked_notes_skipped, count)
    }

    LaunchedEffect(collectionId, collectionName, favoriteOnly) {
        viewModel.initialize(collectionId, collectionName, favoriteOnly)
    }

    LaunchedEffect(recentlyDeletedNoteIds) {
        if (recentlyDeletedNoteIds.isEmpty()) return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = if (recentlyDeletedNoteIds.size == 1) {
                noteMovedToTrashMessage
            } else {
                notesMovedToTrashMessage
            },
            actionLabel = undoLabel,
            duration = SnackbarDuration.Long,
        )

        if (result == SnackbarResult.ActionPerformed) {
            viewModel.restoreNotes(recentlyDeletedNoteIds) {
                Toast.makeText(
                    context,
                    if (recentlyDeletedNoteIds.size == 1) {
                        noteRestoredMessage
                    } else {
                        notesRestoredMessage
                    },
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

        onRecentlyDeletedHandled()
    }

    BackHandler(enabled = isSelectionMode) {
        viewModel.clearSelection()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedNoteIds.size,
                    isInCollection = collectionId != null || favoriteOnly,
                    onCloseClick = { viewModel.clearSelection() },
                    onDeleteClick = { showDeleteDialog = true },
                    onShareClick = {
                        shareSelectedNotes(
                            context = context,
                            selectedNotes = selectedNotes,
                            lockedNotesCannotBeSharedMessage = lockedNotesCannotBeSharedMessage,
                            getLockedNotesSkippedMessage = getLockedNotesSkippedMessage,
                            shareNotesChooserTitle = shareNotesChooserTitle
                        )
                        viewModel.clearSelection()
                    },
                    onAddToCollectionClick = { showBottomSheet = true },
                )
            } else {
                NotesTopBar(
                    title = title.asString(),
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearchActiveChange = viewModel::onSearchActiveChange,
                    onSortOrderChange = viewModel::onSortOrderChange,
                    onMenuClick = onMenuClick,
                    showNavigationIcon = showNavigationIcon,
                )
            }
        },
    ) { scaffoldPadding ->
        val noteListContentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + 56.dp + 24.dp,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = innerPadding.calculateBottomPadding()
                ),
        ) {
            NotesContent(
                notes = notes,
                collectionId = collectionId,
                isSelectionMode = isSelectionMode,
                selectedNoteIds = selectedNoteIds,
                onNoteClick = onNoteClick,
                onNoteLongClick = viewModel::toggleSelection,
                modifier = Modifier.fillMaxSize(),
                contentPadding = noteListContentPadding,
                isLandscape = isLandscape,
            )

            if (!isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                ) {
                    DropdownMenu(
                        expanded = showCreateMenu,
                        onDismissRequest = { showCreateMenu = false },
                        modifier = Modifier
                            .width(224.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        CreateNoteMenuItem(
                            text = stringResource(R.string.note_type_text),
                            iconRes = DesignR.drawable.ic_new_note,
                            onClick = {
                                showCreateMenu = false
                                onAddTextNoteClick()
                            },
                        )
                        CreateNoteMenuItem(
                            text = stringResource(R.string.note_type_checklist),
                            iconRes = DesignR.drawable.ic_checklist,
                            onClick = {
                                showCreateMenu = false
                                showChecklistNameDialog = true
                            },
                        )
                    }
                    FloatingActionButton(
                        onClick = { showCreateMenu = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Icon(
                            painter = painterResource(id = DesignR.drawable.ic_add),
                            contentDescription = stringResource(R.string.create_note_content_description),
                        )
                    }
                }
            }
        }

        if (showChecklistNameDialog) {
            CreateChecklistDialog(
                onDismiss = { showChecklistNameDialog = false },
                onConfirm = { checklistTitle ->
                    showChecklistNameDialog = false
                    onAddChecklistClick(checklistTitle)
                },
            )
        }

        if (showBottomSheet) {
            CollectionSheet(
                collections = collections,
                onDismiss = { showBottomSheet = false },
                onCollectionSelected = viewModel::selectedNotesToCollection,
                onCreateCollection = viewModel::createCollectionAndAddSelectedNotes,
            )
        }

        if (showDeleteDialog) {
            DeleteNotesDialog(
                selectedCount = selectedNoteIds.size,
                isRemovingFromCollection = collectionId != null,
                isRemovingFromFavorites = favoriteOnly,
                onDismiss = { showDeleteDialog = false },
                onConfirmDelete = {
                    viewModel.deleteSelectedNotes { deletedIds ->
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = if (deletedIds.size == 1) {
                                    noteMovedToTrashMessage
                                } else {
                                    notesMovedToTrashMessage
                                },
                                actionLabel = undoLabel,
                                duration = SnackbarDuration.Long,
                            )

                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreNotes(deletedIds) {
                                    Toast.makeText(
                                        context,
                                        if (deletedIds.size == 1) {
                                            noteRestoredMessage
                                        } else {
                                            notesRestoredMessage
                                        },
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }
                        }
                    }
                    showDeleteDialog = false
                },
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
private fun CreateChecklistDialog(
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
private fun rememberSelectedNotes(
    notes: List<Note>,
    selectedNoteIds: Set<Int>,
): List<Note> {
    return remember(notes, selectedNoteIds) {
        notes.filter { selectedNoteIds.contains(it.id) }
    }
}

private fun shareSelectedNotes(
    context: Context,
    selectedNotes: List<Note>,
    lockedNotesCannotBeSharedMessage: String,
    getLockedNotesSkippedMessage: (Int) -> String,
    shareNotesChooserTitle: String
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


@Composable
fun DeleteNotesDialog(
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
