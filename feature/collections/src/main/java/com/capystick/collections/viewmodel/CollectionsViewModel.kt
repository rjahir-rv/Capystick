package com.capystick.collections.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.model.Collection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CollectionSortOrder {
    NAME_ASC,
    NAME_DESC,
    NOTE_COUNT_DESC,
    NOTE_COUNT_ASC
}

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: CollectionRepository,
    noteRepository: NoteRepository,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _sortOrder = MutableStateFlow(CollectionSortOrder.NAME_ASC)
    val sortOrder: StateFlow<CollectionSortOrder> = _sortOrder.asStateFlow()

    val favoriteNoteCount: StateFlow<Int> = noteRepository.getFavoriteNoteCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = 0
        )

    val collections: StateFlow<List<Collection>> = combine(
        repository.getAllCollections(),
        _searchQuery,
        _sortOrder
    ) { list, query, order ->
        list.filter { it.name.contains(query, ignoreCase = true) }
            .sortedWith { a, b ->
                when (order) {
                    CollectionSortOrder.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                    CollectionSortOrder.NAME_DESC -> b.name.compareTo(a.name, ignoreCase = true)
                    CollectionSortOrder.NOTE_COUNT_DESC -> b.noteCount.compareTo(a.noteCount)
                    CollectionSortOrder.NOTE_COUNT_ASC -> a.noteCount.compareTo(b.noteCount)
                }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearchActiveChange(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun onSortOrderChange(order: CollectionSortOrder) {
        _sortOrder.value = order
    }

    fun createCollection(name: String) {
        viewModelScope.launch {
            repository.saveCollection(Collection(name = name))
        }
    }

    fun renameCollection(collection: Collection, newName: String) {
        viewModelScope.launch {
            repository.saveCollection(collection.copy(name = newName))
        }
    }

    fun deleteCollection(collection: Collection) {
        viewModelScope.launch {
            repository.deleteCollection(collection)
        }
    }
}
