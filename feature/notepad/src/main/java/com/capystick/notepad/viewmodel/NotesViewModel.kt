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
    private val _favoriteOnly = MutableStateFlow(false)

    val title: StateFlow<String> = combine(_collectionName, _favoriteOnly) { collectionName, favoriteOnly ->
        when {
            favoriteOnly -> "Favoritas"
            collectionName != null -> collectionName
            else -> "Todas las notas"
        }
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed( stopTimeoutMillis = 5000), "Todas las notas")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive = _isSearchActive.asStateFlow()

    private val _sortOrder = MutableStateFlow( value = NoteSortOrder.DATE_DESC)
    val sortOrder = _sortOrder.asStateFlow()

    val collections: StateFlow<List<Collection>> = collectionRepository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed( stopTimeoutMillis = 5000),
            initialValue = emptyList()
        )


    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<Note>> = _collectionId.flatMapLatest { colId ->
        _favoriteOnly.flatMapLatest { favoriteOnly ->
            val sourceFlow = notesSourceFor(colId, favoriteOnly)

            combine(
                flow = sourceFlow,
                flow2 = _searchQuery,
                flow3 = _sortOrder
            ) { notesList, query, order ->
                notesList
                    .filterByQuery(query)
                    .sortBy(order)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = emptyList()
    )

    fun initialize(id: Int?, name: String?, favoriteOnly: Boolean = false) {
        _collectionId.value = id
        _collectionName.value = name
        _favoriteOnly.value = favoriteOnly
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

    private val _selectedNoteIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNoteIds = _selectedNoteIds.asStateFlow()

    val isSelectionMode = _selectedNoteIds.map { it.isNotEmpty() }
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = false
        )

    fun toggleSelection(noteId: Int) {
        _selectedNoteIds.value = _selectedNoteIds.value.toMutableSet().apply {
            if (contains(noteId)) remove(element = noteId) else add(noteId)
        }
    }

    fun clearSelection() {
        _selectedNoteIds.value = emptySet()
    }

    fun deleteSelectedNotes(onNotesSoftDeleted: (Set<Int>) -> Unit = {}) {
        viewModelScope.launch {
            val colId = _collectionId.value
            val selectedIds = _selectedNoteIds.value
            if (_favoriteOnly.value) {
                removeSelectedNotesFromFavorites(selectedIds)
            } else if (colId != null) {
                removeSelectedNotesFromCollection(selectedIds, colId)
            } else {
                softDeleteSelectedNotes(selectedIds)
                onNotesSoftDeleted(selectedIds)
            }
            clearSelection()
        }
    }

    fun restoreNotes(noteIds: Set<Int>, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            noteIds.forEach { noteId ->
                noteRepository.restoreNote(noteId)
            }
            onComplete()
        }
    }

    fun selectedNotesToCollection(collectionId: Int) {
        viewModelScope.launch {
            addSelectedNotesToCollection(collectionId)
            clearSelection()
        }
    }

    fun createCollectionAndAddSelectedNotes(name: String) {
        viewModelScope.launch {
            val collectionId = collectionRepository.saveCollection(Collection(name = name)).toInt()
            addSelectedNotesToCollection(collectionId)
            clearSelection()
        }
    }

    private fun notesSourceFor(collectionId: Int?, favoriteOnly: Boolean) = when {
        favoriteOnly -> noteRepository.getFavoriteNotes()
        collectionId != null -> collectionRepository.getNotesInCollection(collectionId = collectionId)
        else -> noteRepository.getAllNotes()
    }

    private fun List<Note>.filterByQuery(query: String): List<Note> {
        if (query.isBlank()) return this
        return filter { it.title.contains(query, ignoreCase = true) }
    }

    private fun List<Note>.sortBy(order: NoteSortOrder): List<Note> {
        return when (order) {
            NoteSortOrder.DATE_DESC -> sortedByDescending { it.timestamp }
            NoteSortOrder.DATE_ASC -> sortedBy { it.timestamp }
            NoteSortOrder.TITLE_ASC -> sortedBy { it.title.lowercase() }
            NoteSortOrder.TITLE_DESC -> sortedByDescending { it.title.lowercase() }
        }
    }

    private suspend fun addSelectedNotesToCollection(collectionId: Int) {
        _selectedNoteIds.value.forEach { noteId ->
            collectionRepository.addNoteToCollection(noteId, collectionId)
        }
    }

    private suspend fun removeSelectedNotesFromCollection(
        selectedIds: Set<Int>,
        collectionId: Int,
    ) {
        selectedIds.forEach { noteId ->
            collectionRepository.removeNoteFromCollection(noteId, collectionId = collectionId)
        }
    }

    private suspend fun removeSelectedNotesFromFavorites(selectedIds: Set<Int>) {
        selectedIds.forEach { noteId ->
            noteRepository.updateFavoriteStatus(noteId, isFavorite = false)
        }
    }

    private suspend fun softDeleteSelectedNotes(selectedIds: Set<Int>) {
        selectedIds.forEach { noteId ->
            noteRepository.softDeleteNote(noteId)
        }
    }
}

enum class NoteSortOrder {
    DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC
}
