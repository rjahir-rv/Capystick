package com.capystick.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.app.update.InAppUpdateCoordinator
import com.capystick.app.update.InAppUpdateUiState
import com.capystick.app.widget.WidgetNavigationIntents
import com.capystick.navigation.AppNavigation
import com.capystick.navigation.ExternalNavigationCommand
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject
    lateinit var inAppUpdateCoordinator: InAppUpdateCoordinator

    private var externalNavigationCommand by mutableStateOf<ExternalNavigationCommand?>(
        null,
    )

    private val updateLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult(),
        ) {
            inAppUpdateCoordinator.onUpdateFlowFinished()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        externalNavigationCommand = WidgetNavigationIntents.parseIntent(intent)

        setContent {
            CapystickAppThemeContent {
                val updateUiState by inAppUpdateCoordinator.uiState.collectAsStateWithLifecycle()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(updateUiState) {
                    if (updateUiState == InAppUpdateUiState.ReadyToInstall) {
                        val result =
                            snackbarHostState.showSnackbar(
                                message = getString(R.string.update_ready_message),
                                actionLabel = getString(R.string.update_restart_action),
                                duration = SnackbarDuration.Indefinite,
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            inAppUpdateCoordinator.completeUpdate()
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        modifier = Modifier.fillMaxSize(),
                        externalNavigationCommand = externalNavigationCommand,
                        onExternalNavigationHandled = { externalNavigationCommand = null },
                    )
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        inAppUpdateCoordinator.start()
        inAppUpdateCoordinator.checkForUpdate(updateLauncher)
    }

    override fun onResume() {
        super.onResume()
        inAppUpdateCoordinator.refreshState()
    }

    override fun onStop() {
        inAppUpdateCoordinator.stop()
        super.onStop()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        externalNavigationCommand = WidgetNavigationIntents.parseIntent(intent)
    }
}
