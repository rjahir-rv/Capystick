package com.capystick.notepad.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.capystick.notepad.R

data class NotePreviewStrings(
    val noteAddedToFavorites: String,
    val noteRemovedFromFavorites: String,
    val phoneLockNotConfigured: String,
    val unlockNoteTitle: String,
    val lockNoteTitle: String,
    val authenticateToRemoveLockSubtitle: String,
    val authenticateToLockNoteSubtitle: String,
    val noteLocked: String,
    val lockRemoved: String,
    val unlockToShare: String,
    val shareNoteTitle: String,
    val authenticateToViewContentSubtitle: String,
    val noteAddedToCollection: String,
    val noteAddedToNewCollection: String,
    val authenticationFailed: String,
)

@Composable
internal fun rememberNotePreviewStrings(): NotePreviewStrings {
    val noteAddedToFavorites = stringResource(R.string.note_added_to_favorites)
    val noteRemovedFromFavorites = stringResource(R.string.note_removed_from_favorites)
    val phoneLockNotConfigured = stringResource(R.string.phone_lock_not_configured)
    val unlockNoteTitle = stringResource(R.string.unlock_note)
    val lockNoteTitle = stringResource(R.string.lock_note)
    val authenticateToRemoveLockSubtitle = stringResource(R.string.authenticate_to_remove_lock)
    val authenticateToLockNoteSubtitle = stringResource(R.string.authenticate_to_lock_note)
    val noteLocked = stringResource(R.string.note_locked)
    val lockRemoved = stringResource(R.string.lock_removed)
    val unlockToShare = stringResource(R.string.unlock_to_share)
    val shareNoteTitle = stringResource(R.string.share_note)
    val authenticateToViewContentSubtitle = stringResource(R.string.authenticate_to_view_content)
    val noteAddedToCollection = stringResource(R.string.note_added_to_collection)
    val noteAddedToNewCollection = stringResource(R.string.note_added_to_new_collection)
    val authenticationFailed = stringResource(R.string.authentication_failed)

    return remember(
        noteAddedToFavorites,
        noteRemovedFromFavorites,
        phoneLockNotConfigured,
        unlockNoteTitle,
        lockNoteTitle,
        authenticateToRemoveLockSubtitle,
        authenticateToLockNoteSubtitle,
        noteLocked,
        lockRemoved,
        unlockToShare,
        shareNoteTitle,
        authenticateToViewContentSubtitle,
        noteAddedToCollection,
        noteAddedToNewCollection,
        authenticationFailed
    ) {
        NotePreviewStrings(
            noteAddedToFavorites = noteAddedToFavorites,
            noteRemovedFromFavorites = noteRemovedFromFavorites,
            phoneLockNotConfigured = phoneLockNotConfigured,
            unlockNoteTitle = unlockNoteTitle,
            lockNoteTitle = lockNoteTitle,
            authenticateToRemoveLockSubtitle = authenticateToRemoveLockSubtitle,
            authenticateToLockNoteSubtitle = authenticateToLockNoteSubtitle,
            noteLocked = noteLocked,
            lockRemoved = lockRemoved,
            unlockToShare = unlockToShare,
            shareNoteTitle = shareNoteTitle,
            authenticateToViewContentSubtitle = authenticateToViewContentSubtitle,
            noteAddedToCollection = noteAddedToCollection,
            noteAddedToNewCollection = noteAddedToNewCollection,
            authenticationFailed = authenticationFailed,
        )
    }
}
