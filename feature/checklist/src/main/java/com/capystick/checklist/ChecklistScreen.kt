@file:Suppress("AssignedValueIsNeverRead")

package com.capystick.checklist

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.checklist.components.ChecklistEditor
import com.capystick.checklist.components.ChecklistFab
import com.capystick.checklist.components.ChecklistTopBar
import com.capystick.checklist.components.DiscardChangesDialog
import com.capystick.checklist.components.LockedChecklistPrompt
import com.capystick.checklist.viewmodel.ChecklistViewModel
import com.capystick.designsystem.components.rememberBiometricAuthenticator

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
            ChecklistTopBar(
                title = uiState.title,
                onBackClick = ::navigateBack,
                onSaveClick = saveChecklist,
            )
        },
        floatingActionButton = {
            val note = uiState.note
            val canEdit = note == null || !note.isSecure || isUnlocked || !authenticator.isDeviceSecure()
            if (canEdit) {
                ChecklistFab(
                    onClick = { focusedItemId = viewModel.addItem() },
                )
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
