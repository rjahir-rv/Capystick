@file:Suppress("AssignedValueIsNeverRead")

package com.capystick.notepad

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.model.Note
import com.capystick.notepad.components.CollectionSheet
import com.capystick.notepad.components.CreateChecklistDialog
import com.capystick.notepad.components.CreateNoteMenu
import com.capystick.notepad.components.DeleteNotesDialog
import com.capystick.notepad.components.NotesContent
import com.capystick.notepad.components.NotesTopBar
import com.capystick.notepad.components.SelectionTopBar
import com.capystick.notepad.util.shareSelectedNotes
import com.capystick.notepad.viewmodel.NotesViewModel
import kotlinx.coroutines.launch

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
    val appContext = context.applicationContext
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
        appContext.getString(R.string.locked_notes_skipped, count)
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
                searchQuery = searchQuery,
                isSelectionMode = isSelectionMode,
                selectedNoteIds = selectedNoteIds,
                onNoteClick = onNoteClick,
                onNoteLongClick = viewModel::toggleSelection,
                modifier = Modifier.fillMaxSize(),
                contentPadding = noteListContentPadding,
                isLandscape = isLandscape,
            )

            if (!isSelectionMode) {
                CreateNoteMenu(
                    expanded = showCreateMenu,
                    onExpandedChange = { showCreateMenu = it },
                    onAddTextNoteClick = onAddTextNoteClick,
                    onAddChecklistClick = { showChecklistNameDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                )
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
private fun rememberSelectedNotes(
    notes: List<Note>,
    selectedNoteIds: Set<Int>,
): List<Note> {
    return remember(notes, selectedNoteIds) {
        notes.filter { selectedNoteIds.contains(it.id) }
    }
}
