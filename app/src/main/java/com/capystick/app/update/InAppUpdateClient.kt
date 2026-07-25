package com.capystick.app.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

interface InAppUpdateClient {
    fun requestUpdateInfo(
        onSuccess: (UpdateHandle) -> Unit,
        onFailure: () -> Unit,
    )

    fun registerInstallStateListener(listener: (UpdateInstallStatus) -> Unit)

    fun unregisterInstallStateListener()

    fun startFlexibleUpdate(
        update: UpdateHandle,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ): Boolean

    fun completeUpdate()
}

interface UpdateHandle {
    val availability: UpdateAvailability
    val installStatus: UpdateInstallStatus
    val isFlexibleUpdateAllowed: Boolean
}

enum class UpdateAvailability {
    AVAILABLE,
    NOT_AVAILABLE,
    UNKNOWN,
}

enum class UpdateInstallStatus {
    DOWNLOADING,
    DOWNLOADED,
    OTHER,
}
