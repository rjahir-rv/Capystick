package com.capystick.app.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability as PlayUpdateAvailability

class PlayInAppUpdateClient(
    context: Context,
) : InAppUpdateClient {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private var installStateListener: InstallStateUpdatedListener? = null

    override fun requestUpdateInfo(
        onSuccess: (UpdateHandle) -> Unit,
        onFailure: () -> Unit,
    ) {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { onSuccess(PlayUpdateHandle(it)) }
            .addOnFailureListener { onFailure() }
    }

    override fun registerInstallStateListener(listener: (UpdateInstallStatus) -> Unit) {
        unregisterInstallStateListener()
        installStateListener =
            InstallStateUpdatedListener { state ->
                listener(state.installStatus().toUpdateInstallStatus())
            }.also(appUpdateManager::registerListener)
    }

    override fun unregisterInstallStateListener() {
        installStateListener?.let(appUpdateManager::unregisterListener)
        installStateListener = null
    }

    override fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    override fun startFlexibleUpdate(
        update: UpdateHandle,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ): Boolean {
        val appUpdateInfo = (update as? PlayUpdateHandle)?.appUpdateInfo ?: return false
        return appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            launcher,
            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        )
    }
}

private class PlayUpdateHandle(
    val appUpdateInfo: AppUpdateInfo,
) : UpdateHandle {
    override val availability: UpdateAvailability =
        when (appUpdateInfo.updateAvailability()) {
            PlayUpdateAvailability.UPDATE_AVAILABLE -> UpdateAvailability.AVAILABLE
            PlayUpdateAvailability.UPDATE_NOT_AVAILABLE -> UpdateAvailability.NOT_AVAILABLE
            else -> UpdateAvailability.UNKNOWN
        }

    override val installStatus: UpdateInstallStatus =
        appUpdateInfo.installStatus().toUpdateInstallStatus()

    override val isFlexibleUpdateAllowed: Boolean =
        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
}

private fun Int.toUpdateInstallStatus(): UpdateInstallStatus =
    when (this) {
        InstallStatus.DOWNLOADING -> UpdateInstallStatus.DOWNLOADING
        InstallStatus.DOWNLOADED -> UpdateInstallStatus.DOWNLOADED
        else -> UpdateInstallStatus.OTHER
    }
