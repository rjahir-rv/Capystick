package com.capystick.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Collection
import com.capystick.model.Note
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val collectionRepository: CollectionRepository
) : ViewModel() {

    private val _collectionId = MutableStateFlow<Int?>(null)
    private val _collectionName = MutableStateFlow<String?>(null)

    val title: StateFlow<String> = _collectionName.map { it ?: "Todas las notas" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Todas las notas")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _sortOrder = MutableStateFlow(NoteSortOrder.DATE_DESC)
    val sortOrder = _sortOrder.asStateFlow()

    val collections: StateFlow<List<Collection>> = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _collectionId.flatMapLatest { colId ->
        val sourceFlow = if (colId != null) {
            collectionRepository.getNotesInCollection(colId)
        } else {
            noteRepository.getAllNotes()
        }
        
        combine(
            flow = sourceFlow,
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
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = emptyList()
    )

    fun initialize(id: Int?, name: String?) {
        _collectionId.value = id
        _collectionName.value = name
    }

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

    fun addNoteToCollection(noteId: Int, collectionId: Int) {
        viewModelScope.launch {
            collectionRepository.addNoteToCollection(noteId, collectionId)
        }
    }

    fun createCollectionAndAddNote(name: String, noteId: Int) {
        viewModelScope.launch {
            val id = collectionRepository.saveCollection(Collection(name = name))
            collectionRepository.addNoteToCollection(noteId, id.toInt())
        }
    }
}

enum class NoteSortOrder {
    DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC
}