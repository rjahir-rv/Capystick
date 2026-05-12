package com.capystick.notepad.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.capystick.notepad.R
import com.capystick.notepad.util.TextUndoManager
import com.mohamedrejeb.richeditor.model.RichTextState
import com.capystick.core.designsystem.R as DesignR

@Composable
internal fun FormattingToolbar(
    richTextState: RichTextState,
    undoManager: TextUndoManager,
    showStyleMenu: Boolean,
    onShowStyleMenuChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = MaterialTheme.typography
    val headlineStyle = typography.headlineMedium.toSpanStyle()
    val titleStyle = typography.titleLarge.toSpanStyle()
    val isBold = richTextState.currentSpanStyle.fontWeight == FontWeight.Bold
    val isItalic = richTextState.currentSpanStyle.fontStyle == FontStyle.Italic
    val isUnderline = richTextState.currentSpanStyle.textDecoration?.contains(TextDecoration.Underline) == true
    val isHeadline = richTextState.currentSpanStyle.fontSize == headlineStyle.fontSize
    val isTitle = richTextState.currentSpanStyle.fontSize == titleStyle.fontSize

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(0.9f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FormatIconButton(
                isActive = isBold,
                onClick = {
                    richTextState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
                },
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_format_bold),
                    contentDescription = stringResource(R.string.bold_content_description),
                    tint = if (isBold) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                )
            }
            FormatIconButton(
                isActive = isItalic,
                onClick = {
                    richTextState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
                },
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_format_italic),
                    contentDescription = stringResource(R.string.italic_content_description),
                    tint = if (isItalic) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                )
            }
            FormatIconButton(
                isActive = isUnderline,
                onClick = {
                    richTextState.toggleSpanStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                    )
                },
            ) {
                Text(
                    text = "U",
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = TextDecoration.Underline,
                    color = if (isUnderline) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                )
            }
            TextStyleMenu(
                richTextState = richTextState,
                showStyleMenu = showStyleMenu,
                isHighlighted = isHeadline || isTitle,
                headlineStyle = headlineStyle,
                titleStyle = titleStyle,
                onShowStyleMenuChange = onShowStyleMenuChange,
            )
            IconButton(
                onClick = {
                    richTextState.toggleSpanStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                    )
                },
            ) {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_format_streamline),
                    contentDescription = stringResource(R.string.strikethrough_content_description),
                    modifier = Modifier.size(16.dp),
                )
            }
            IconButton(
                onClick = { undoManager.undo() },
                enabled = undoManager.canUndo,
            ) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_undo),
                    contentDescription = stringResource(R.string.undo_content_description),
                    tint = if (undoManager.canUndo) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
            IconButton(
                onClick = { undoManager.redo() },
                enabled = undoManager.canRedo,
            ) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_redo),
                    contentDescription = stringResource(R.string.redo_content_description),
                    tint = if (undoManager.canRedo) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun FormatIconButton(
    isActive: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.background(
            color = if (isActive) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            shape = CircleShape,
        ),
    ) {
        content()
    }
}

@Composable
private fun TextStyleMenu(
    richTextState: RichTextState,
    showStyleMenu: Boolean,
    isHighlighted: Boolean,
    headlineStyle: SpanStyle,
    titleStyle: SpanStyle,
    onShowStyleMenuChange: (Boolean) -> Unit,
) {
    Box {
        IconButton(onClick = { onShowStyleMenuChange(true) }) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_format_h1),
                contentDescription = stringResource(R.string.text_style_content_description),
                modifier = Modifier.background(
                    color = if (isHighlighted) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    shape = CircleShape,
                ),
            )
        }
        DropdownMenu(
            expanded = showStyleMenu,
            onDismissRequest = { onShowStyleMenuChange(false) },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.text_style_title), style = MaterialTheme.typography.titleLarge) },
                onClick = {
                    richTextState.toggleSpanStyle(headlineStyle)
                    onShowStyleMenuChange(false)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.text_style_subtitle), style = MaterialTheme.typography.titleMedium) },
                onClick = {
                    richTextState.toggleSpanStyle(titleStyle)
                    onShowStyleMenuChange(false)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.text_style_body), style = MaterialTheme.typography.bodyLarge) },
                onClick = {
                    val currentStyle = richTextState.currentSpanStyle
                    if (currentStyle.fontSize == headlineStyle.fontSize) {
                        richTextState.toggleSpanStyle(headlineStyle)
                    } else if (currentStyle.fontSize == titleStyle.fontSize) {
                        richTextState.toggleSpanStyle(titleStyle)
                    }
                    onShowStyleMenuChange(false)
                },
            )
        }
    }
}
