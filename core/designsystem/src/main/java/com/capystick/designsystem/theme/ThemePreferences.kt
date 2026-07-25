package com.capystick.designsystem.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "capystick_theme_preferences"
)

/**
 * Manages persistent theme selection using DataStore Preferences.
 *
 * Usage:
 * ```
 * val prefs = ThemePreferences(context)
 * prefs.themeOption.collect { option -> ... }
 * prefs.setTheme(ThemeOption.DARK)
 * ```
 */
class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_option")
        private val PALETTE_KEY = stringPreferencesKey("palette_option")
    }

    val themeSettings: Flow<ThemeSettings> = context.dataStore.data.map { preferences ->
        ThemeSettings(
            themeOption = preferences.themeOption,
            paletteOption = preferences.paletteOption,
        )
    }

    val themeOption: Flow<ThemeOption> = themeSettings.map { it.themeOption }

    val paletteOption: Flow<ColorPaletteOption> = themeSettings.map { it.paletteOption }

    /** Persists the given [ThemeOption] to DataStore. */
    suspend fun setTheme(option: ThemeOption) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = option.name
        }
    }

    /** Persists the given [ColorPaletteOption] to DataStore. */
    suspend fun setPalette(option: ColorPaletteOption) {
        context.dataStore.edit { preferences ->
            preferences[PALETTE_KEY] = option.name
        }
    }

    private val Preferences.themeOption: ThemeOption
        get() = ThemeOption.entries.find { it.name == this[THEME_KEY] } ?: ThemeOption.SYSTEM

    private val Preferences.paletteOption: ColorPaletteOption
        get() = ColorPaletteOption.entries.find { it.name == this[PALETTE_KEY] } ?: ColorPaletteOption.DEFAULT
}

data class ThemeSettings(
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val paletteOption: ColorPaletteOption = ColorPaletteOption.DEFAULT,
)
