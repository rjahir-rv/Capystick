package com.capystick.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun CapystickTheme(
    themeOption: ThemeOption = ThemeOption.SYSTEM,
    paletteOption: ColorPaletteOption = ColorPaletteOption.DEFAULT,
    content: @Composable () -> Unit,
) {
    val systemIsDark = isSystemInDarkTheme()
    val useDynamicColors = themeOption == ThemeOption.DYNAMIC &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val isDark = when (themeOption) {
        ThemeOption.DARK -> true
        ThemeOption.LIGHT -> false
        ThemeOption.SYSTEM -> systemIsDark
        ThemeOption.DYNAMIC -> systemIsDark
    }

    val colorScheme = when {
        useDynamicColors -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> resolvePaletteColorScheme(
            paletteOption = paletteOption,
            isDark = isDark,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
