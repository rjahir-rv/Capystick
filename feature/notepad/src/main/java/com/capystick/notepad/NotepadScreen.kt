package com.capystick.notepad

import android.content.ClipData
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.core.designsystem.R
import com.capystick.designsystem.components.rememberBiometricAuthenticator
import com.capystick.notepad.components.FormattingToolbar
import com.capystick.notepad.components.NotepadTopBar
import com.capystick.notepad.components.RichNoteEditor
import com.capystick.notepad.util.rememberNotepadEditorState
import com.capystick.notepad.viewmodel.NotepadViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen(
    modifier: Modifier = Modifier,
    noteId: Int? = null,
    collectionId: Int? = null,
    innerPadding: PaddingValues,
    isUnlockedInitially: Boolean = false,
    onMenuClick: () -> Unit,
    onNoteSaved: () -> Unit = {},
    viewModel: NotepadViewModel = hiltViewModel(),
) {
    val clipboardManager = LocalClipboard.current
    val context = LocalContext.current
    val editorState = rememberNotepadEditorState()
    val scope = rememberCoroutineScope()
    val authenticator = rememberBiometricAuthenticator()
    var isUnlocked by rememberSaveable(noteId) { mutableStateOf(isUnlockedInitially) }

    LaunchedEffect(editorState.richTextState.annotatedString) {
        delay(500)
        editorState.undoManager.saveSnapshot()
    }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.clearNotepad()
            editorState.reset()
        }
    }

    val editorUiState by viewModel.editorState.collectAsStateWithLifecycle()
    val note = editorUiState.note

    LaunchedEffect(note) {
        if (noteId != null) {
            note?.let(editorState::load)
        }
    }

    LaunchedEffect(editorUiState.noteMissing) {
        if (editorUiState.noteMissing) {
            onNoteSaved()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NotepadTopBar(
                title = editorState.title,
                noteId = noteId,
                canCopy = !editorState.isNoteEmpty && (note?.isSecure != true || isUnlocked),
                onTitleChange = { editorState.title = it },
                onNavigateBack = onNoteSaved,
                onOpenMenu = onMenuClick,
                onCopyClick = {
                    val copyText = editorState.richTextState.toText()
                    val clipData = ClipData.newPlainText("Copy note", copyText)
                    scope.launch {
                        clipboardManager.setClipEntry(ClipEntry(clipData))
                    }
                },
                onSaveClick = {
                    viewModel.saveNote(
                        editorState.title,
                        editorState.richTextState.toHtml(),
                        collectionId,
                    ) {
                        val message = if (collectionId != null && noteId == null) {
                            "Nota guardada y añadida a la coleccion"
                        } else {
                            "Nota guardada"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        editorState.reset()
                        onNoteSaved()
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        val deviceSecure = authenticator.isDeviceSecure()
        val needsRecovery = note != null && note.isSecure && !deviceSecure
        val needsUnlock = note != null && note.isSecure && !isUnlocked && deviceSecure

        if (needsRecovery) {
            LockedEditorPrompt(
                message = "El bloqueo del telefono esta desactivado",
                actionLabel = "Quitar bloqueo y editar",
                onClick = {
                    viewModel.updateSecureStatus(note.id, isSecure = false)
                    isUnlocked = true
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
            )
        } else if (needsUnlock) {
            LockedEditorPrompt(
                message = "Esta nota esta bloqueada",
                actionLabel = "Desbloquear",
                onClick = {
                    authenticator.authenticate(
                        title = "Desbloquear nota",
                        subtitle = "Autenticate para editar el contenido",
                        onSuccess = { isUnlocked = true },
                        onError = { /* Toast is handled by the authenticator when needed. */ },
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(scaffoldPadding)
                    .padding(
                        start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                        end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                        bottom = innerPadding.calculateBottomPadding(),
                    )
                    .consumeWindowInsets(scaffoldPadding)
                    .imePadding(),
            ) {
                RichNoteEditor(
                    richTextState = editorState.richTextState,
                    modifier = Modifier.fillMaxSize(),
                )

                FormattingToolbar(
                    richTextState = editorState.richTextState,
                    undoManager = editorState.undoManager,
                    showStyleMenu = editorState.showStyleMenu,
                    onShowStyleMenuChange = { editorState.showStyleMenu = it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun LockedEditorPrompt(
    message: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.ic_lock),
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onClick) {
                Text(actionLabel)
            }
        }
    }
}
