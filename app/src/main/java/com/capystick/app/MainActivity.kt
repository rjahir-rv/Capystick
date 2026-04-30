package com.capystick.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.designsystem.theme.CapystickTheme
import com.capystick.designsystem.theme.ThemeOption
import com.capystick.designsystem.theme.ThemePreferences
import com.capystick.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val themePreferences = ThemePreferences(applicationContext)

        setContent {
            val themeOption by themePreferences.themeOption.collectAsStateWithLifecycle(
                initialValue = ThemeOption.SYSTEM
            )

            CapystickTheme(themeOption = themeOption) {
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
