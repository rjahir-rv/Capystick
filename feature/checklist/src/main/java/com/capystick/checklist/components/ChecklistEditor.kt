package com.capystick.checklist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capystick.checklist.R
import com.capystick.model.ChecklistItem
import kotlinx.coroutines.delay
import com.capystick.core.designsystem.R as DesignR

@Composable
internal fun ChecklistEditor(
    items: List<ChecklistItem>,
    focusedItemId: String?,
    onFocusedItemHandled: () -> Unit,
    onItemTextChange: (String, String) -> Unit,
    onItemCheckedChange: (String, Boolean) -> Unit,
    onRemoveItem: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val bottomContentPadding = maxOf(120.dp, imeBottomPadding + 96.dp)

    LaunchedEffect(focusedItemId, items) {
        val itemId = focusedItemId ?: return@LaunchedEffect
        val itemIndex = items.indexOfFirst { it.id == itemId }
        if (itemIndex >= 0) {
            listState.animateScrollToItem(itemIndex, scrollOffset = 120)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 24.dp,
            end = 20.dp,
            bottom = bottomContentPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items, key = ChecklistItem::id) { item ->
            ChecklistEditorRow(
                item = item,
                shouldRequestFocus = item.id == focusedItemId,
                onFocusRequested = onFocusedItemHandled,
                onTextChange = { onItemTextChange(item.id, it) },
                onCheckedChange = { onItemCheckedChange(item.id, it) },
                onRemoveClick = { onRemoveItem(item.id) },
            )
        }
    }
}

@Composable
private fun ChecklistEditorRow(
    item: ChecklistItem,
    shouldRequestFocus: Boolean,
    onFocusRequested: () -> Unit,
    onTextChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember(item.id) { FocusRequester() }
    val bringIntoViewRequester = remember(item.id) { BringIntoViewRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(shouldRequestFocus) {
        if (shouldRequestFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
            delay(350)
            bringIntoViewRequester.bringIntoView()
            onFocusRequested()
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(start = 18.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(
                    id = if (item.checked) DesignR.drawable.ic_check_circle else DesignR.drawable.ic_circle,
                ),
                contentDescription = if (item.checked) {
                    stringResource(R.string.completed_item_content_description)
                } else {
                    stringResource(R.string.pending_item_content_description)
                },
                tint = if (item.checked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onCheckedChange(!item.checked) },
            )
            Spacer(modifier = Modifier.width(14.dp))
            BasicTextField(
                value = item.text,
                onValueChange = onTextChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = if (item.checked) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 20.sp,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                ),
                modifier = Modifier
                    .weight(1f)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    if (item.text.isBlank()) {
                        Text(
                            text = stringResource(R.string.new_item_placeholder),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    innerTextField()
                },
            )
            IconButton(onClick = onRemoveClick) {
                Icon(
                    painter = painterResource(id = DesignR.drawable.ic_close),
                    contentDescription = stringResource(R.string.delete_item_content_description),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.78f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
