package com.capystick.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.capystick.app.widget.WidgetNavigationIntents
import com.capystick.navigation.AppNavigation
import com.capystick.navigation.ExternalNavigationCommand
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var externalNavigationCommand by mutableStateOf<ExternalNavigationCommand?>(
        null,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        externalNavigationCommand = WidgetNavigationIntents.parseIntent(intent)

        setContent {
            CapystickAppThemeContent {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                    externalNavigationCommand = externalNavigationCommand,
                    onExternalNavigationHandled = { externalNavigationCommand = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        externalNavigationCommand = WidgetNavigationIntents.parseIntent(intent)
    }
}
