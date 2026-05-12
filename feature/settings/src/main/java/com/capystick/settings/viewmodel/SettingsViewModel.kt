package com.capystick.settings.viewmodel

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.settings.R
import com.capystick.designsystem.theme.ColorPaletteOption
import com.capystick.designsystem.theme.ThemeOption
import com.capystick.designsystem.theme.ThemePreferences
import com.capystick.domain.repository.NotesExportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param : ApplicationContext private val context: Context,
    private val notesExportRepository: NotesExportRepository,
) : ViewModel() {

    private val themePreferences = ThemePreferences(context)
    val themeOption: StateFlow<ThemeOption> = themePreferences.themeOption
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( stopTimeoutMillis = 5_000),
            initialValue = ThemeOption.SYSTEM,
        )
    val paletteOption: StateFlow<ColorPaletteOption> = themePreferences.paletteOption
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = ColorPaletteOption.DEFAULT,
        )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    /** Persists the selected theme. */
    fun setTheme(option: ThemeOption) {
        viewModelScope.launch {
            themePreferences.setTheme(option)
        }
    }

    /** Persists the selected color palette. */
    fun setPalette(option: ColorPaletteOption) {
        viewModelScope.launch {
            themePreferences.setPalette(option)
        }
    }

    fun exportNotesToDirectory(directoryUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            try {
                val hasActiveNotes = notesExportRepository.hasActiveNotes()
                if (!hasActiveNotes) {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            showNoNotesWarning = true,
                        )
                    }
                    return@launch
                }

                val exportResult = notesExportRepository.exportActiveNotes()
                val writeResult = writeNotesToDirectory(directoryUri, exportResult.notes)
                if (writeResult.exportedCount == 0) {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            errorMessage = context.getString(R.string.export_notes_error),
                        )
                    }
                    return@launch
                }

                val message = if (writeResult.skippedCount == 0) {
                    context.getString(R.string.export_notes_success, writeResult.exportedCount)
                } else {
                    context.getString(
                        R.string.export_notes_partial_success,
                        writeResult.exportedCount,
                        writeResult.skippedCount,
                    )
                }
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportSuccessMessage = message,
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        errorMessage = context.getString(R.string.export_notes_error),
                    )
                }
            }
        }
    }

    fun dismissExportSuccess() = _uiState.update { it.copy(exportSuccessMessage = null) }

    fun dismissError() = _uiState.update { it.copy(errorMessage = null) }

    fun dismissNoNotesWarning() = _uiState.update { it.copy(showNoNotesWarning = false) }

    private suspend fun writeNotesToDirectory(
        directoryUri: Uri,
        notes: List<com.capystick.domain.repository.NoteTextExport>,
    ): ExportWriteResult = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, directoryUri)
            ?: throw IllegalStateException(context.getString(R.string.open_selected_folder_error))

        var exportedCount = 0
        var skippedCount = 0
        notes.forEach { noteExport ->
            try {
                val file = root.createFile("text/plain", noteExport.fileName.removeSuffix(".txt"))
                val fileUri = file?.uri ?: throw IllegalStateException(context.getString(R.string.create_file_error))
                context.contentResolver.openOutputStream(fileUri)?.use { output ->
                    output.write(noteExport.content.toByteArray(StandardCharsets.UTF_8))
                } ?: throw IllegalStateException(context.getString(R.string.open_output_stream_error))
                exportedCount++
            } catch (_: Exception) {
                skippedCount++
            }
        }
        ExportWriteResult(exportedCount = exportedCount, skippedCount = skippedCount)
    }
}

data class SettingsUiState(
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val errorMessage: String? = null,
    val showNoNotesWarning: Boolean = false,
)

private data class ExportWriteResult(
    val exportedCount: Int,
    val skippedCount: Int,
)
