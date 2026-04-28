package com.capystick.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capystick.domain.repository.CollectionRepository
import com.capystick.model.Collection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val repository: CollectionRepository
) : ViewModel() {

    val collections: StateFlow<List<Collection>> = repository.getAllCollections()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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
