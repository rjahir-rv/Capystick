package com.capystick.notepad.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

@Composable
internal fun RichNoteEditor(
    richTextState: RichTextState,
    modifier: Modifier = Modifier,
) {
    RichTextEditor(
        state = richTextState,
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, start = 16.dp, end = 16.dp),
    )
}
