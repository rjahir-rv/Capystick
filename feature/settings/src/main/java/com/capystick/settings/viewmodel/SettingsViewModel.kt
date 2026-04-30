package com.capystick.settings.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.designsystem.theme.ThemeOption
import com.capystick.designsystem.theme.ThemePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {

    private val themePreferences = ThemePreferences(context)

    /** Currently saved theme option, collected as a hot StateFlow. */
    val themeOption: StateFlow<ThemeOption> = themePreferences.themeOption
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeOption.SYSTEM,
        )

    /** Persists the selected theme. */
    fun setTheme(option: ThemeOption) {
        viewModelScope.launch {
            themePreferences.setTheme(option)
        }
    }
}
