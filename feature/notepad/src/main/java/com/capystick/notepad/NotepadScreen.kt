package com.capystick.notepad

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.capystick.core.designsystem.R
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.notepad.util.TextUndoManager
import com.capystick.notepad.viewmodel.NotepadViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen(
    modifier: Modifier = Modifier,
    noteId: Int? = null,
    innerPadding: PaddingValues,
    onMenuClick: () -> Unit,
    onNoteSaved: () -> Unit = {},
    viewModel: NotepadViewModel = hiltViewModel()
) {
    val clipboardManager = LocalClipboard.current
    val richTextState = rememberRichTextState()
    val undoManager = remember { TextUndoManager(richTextState) }
    var title by rememberSaveable { mutableStateOf("Nueva nota") }
    val scope = rememberCoroutineScope()
    var showStyleMenu by remember { mutableStateOf(false) }
    val typography = MaterialTheme.typography
    val headlineStyle = typography.headlineMedium.toSpanStyle()
    val titleStyle = typography.titleLarge.toSpanStyle()
    val isNoteEmpty = richTextState.annotatedString.text.isEmpty()


    LaunchedEffect(richTextState.annotatedString) {
        delay(500)
        undoManager.saveSnapshot()
    }

    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.clearNotepad()
            title = "Nueva nota"
            richTextState.setHtml("")
            undoManager.clear()
        }
    }

    val note by viewModel.note.collectAsStateWithLifecycle()
    LaunchedEffect(note) {
        if (noteId != null) {
            note?.let {
                title = it.title
                richTextState.setHtml(it.content)
                undoManager.clear()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    val titleScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier.horizontalScroll(titleScrollState, reverseScrolling = true)
                    ) {
                        BasicTextField(
                            value = title,
                            onValueChange = { title = it },
                            textStyle = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    }
                },
                navigationIcon = {
                    if (noteId == null) {
                        IconButton(onClick = onMenuClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_menu),
                                contentDescription = "Menu",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = onNoteSaved) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_back),
                                contentDescription = "Back",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isNoteEmpty){
                            return@IconButton
                        }else{
                            val copyText = richTextState.toText()
                            val clipData = ClipData.newPlainText("Copy note", copyText)
                            scope.launch {
                                clipboardManager.setClipEntry(ClipEntry(clipData))
                            }
                        }

                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_copy),
                            contentDescription = "Copiar todo",
                            tint = MaterialTheme.colorScheme.primaryContainer
                        )
                    }

                    IconButton(onClick = { 
                        viewModel.saveNote(title, richTextState.toHtml()) { 
                            title = "Nueva nota"
                            richTextState.setHtml("")
                            onNoteSaved() 
                        } 
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_saved),
                            contentDescription = "Guardar nota",
                            tint = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .consumeWindowInsets(scaffoldPadding)
                .imePadding()
        ) {
            RichTextEditor(
                state = richTextState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
            )
            
            // Floating Toolbar
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isBold = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold
                    val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
                    val isUnderline = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true
                    val isHeadline = richTextState.currentSpanStyle.fontSize == headlineStyle.fontSize
                    val isTitle = richTextState.currentSpanStyle.fontSize == titleStyle.fontSize


                    IconButton(
                        onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                        modifier = Modifier.background(
                            color = if (isBold) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            CircleShape
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_format_bold),
                            contentDescription = "Bold",
                            tint = if (isBold) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                        modifier = Modifier.background(
                            color = if (isItalic) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            CircleShape
                        )
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_format_italic),
                            contentDescription = "Italic",
                            tint = if (isItalic) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                        modifier = Modifier.background(
                            color = if (isUnderline) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                            CircleShape
                        )
                    ) {
                        Text(
                            text = "U",
                            style = MaterialTheme.typography.titleMedium,
                            textDecoration = TextDecoration.Underline,
                            color = if (isUnderline) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box {
                        IconButton(onClick = { showStyleMenu = true }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_format_h1),
                                contentDescription = "Estilo de texto",
                                modifier = Modifier.background(color = if (isHeadline || isTitle) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent, CircleShape),
                            )
                        }
                        DropdownMenu(
                            expanded = showStyleMenu,
                            onDismissRequest = { showStyleMenu = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Título", style = MaterialTheme.typography.titleLarge) },
                                onClick = {
                                    richTextState.toggleSpanStyle(headlineStyle)
                                    showStyleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Subtítulo", style = MaterialTheme.typography.titleMedium) },
                                onClick = {
                                    richTextState.toggleSpanStyle(titleStyle)
                                    showStyleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Cuerpo", style = MaterialTheme.typography.bodyLarge) },
                                onClick = {
                                    val currentStyle = richTextState.currentSpanStyle
                                    if (currentStyle.fontSize == headlineStyle.fontSize) {
                                        richTextState.toggleSpanStyle(headlineStyle)
                                    } else if (currentStyle.fontSize == titleStyle.fontSize) {
                                        richTextState.toggleSpanStyle(titleStyle)
                                    }
                                    showStyleMenu = false
                                }
                            )
                        }
                    }
                    IconButton(onClick = { richTextState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) }) {
                        Icon(
                            painterResource(R.drawable.ic_format_streamline),
                            contentDescription = "icon streamline",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { undoManager.undo() },
                        enabled = undoManager.canUndo
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_undo),
                            contentDescription = "Deshacer",
                            tint = if (undoManager.canUndo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { undoManager.redo() },
                        enabled = undoManager.canRedo
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_redo),
                            contentDescription = "Rehacer",
                            tint = if (undoManager.canRedo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
