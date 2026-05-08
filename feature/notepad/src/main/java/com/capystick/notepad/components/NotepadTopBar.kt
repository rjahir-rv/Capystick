package com.capystick.notepad.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.capystick.core.designsystem.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NotepadTopBar(
    title: String,
    noteId: Int?,
    canCopy: Boolean,
    onTitleChange: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onOpenMenu: () -> Unit,
    onCopyClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            NoteTitleField(
                title = title,
                onTitleChange = onTitleChange,
            )
        },
        navigationIcon = {
            if (noteId == null) {
                IconButton(onClick = onOpenMenu) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "Menu",
                        modifier = Modifier.size(28.dp),
                    )
                }
            } else {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back),
                        contentDescription = "Back",
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        },
        actions = {
            IconButton(
                onClick = onCopyClick,
                enabled = canCopy,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_copy),
                    contentDescription = "Copiar todo",
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            IconButton(onClick = onSaveClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_saved),
                    contentDescription = "Guardar nota",
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        },
    )
}

@Composable
internal fun NoteTitleField(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleScrollState = rememberScrollState()
    val titleFocus = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.horizontalScroll(titleScrollState, reverseScrolling = true),
    ) {
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(titleFocus)
                .                   onFocusChanged { isFocused = it.isFocused },
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                if (title.isBlank() && !isFocused) {
                    Text(
                        text = "Nueva nota",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
                innerTextField()
            },
        )
    }
}
