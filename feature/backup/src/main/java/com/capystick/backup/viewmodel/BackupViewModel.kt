package com.capystick.backup.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.data.backup.BackupSerializer
import com.capystick.domain.repository.BackupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(

    @param : ApplicationContext private val context: Context,
    private val backupRepository: BackupRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    // Attempts to export the full database as JSON

    fun exportBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val hasNotes = backupRepository.hasActiveNotes()
                if (!hasNotes) {
                    _uiState.update {
                        it.copy(isLoading = false, showNoNotesWarning = true)
                    }
                    return@launch
                }
                val backupData = backupRepository.exportBackup()
                val json = BackupSerializer.toJson(backupData)
                outputStream.bufferedWriter().use { it.write(json) }
                _uiState.update {
                    it.copy(isLoading = false, exportSuccess = true)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al exportar: por favor verifica el archivo")
                }
            }
        }
    }

    /** Shows the confirmation dialog before actually importing. */
    fun requestImport(inputStream: InputStream) {
        pendingInputStream = inputStream
        _uiState.update { it.copy(showImportConfirmation = true) }
    }

    fun confirmImport() {
        val stream = pendingInputStream ?: return
        _uiState.update { it.copy(showImportConfirmation = false, isLoading = true) }
        viewModelScope.launch {
            try {
                val json = stream.bufferedReader().use { it.readText() }
                val backupData = BackupSerializer.fromJson(json)
                backupRepository.importBackup(backupData)
                _uiState.update { it.copy(isLoading = false, importSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "Error al importar: ${e.localizedMessage}")
                }
            } finally {
                pendingInputStream = null
            }
        }
    }

    fun dismissImportConfirmation() {
        pendingInputStream = null
        _uiState.update { it.copy(showImportConfirmation = false) }
    }
    fun dismissNoNotesWarning() = _uiState.update { it.copy(showNoNotesWarning = false) }
    fun dismissError() = _uiState.update { it.copy(error = null) }
    fun dismissExportSuccess() = _uiState.update { it.copy(exportSuccess = false) }
    fun dismissImportSuccess() = _uiState.update { it.copy(importSuccess = false) }
    private var pendingInputStream: InputStream? = null
}

data class BackupUiState(
    val isLoading: Boolean = false,
    val showNoNotesWarning: Boolean = false,
    val showImportConfirmation: Boolean = false,
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null,
)
