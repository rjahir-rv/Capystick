@file:Suppress("AssignedValueIsNeverRead")

package com.capystick.notepad

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.core.designsystem.R
import com.capystick.model.Note
import com.capystick.notepad.components.CollectionSheet
import com.capystick.notepad.components.NotesContent
import com.capystick.notepad.components.NotesTopBar
import com.capystick.notepad.components.SelectionTopBar
import com.capystick.notepad.util.buildShareNotesText
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
    onNoteClick: (Int) -> Unit = {},
    onAddNoteClick: () -> Unit = {},
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
    val context = LocalContext.current
    val selectedNotes = rememberSelectedNotes(notes, selectedNoteIds)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(collectionId, collectionName, favoriteOnly) {
        viewModel.initialize(collectionId, collectionName, favoriteOnly)
    }

    LaunchedEffect(recentlyDeletedNoteIds) {
        if (recentlyDeletedNoteIds.isEmpty()) return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(
            message = if (recentlyDeletedNoteIds.size == 1) {
                "Nota enviada a la papelera"
            } else {
                "Notas enviadas a la papelera"
            },
            actionLabel = "Deshacer",
            duration = SnackbarDuration.Long,
        )

        if (result == SnackbarResult.ActionPerformed) {
            viewModel.restoreNotes(recentlyDeletedNoteIds) {
                Toast.makeText(
                    context,
                    if (recentlyDeletedNoteIds.size == 1) {
                        "Nota restaurada"
                    } else {
                        "Notas restauradas"
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
                                    "Nota enviada a la papelera"
                                } else {
                                    "Notas enviadas a la papelera"
                                },
                                actionLabel = "Deshacer",
                                duration = SnackbarDuration.Long,
                            )

                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreNotes(deletedIds) {
                                    Toast.makeText(
                                        context,
                                        if (deletedIds.size == 1) {
                                            "Nota restaurada"
                                        } else {
                                            "Notas restauradas"
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

private fun shareSelectedNotes(
    context: Context,
    selectedNotes: List<Note>,
) {
    val shareableNotes = selectedNotes.filterNot(Note::isSecure)
    val skippedCount = selectedNotes.size - shareableNotes.size

    if (shareableNotes.isEmpty()) {
        Toast.makeText(
            context,
            "Las notas bloqueadas no se pueden compartir en seleccion multiple",
            Toast.LENGTH_SHORT,
        ).show()
        return
    }

    if (skippedCount > 0) {
        Toast.makeText(
            context,
            "Se omitieron $skippedCount notas bloqueadas",
            Toast.LENGTH_SHORT,
        ).show()
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareNotesText(shareableNotes))
    }
    context.startActivity(Intent.createChooser(intent, "Compartir notas"))
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
                    isRemovingFromFavorites -> "Quitar de favoritas"
                    isRemovingFromCollection -> "Quitar de la colección"
                    else -> "Eliminar notas"
                },
            )
        },
        text = {
            Text(
                text = when {
                    isRemovingFromFavorites -> "¿Deseas quitar las $selectedCount notas de favoritas? Seguirán estando disponibles en 'Todas las notas'."
                    isRemovingFromCollection -> "¿Estás seguro de que deseas quitar las $selectedCount notas de esta colección? Seguirán estando disponibles en 'Todas las notas'."
                    else -> "¿Estás seguro de que deseas eliminar las $selectedCount notas seleccionadas?"
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(
                    text = if (isRemovingFromList) "Quitar" else "Eliminar",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}

