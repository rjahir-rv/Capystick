package com.capystick.notepad

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.core.designsystem.R
import com.capystick.model.Note
import com.capystick.notepad.components.CollectionAssignmentSheet
import com.capystick.notepad.components.DeleteNotesDialog
import com.capystick.notepad.components.NotesContent
import com.capystick.notepad.components.NotesTopBar
import com.capystick.notepad.components.SelectionTopBar
import com.capystick.notepad.ui.buildShareNotesText
import com.capystick.notepad.viewmodel.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    collectionId: Int? = null,
    collectionName: String? = null,
    onMenuClick: () -> Unit = {},
    onNoteClick: (Int) -> Unit = {},
    onAddNoteClick: () -> Unit = {},
    viewModel: NotesViewModel = hiltViewModel(),
) {
    LaunchedEffect(collectionId, collectionName) {
        viewModel.initialize(collectionId, collectionName)
    }

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val title by viewModel.title.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isSearchActive by viewModel.isSearchActive.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val selectedNotes = rememberSelectedNotes(notes, selectedNoteIds)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isSelectionMode) {
                SelectionTopBar(
                    selectedCount = selectedNoteIds.size,
                    isInCollection = collectionId != null,
                    onCloseClick = { viewModel.clearSelection() },
                    onDeleteClick = { showDeleteDialog = true },
                    onShareClick = {
                        shareSelectedNotes(
                            context = context,
                            selectedNotes = selectedNotes,
                        )
                        viewModel.clearSelection()
                    },
                    onAddToCollectionClick = { showBottomSheet = true },
                )
            } else {
                NotesTopBar(
                    title = title,
                    isSearchActive = isSearchActive,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSearchActiveChange = viewModel::onSearchActiveChange,
                    onSortOrderChange = viewModel::onSortOrderChange,
                    onMenuClick = onMenuClick,
                )
            }
        },
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .padding(innerPadding),
        ) {
            NotesContent(
                notes = notes,
                collectionId = collectionId,
                isSelectionMode = isSelectionMode,
                selectedNoteIds = selectedNoteIds,
                onNoteClick = onNoteClick,
                onNoteLongClick = viewModel::toggleSelection,
                modifier = Modifier.fillMaxSize(),
            )

            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = onAddNoteClick,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_add),
                        contentDescription = "Crear nota",
                    )
                }
            }
        }

        if (showBottomSheet) {
            CollectionAssignmentSheet(
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
                onDismiss = { showDeleteDialog = false },
                onConfirmDelete = {
                    viewModel.deleteSelectedNotes()
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

private fun shareSelectedNotes(
    context: android.content.Context,
    selectedNotes: List<Note>,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareNotesText(selectedNotes))
    }
    context.startActivity(Intent.createChooser(intent, "Compartir notas"))
}
