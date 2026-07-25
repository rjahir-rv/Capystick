package com.capystick.app.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultInAppUpdateCoordinator(
    private val client: InAppUpdateClient,
) : InAppUpdateCoordinator {
    private val _uiState = MutableStateFlow<InAppUpdateUiState>(InAppUpdateUiState.Idle)
    override val uiState: StateFlow<InAppUpdateUiState> = _uiState.asStateFlow()

    private var hasCheckedForUpdate = false
    private var updateFlowStarted = false

    override fun start() {
        client.registerInstallStateListener(::onInstallStatusChanged)
    }

    override fun stop() {
        client.unregisterInstallStateListener()
    }

    override fun checkForUpdate(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        if (hasCheckedForUpdate) return

        hasCheckedForUpdate = true
        _uiState.value = InAppUpdateUiState.Checking
        client.requestUpdateInfo(
            onSuccess = { update ->
                when {
                    update.installStatus == UpdateInstallStatus.DOWNLOADED -> {
                        _uiState.value = InAppUpdateUiState.ReadyToInstall
                    }

                    update.availability == UpdateAvailability.AVAILABLE &&
                        update.isFlexibleUpdateAllowed -> {
                        updateFlowStarted =
                            runCatching {
                                client.startFlexibleUpdate(update, launcher)
                            }.getOrDefault(false)
                        _uiState.value =
                            if (updateFlowStarted) {
                                InAppUpdateUiState.Downloading
                            } else {
                                InAppUpdateUiState.Idle
                            }
                    }

                    else -> _uiState.value = InAppUpdateUiState.Idle
                }
            },
            onFailure = {
                _uiState.value = InAppUpdateUiState.Idle
            },
        )
    }

    override fun refreshState() {
        if (_uiState.value == InAppUpdateUiState.Checking) return

        client.requestUpdateInfo(
            onSuccess = { update ->
                if (update.installStatus == UpdateInstallStatus.DOWNLOADED) {
                    _uiState.value = InAppUpdateUiState.ReadyToInstall
                }
            },
            onFailure = {},
        )
    }

    override fun onUpdateFlowFinished() {
        updateFlowStarted = false
        if (_uiState.value != InAppUpdateUiState.ReadyToInstall) {
            _uiState.value = InAppUpdateUiState.Idle
        }
    }

    override fun completeUpdate() {
        client.completeUpdate()
    }

    private fun onInstallStatusChanged(status: UpdateInstallStatus) {
        _uiState.value =
            when (status) {
                UpdateInstallStatus.DOWNLOADING -> InAppUpdateUiState.Downloading
                UpdateInstallStatus.DOWNLOADED -> InAppUpdateUiState.ReadyToInstall
                UpdateInstallStatus.OTHER -> return
            }
    }
}
