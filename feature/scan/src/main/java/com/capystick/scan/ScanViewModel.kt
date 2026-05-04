package com.capystick.scan

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.domain.scan.TextRecognizer
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ScanUiState {
    data object Idle : ScanUiState
    data class PhotoPreview(val bitmap: Bitmap) : ScanUiState
    data object Processing : ScanUiState
    data class TextExtracted(val text: String) : ScanUiState
    data class Error(val message: String) : ScanUiState
}

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val textRecognizer: TextRecognizer,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScanUiState>(ScanUiState.Idle)
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onPhotoCaptured(bitmap: Bitmap) {
        _uiState.value = ScanUiState.PhotoPreview(bitmap)
    }

    fun onUsePhoto(bitmap: Bitmap) {
        _uiState.value = ScanUiState.Processing
        viewModelScope.launch {
            textRecognizer.extractText(bitmap)
                .onSuccess { text ->
                    _uiState.value = ScanUiState.TextExtracted(text)
                }
                .onFailure { error ->
                    _uiState.value = ScanUiState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    fun onSaveNote(title: String, content: String, onSaved: (Int) -> Unit) {
        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content,
                timestamp = System.currentTimeMillis(),
                colorHex = 0L, // Placeholder
            )
            val id = noteRepository.saveNote(note)
            onSaved(id.toInt())
        }
    }

    fun onRetry() {
        _uiState.value = ScanUiState.Idle
    }
}
