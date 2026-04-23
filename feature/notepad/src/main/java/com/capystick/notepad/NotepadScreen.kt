package com.capystick.notepad

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.capystick.notepad.viewmodel.NotepadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotepadScreen(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    onMenuClick: () -> Unit,
    onNoteSaved: () -> Unit = {},
    viewModel: NotepadViewModel = hiltViewModel()
) {
    val richTextState = rememberRichTextState()
    var title by rememberSaveable { mutableStateOf("Nueva nota") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_menu),
                            contentDescription = "Menu",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveNote(title, richTextState.toHtml()) { onNoteSaved() } }) {
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
                    .imePadding()
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

                    IconButton(
                        onClick = { richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                        modifier = Modifier.background(
                            if (isBold) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
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
                            if (isItalic) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
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
                            if (isUnderline) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
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
                    IconButton(onClick = { /* TODO: Implement H1 */ }) {
                        Icon(painterResource(R.drawable.ic_format_h1), contentDescription = "H1")
                    }
                    IconButton(onClick = { /* TODO: Implement H2 */ }) {
                        Text(
                            text = "H2",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { /* TODO: Implement List */ }) {
                        Icon(painterResource(R.drawable.ic_format_streamline), contentDescription = "List")
                    }
                }
            }
        }
    }
}
