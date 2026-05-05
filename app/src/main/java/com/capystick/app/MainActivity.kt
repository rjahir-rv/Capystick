package com.capystick.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.designsystem.theme.CapystickTheme
import com.capystick.designsystem.theme.ColorPaletteOption
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
            val preferences = remember { themePreferences }
            val themeOption by preferences.themeOption.collectAsStateWithLifecycle(
                initialValue = ThemeOption.SYSTEM,
            )
            val paletteOption by preferences.paletteOption.collectAsStateWithLifecycle(
                initialValue = ColorPaletteOption.DEFAULT,
            )

            CapystickTheme(
                themeOption = themeOption,
                paletteOption = paletteOption,
            ) {
                SyncSystemBarIconAppearance(themeOption = themeOption)
                AppNavigation(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ComponentActivity.SyncSystemBarIconAppearance(
    themeOption: ThemeOption,
) {
    val view = LocalView.current
    val useDarkIcons = when (themeOption) {
        ThemeOption.LIGHT -> true
        ThemeOption.DARK -> false
        ThemeOption.SYSTEM, ThemeOption.DYNAMIC -> !isSystemInDarkTheme()
    }

    SideEffect {
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = useDarkIcons
            isAppearanceLightNavigationBars = useDarkIcons
        }
    }
}
