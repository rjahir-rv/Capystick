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
import com.capystick.designsystem.theme.ColorPaletteOption
import com.capystick.designsystem.theme.ThemeOption
import com.capystick.designsystem.theme.ThemePreferences

@Composable
fun ComponentActivity.CapystickAppThemeContent(content: @Composable () -> Unit) {
    val themePreferences = remember { ThemePreferences(applicationContext) }
    val themeOption by themePreferences.themeOption.collectAsStateWithLifecycle(
        initialValue = ThemeOption.SYSTEM,
    )
    val paletteOption by themePreferences.paletteOption.collectAsStateWithLifecycle(
        initialValue = ColorPaletteOption.DEFAULT,
    )

    CapystickTheme(
        themeOption = themeOption,
        paletteOption = paletteOption,
    ) {
        SyncSystemBarIconAppearance(themeOption = themeOption)
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
