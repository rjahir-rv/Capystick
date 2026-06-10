package com.capystick.app.update

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultInAppUpdateCoordinatorTest {
    private val launcher = TestActivityResultLauncher()

    @Test
    fun `check returns to idle when no update is available`() {
        val client =
            FakeInAppUpdateClient(
                update = FakeUpdateHandle(availability = UpdateAvailability.NOT_AVAILABLE),
            )
        val coordinator = DefaultInAppUpdateCoordinator(client)

        coordinator.checkForUpdate(launcher)

        assertEquals(InAppUpdateUiState.Idle, coordinator.uiState.value)
        assertFalse(client.startFlexibleUpdateCalled)
    }

    @Test
    fun `check starts an allowed flexible update only once`() {
        val client =
            FakeInAppUpdateClient(
                update =
                    FakeUpdateHandle(
                        availability = UpdateAvailability.AVAILABLE,
                        isFlexibleUpdateAllowed = true,
                    ),
            )
        val coordinator = DefaultInAppUpdateCoordinator(client)

        coordinator.checkForUpdate(launcher)
        coordinator.checkForUpdate(launcher)

        assertEquals(InAppUpdateUiState.Downloading, coordinator.uiState.value)
        assertEquals(1, client.updateInfoRequestCount)
        assertTrue(client.startFlexibleUpdateCalled)
    }

    @Test
    fun `downloaded update becomes ready to install`() {
        val client = FakeInAppUpdateClient()
        val coordinator = DefaultInAppUpdateCoordinator(client)
        coordinator.start()

        client.sendInstallStatus(UpdateInstallStatus.DOWNLOADED)

        assertEquals(InAppUpdateUiState.ReadyToInstall, coordinator.uiState.value)
    }

    @Test
    fun `refresh restores a downloaded update`() {
        val client =
            FakeInAppUpdateClient(
                update = FakeUpdateHandle(installStatus = UpdateInstallStatus.DOWNLOADED),
            )
        val coordinator = DefaultInAppUpdateCoordinator(client)

        coordinator.refreshState()

        assertEquals(InAppUpdateUiState.ReadyToInstall, coordinator.uiState.value)
    }

    @Test
    fun `start and stop register and unregister install listener`() {
        val client = FakeInAppUpdateClient()
        val coordinator = DefaultInAppUpdateCoordinator(client)

        coordinator.start()
        assertTrue(client.listenerRegistered)

        coordinator.stop()
        assertFalse(client.listenerRegistered)
    }

    @Test
    fun `complete update delegates to client`() {
        val client = FakeInAppUpdateClient()
        val coordinator = DefaultInAppUpdateCoordinator(client)

        coordinator.completeUpdate()

        assertTrue(client.completeUpdateCalled)
    }
}

private class FakeInAppUpdateClient(
    private val update: UpdateHandle = FakeUpdateHandle(),
    private val startResult: Boolean = true,
) : InAppUpdateClient {
    var updateInfoRequestCount = 0
    var startFlexibleUpdateCalled = false
    var listenerRegistered = false
    var completeUpdateCalled = false
    private var listener: ((UpdateInstallStatus) -> Unit)? = null

    override fun requestUpdateInfo(
        onSuccess: (UpdateHandle) -> Unit,
        onFailure: () -> Unit,
    ) {
        updateInfoRequestCount += 1
        onSuccess(update)
    }

    override fun registerInstallStateListener(listener: (UpdateInstallStatus) -> Unit) {
        this.listener = listener
        listenerRegistered = true
    }

    override fun unregisterInstallStateListener() {
        listener = null
        listenerRegistered = false
    }

    override fun startFlexibleUpdate(
        update: UpdateHandle,
        launcher: ActivityResultLauncher<IntentSenderRequest>,
    ): Boolean {
        startFlexibleUpdateCalled = true
        return startResult
    }

    override fun completeUpdate() {
        completeUpdateCalled = true
    }

    fun sendInstallStatus(status: UpdateInstallStatus) {
        listener?.invoke(status)
    }
}

private data class FakeUpdateHandle(
    override val availability: UpdateAvailability = UpdateAvailability.UNKNOWN,
    override val installStatus: UpdateInstallStatus = UpdateInstallStatus.OTHER,
    override val isFlexibleUpdateAllowed: Boolean = false,
) : UpdateHandle

private class TestActivityResultLauncher : ActivityResultLauncher<IntentSenderRequest>() {
    override fun launch(
        input: IntentSenderRequest,
        options: ActivityOptionsCompat?,
    ) = Unit

    override fun unregister() = Unit

    override val contract: ActivityResultContract<IntentSenderRequest, *>
        get() = throw UnsupportedOperationException()
}
