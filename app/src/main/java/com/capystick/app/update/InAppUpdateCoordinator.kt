package com.capystick.app.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.StateFlow

interface InAppUpdateCoordinator {
    val uiState: StateFlow<InAppUpdateUiState>

    fun start()

    fun stop()

    fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>)

    fun refreshState()

    fun onUpdateFlowFinished()

    fun completeUpdate()
}

sealed interface InAppUpdateUiState {
    data object Idle : InAppUpdateUiState

    data object Checking : InAppUpdateUiState

    data object Downloading : InAppUpdateUiState

    data object ReadyToInstall : InAppUpdateUiState
}
