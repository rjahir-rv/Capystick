package com.capystick.app.widget

import android.content.Context
import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.domain.repository.WidgetRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetAutoRefreshManager
    @Inject
    constructor(
        @param:ApplicationContext
        private val context: Context,
        noteRepository: NoteRepository,
        collectionRepository: CollectionRepository,
        widgetRepository: WidgetRepository,
    ) {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private var started = false

        private val widgetDataChanges =
            combine(
                noteRepository.getAllNotes(),
                collectionRepository.getAllCollections(),
                widgetRepository.getWidgetConfigurations(),
            ) { _, _, _ -> Unit }

        fun start() {
            if (started) return
            started = true

            scope.launch {
                widgetDataChanges.collectLatest {
                    NotesWidgetUpdater.updateAll(context)
                }
            }
        }
    }
