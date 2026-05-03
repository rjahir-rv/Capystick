package com.capystick.notepad

import android.content.ClipData
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.notepad.components.FormattingToolbar
import com.capystick.notepad.components.NotepadTopBar
import com.capystick.notepad.components.RichNoteEditor
import com.capystick.notepad.ui.rememberNotepadEditorState
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
    onMenuClick: () -> Unit,
    onNoteSaved: () -> Unit = {},
    viewModel: NotepadViewModel = hiltViewModel(),
) {
    val clipboardManager = LocalClipboard.current
    val editorState = rememberNotepadEditorState()
    val scope = rememberCoroutineScope()

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

    val note by viewModel.note.collectAsStateWithLifecycle()
    LaunchedEffect(note) {
        if (noteId != null) {
            note?.let(editorState::load)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            NotepadTopBar(
                title = editorState.title,
                noteId = noteId,
                canCopy = !editorState.isNoteEmpty,
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
                        editorState.reset()
                        onNoteSaved()
                    }
                },
            )
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
