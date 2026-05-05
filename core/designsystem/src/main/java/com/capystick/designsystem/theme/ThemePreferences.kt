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

    val themeOption: Flow<ThemeOption> = context.dataStore.data.map { preferences ->
        val saved = preferences[THEME_KEY]
        ThemeOption.entries.find { it.name == saved } ?: ThemeOption.SYSTEM
    }

    val paletteOption: Flow<ColorPaletteOption> = context.dataStore.data.map { preferences ->
        val saved = preferences[PALETTE_KEY]
        ColorPaletteOption.entries.find { it.name == saved } ?: ColorPaletteOption.DEFAULT
    }

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
}
