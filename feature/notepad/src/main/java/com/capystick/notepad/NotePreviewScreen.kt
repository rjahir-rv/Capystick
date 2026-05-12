package com.capystick.notepad

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.notepad.components.CollectionSheet
import com.capystick.notepad.components.NotePreviewTopBar
import com.capystick.notepad.components.LockedNotePrompt
import com.capystick.notepad.components.NotePreviewContent
import com.capystick.designsystem.components.rememberBiometricAuthenticator
import com.capystick.model.NoteType
import com.capystick.notepad.util.noteContentToPlainText
import com.capystick.notepad.util.rememberNotePreviewStrings
import com.capystick.notepad.viewmodel.NotePreviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotePreviewScreen(
    modifier: Modifier = Modifier,
    noteId: Int,
    innerPadding: PaddingValues,
    isUnlockedInitially: Boolean = false,
    onBack: () -> Unit,
    onDeleteComplete: () -> Unit = onBack,
    onNoteMovedToTrash: (Int) -> Unit = {},
    onEditNote: (Int, Boolean) -> Unit = { _, _ -> },
    onEditChecklist: (Int, Boolean) -> Unit = { _, _ -> },
    viewModel: NotePreviewViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var isNavigatingAfterDelete by rememberSaveable(noteId) { mutableStateOf(false) }
    val context = LocalContext.current
    val note = uiState.note
    val authenticator = rememberBiometricAuthenticator()
    var isUnlocked by rememberSaveable(noteId) { mutableStateOf(isUnlockedInitially) }
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    var showCollectionSheet by rememberSaveable { mutableStateOf(false) }
    val strings = rememberNotePreviewStrings()

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    LaunchedEffect(uiState.noteMissing) {
        if (uiState.noteMissing && !isNavigatingAfterDelete) {
            onBack()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NotePreviewTopBar(
                title = note?.title ?: stringResource(R.string.loading),
                isFavorite = note?.isFavorite == true,
                isSecure = note?.isSecure == true,
                onBack = onBack,
                onToggleFavoriteClick = {
                    if (note != null) {
                        viewModel.toggleFavorite(note) { isFavorite ->
                            Toast.makeText(
                                context,
                                if (isFavorite) strings.noteAddedToFavorites else strings.noteRemovedFromFavorites,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
                onToggleSecureClick = {
                    if (note != null) {
                        if (note.isSecure && !authenticator.isDeviceSecure()) {
                            Toast.makeText(context, strings.phoneLockNotConfigured, Toast.LENGTH_SHORT).show()
                        } else {
                            authenticator.authenticate(
                                title = if (note.isSecure) strings.unlockNoteTitle else strings.lockNoteTitle,
                                subtitle = if (note.isSecure) strings.authenticateToRemoveLockSubtitle else strings.authenticateToLockNoteSubtitle,
                                onSuccess = {
                                    viewModel.toggleSecure(note) { isSecure ->
                                        Toast.makeText(
                                            context,
                                            if (isSecure) strings.noteLocked else strings.lockRemoved,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                },
                                onError = {
                                    Toast.makeText(context, strings.authenticationFailed, Toast.LENGTH_SHORT).show()
                                },
                            )
                        }
                    }
                },
                onAddToCollectionClick = { showCollectionSheet = true },
                onShareClick = {
                    if (note != null) {
                        if (note.isSecure && !isUnlocked) {
                            Toast.makeText(context, strings.unlockToShare, Toast.LENGTH_SHORT).show()
                        } else {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${note.title}\n\n${noteContentToPlainText(note)}",
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, strings.shareNoteTitle))
                        }
                    }
                },
                onDeleteClick = { showDeleteDialog = true },
            )
        },
    ) { scaffoldPadding ->
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_note_title)) },
                text = { Text(stringResource(R.string.delete_note_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            note?.let {
                                viewModel.deleteNote(it) {
                                    showDeleteDialog = false
                                    isNavigatingAfterDelete = true
                                    onNoteMovedToTrash(it.id)
                                    onDeleteComplete()
                                }
                            }
                        },
                    ) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.loading_note))
            }
        } else if (note != null) {
            val deviceSecure = authenticator.isDeviceSecure()
            val needsRecovery = note.isSecure && !deviceSecure
            val needsUnlock = note.isSecure && !isUnlocked && deviceSecure

            when {
                needsRecovery -> {
                    LockedNotePrompt(
                        message = stringResource(R.string.phone_lock_disabled),
                        actionLabel = stringResource(R.string.remove_lock_and_open),
                        onUnlock = {
                            viewModel.toggleSecure(note) {
                                Toast.makeText(context, strings.lockRemoved, Toast.LENGTH_SHORT).show()
                            }
                            isUnlocked = true
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaffoldPadding),
                    )
                }

                needsUnlock -> {
                    LockedNotePrompt(
                        message = stringResource(R.string.locked_note_message),
                        actionLabel = stringResource(R.string.unlock),
                        onUnlock = {
                            authenticator.authenticate(
                                title = strings.unlockNoteTitle,
                                subtitle = strings.authenticateToViewContentSubtitle,
                                onSuccess = { isUnlocked = true },
                                onError = {
                                    Toast.makeText(context, strings.authenticationFailed, Toast.LENGTH_SHORT).show()
                                },
                            )
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaffoldPadding),
                    )
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(scaffoldPadding)
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            onClick = {
                                if (note.type == NoteType.CHECKLIST) {
                                    onEditChecklist(noteId, isUnlocked)
                                } else {
                                    onEditNote(noteId, isUnlocked)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            NotePreviewContent(note = note)
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        if (showCollectionSheet && note != null) {
            CollectionSheet(
                collections = collections,
                onDismiss = { showCollectionSheet = false },
                onCollectionSelected = { collectionId ->
                    viewModel.addNoteToCollection(note.id, collectionId) {
                        Toast.makeText(
                            context,
                            strings.noteAddedToCollection,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                onCreateCollection = { name ->
                    viewModel.createCollectionAndAddNote(note.id, name) {
                        Toast.makeText(
                            context,
                            strings.noteAddedToNewCollection,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
            )
        }
    }
}
