@file:Suppress("AssignedValueIsNeverRead")

package com.capystick.checklist

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.checklist.viewmodel.ChecklistViewModel
import com.capystick.designsystem.components.rememberBiometricAuthenticator
import com.capystick.model.ChecklistItem
import kotlinx.coroutines.delay
import com.capystick.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
    noteId: Int? = null,
    collectionId: Int? = null,
    initialTitle: String = "",
    isUnlockedInitially: Boolean = false,
    onMenuClick: () -> Unit = {},
    onChecklistSaved: () -> Unit = {},
    viewModel: ChecklistViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authenticator = rememberBiometricAuthenticator()
    var isUnlocked by rememberSaveable(noteId) { mutableStateOf(isUnlockedInitially) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var focusedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val checklistSavedMessage = stringResource(R.string.checklist_saved)
    val checklistSavedToCollectionMessage = stringResource(R.string.checklist_saved_to_collection)
    val unlockChecklistMessage = stringResource(R.string.unlock_checklist_title)
    val unlockChecklistSubtitle = stringResource(R.string.unlock_checklist_subtitle)

    fun navigateBack() {
        if (uiState.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onChecklistSaved()
        }
    }
    val saveChecklist = {
        viewModel.saveChecklist(collectionId) {
            val message = if (collectionId != null && noteId == null) {
                checklistSavedToCollectionMessage
            } else {
                checklistSavedMessage
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onChecklistSaved()
        }
    }

    LaunchedEffect(noteId, initialTitle) {
        if (noteId != null) {
            viewModel.loadNote(noteId)
        } else {
            viewModel.clearChecklist(initialTitle)
        }
    }

    LaunchedEffect(uiState.noteMissing) {
        if (uiState.noteMissing) {
            onChecklistSaved()
        }
    }

    BackHandler(enabled = true) {
        navigateBack()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = uiState.title.ifBlank { stringResource(R.string.checklist_default_title) },
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = ::navigateBack) {
                        Icon(
                            painter = painterResource(id = DesignR.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_content_description),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = saveChecklist) {
                        Icon(
                            painter = painterResource(id = DesignR.drawable.ic_save),
                            contentDescription = stringResource(R.string.save_checklist_content_description),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
                ),
            )
        },
        floatingActionButton = {
            val note = uiState.note
            val canEdit = note == null || !note.isSecure || isUnlocked || !authenticator.isDeviceSecure()
            if (canEdit) {
                FloatingActionButton(
                    onClick = {
                        focusedItemId = viewModel.addItem()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.ic_add),
                        contentDescription = stringResource(R.string.add_item_content_description),
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        },
    ) { scaffoldPadding ->
        val note = uiState.note
        val deviceSecure = authenticator.isDeviceSecure()
        val needsRecovery = note != null && note.isSecure && !deviceSecure
        val needsUnlock = note != null && note.isSecure && !isUnlocked && deviceSecure

        when {
            needsRecovery -> {
                LockedChecklistPrompt(
                    message = stringResource(R.string.phone_lock_disabled),
                    actionLabel = stringResource(R.string.remove_lock_and_edit),
                    onClick = {
                        viewModel.updateSecureStatus(note.id, isSecure = false)
                        isUnlocked = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                )
            }

            needsUnlock -> {
                LockedChecklistPrompt(
                    message = stringResource(R.string.locked_checklist_message),
                    actionLabel = stringResource(R.string.unlock),
                    onClick = {
                        authenticator.authenticate(
                            title = unlockChecklistMessage,
                            subtitle = unlockChecklistSubtitle,
                            onSuccess = { isUnlocked = true },
                            onError = { },
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding),
                )
            }

            else -> {
                ChecklistEditor(
                    items = uiState.items,
                    focusedItemId = focusedItemId,
                    onFocusedItemHandled = { focusedItemId = null },
                    onItemTextChange = viewModel::onItemTextChange,
                    onItemCheckedChange = viewModel::onItemCheckedChange,
                    onRemoveItem = viewModel::removeItem,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f))
                        .padding(scaffoldPadding)
                        .padding(
                            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                            bottom = innerPadding.calculateBottomPadding(),
                        )
                        .consumeWindowInsets(scaffoldPadding)
                )
            }
        }
    }

    if (showDiscardDialog) {
        DiscardChangesDialog(
            onDismiss = { showDiscardDialog = false },
            onDiscard = {
                showDiscardDialog = false
                onChecklistSaved()
            },
            onSave = {
                showDiscardDialog = false
                saveChecklist()
            },
        )
    }
}

@Composable
private fun ChecklistEditor(
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
            top = 32.dp,
            end = 20.dp,
            bottom = bottomContentPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp),
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
        shape = RoundedCornerShape(34.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .height(74.dp)
                .padding(start = 22.dp, end = 12.dp),
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
                    .size(38.dp)
                    .clickable { onCheckedChange(!item.checked) },
            )
            Spacer(modifier = Modifier.width(18.dp))
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
                    fontSize = 22.sp,
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
                    tint = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun DiscardChangesDialog(
    onDismiss: () -> Unit,
    onDiscard: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.discard_changes_title)) },
        text = { Text(stringResource(R.string.discard_changes_message)) },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = stringResource(R.string.discard),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

@Composable
private fun LockedChecklistPrompt(
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
                painter = painterResource(id = DesignR.drawable.ic_lock),
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
