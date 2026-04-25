package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _sortOrder = MutableStateFlow(NoteSortOrder.DATE_DESC)
    val sortOrder = _sortOrder.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(
        flow = repository.getAllNotes(),
        flow2 = _searchQuery,
        flow3 = _sortOrder
    ) { notesList, query, order ->
        var result = notesList
        if (query.isNotBlank()) {
            result = result.filter { it.title.contains(query, ignoreCase = true) }
        }
        result = when (order) {
            NoteSortOrder.DATE_DESC -> result.sortedByDescending { it.timestamp }
            NoteSortOrder.DATE_ASC -> result.sortedBy { it.timestamp }
            NoteSortOrder.TITLE_ASC -> result.sortedBy { it.title.lowercase() }
            NoteSortOrder.TITLE_DESC -> result.sortedByDescending { it.title.lowercase() }
        }
        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(isActive: Boolean) {
        _isSearchActive.value = isActive
        if (!isActive) {
            _searchQuery.value = ""
        }
    }

    fun onSortOrderChange(order: NoteSortOrder) {
        _sortOrder.value = order
    }
}

enum class NoteSortOrder {
    DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC
}