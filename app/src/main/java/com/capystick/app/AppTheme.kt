package com.capystick.app

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capystick.designsystem.theme.CapystickTheme
import com.capystick.designsystem.theme.ThemeOption
import com.capystick.designsystem.theme.ThemePreferences
import com.capystick.designsystem.theme.ThemeSettings
import kotlinx.coroutines.flow.map

@Composable
fun ComponentActivity.CapystickAppThemeContent(content: @Composable () -> Unit) {
    val themePreferences = remember { ThemePreferences(applicationContext) }
    val themeSettingsFlow =
        remember(themePreferences) {
            themePreferences.themeSettings.map<ThemeSettings, ThemeSettings?> { it }
        }
    val themeSettings by themeSettingsFlow.collectAsStateWithLifecycle(
        initialValue = null,
    )
    val settings = themeSettings ?: return

    CapystickTheme(
        themeOption = settings.themeOption,
        paletteOption = settings.paletteOption,
    ) {
        SyncSystemBarIconAppearance(themeOption = settings.themeOption)
        content()
    }
}

@Composable
private fun ComponentActivity.SyncSystemBarIconAppearance(themeOption: ThemeOption) {
    val view = LocalView.current
    val useDarkIcons =
        when (themeOption) {
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
