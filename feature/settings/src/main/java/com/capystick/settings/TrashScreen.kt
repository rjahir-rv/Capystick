@file:Suppress("AssignedValueIsNeverRead")

package com.capystick.settings

import android.text.Html
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.core.designsystem.R
import com.capystick.designsystem.components.CapyNoteCard
import com.capystick.model.Note
import com.capystick.settings.viewmodel.TrashViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrashViewModel = hiltViewModel(),
) {
    val deletedNotes by viewModel.deletedNotes.collectAsStateWithLifecycle()

    var showRestoreAllDialog by remember { mutableStateOf(false) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TrashTopAppBar(
                hasNotes = deletedNotes.isNotEmpty(),
                onBack = onBack,
                onRestoreAll = { showRestoreAllDialog = true },
                onDeleteAll = { showDeleteAllDialog = true },
            )
        },
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            AnimatedVisibility(
                visible = deletedNotes.isEmpty(),
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                modifier = Modifier.align(Alignment.Center),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "La papelera está vacía",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Notes list
            AnimatedVisibility(
                visible = deletedNotes.isNotEmpty(),
                enter = fadeIn(animationSpec = tween( durationMillis = 300)),
                exit = fadeOut(animationSpec = tween( durationMillis = 300)),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(items = deletedNotes, key = { it.id }) { note ->
                        val plainText = remember(note.content, note.isSecure) {
                            if (note.isSecure) {
                                ""
                            } else {
                                Html.fromHtml(note.content, Html.FROM_HTML_MODE_COMPACT)
                                    .toString().trim()
                            }
                        }
                        val dateString = remember(note.timestamp) {
                            SimpleDateFormat("MMM dd", Locale.getDefault())
                                .format(Date(note.timestamp))
                        }
                        TrashNoteCard(
                            note = note,
                            plainText = plainText,
                            dateString = dateString,
                            onRestore = { viewModel.restoreNote(note.id) },
                            onPermanentDelete = { noteToDelete = note },
                        )
                    }
                }
            }
        }
    }


    if (showRestoreAllDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreAllDialog = false },
            title = { Text("Restaurar todo") },
            text = { Text("¿Deseas restaurar todas las notas de la papelera?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.restoreAllNotes()
                    showRestoreAllDialog = false
                }) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreAllDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("Vaciar papelera") },
            text = { Text("Esta acción eliminará permanentemente todas las notas de la papelera y no se podrá deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.permanentlyDeleteAllTrashed()
                    showDeleteAllDialog = false
                }) {
                    Text("Eliminar todo", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text("Cancelar")
                }
            },
        )
    }

    val noteBeingDeleted = noteToDelete
    if (noteBeingDeleted != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Eliminar nota") },
            text = {
                Text(
                    "¿Deseas eliminar permanentemente \"${noteBeingDeleted.title}\"? " +
                        "Esta acción no se puede deshacer.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.permanentlyDeleteNote(noteBeingDeleted)
                    noteToDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopAppBar(
    hasNotes: Boolean,
    onBack: () -> Unit,
    onRestoreAll: () -> Unit,
    onDeleteAll: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = "Papelera",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Volver",
                    modifier = Modifier.size(28.dp),
                )
            }
        },
        actions = {
            if (hasNotes) {
                // Restaurar todas
                IconButton(onClick = onRestoreAll) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_restore),
                        contentDescription = "Restaurar todo",
                        modifier = Modifier.size(24.dp),
                    )
                }
                // Eliminar todas permanentemente
                IconButton(onClick = onDeleteAll) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete_forever),
                        contentDescription = "Vaciar papelera",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

// ── Trash Note Card ───────────────────────────────────────────────────────────

@Composable
private fun TrashNoteCard(
    note: Note,
    plainText: String,
    dateString: String,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CapyNoteCard(
        title = note.title,
        dateString = dateString,
        plainText = plainText,
        isSecure = note.isSecure,
        onClick = {},
        modifier = modifier,
        trailingActions = {
            // Restaurar nota individual
            IconButton(onClick = onRestore) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_restore_note),
                    contentDescription = "Restaurar nota",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            // Eliminar nota permanentemente
            IconButton(onClick = onPermanentDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    contentDescription = "Eliminar permanentemente",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp),
                )
            }
        },
    )
}
