package com.capystick.domain.widget

import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.domain.repository.WidgetRepository
import com.capystick.model.WidgetConfiguration
import com.capystick.model.WidgetContentState
import com.capystick.model.WidgetMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetWidgetContent @Inject constructor(
    private val widgetRepository: WidgetRepository,
    private val noteRepository: NoteRepository,
    private val collectionRepository: CollectionRepository,
    private val widgetContentMapper: WidgetContentMapper,
) {
    suspend operator fun invoke(appWidgetId: Int): WidgetContentState = observe(appWidgetId).first()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(appWidgetId: Int): Flow<WidgetContentState> =
        widgetRepository.getWidgetConfigurations()
            .map { configurations ->
                configurations.firstOrNull { it.appWidgetId == appWidgetId }
                    ?: WidgetConfiguration(
                        appWidgetId = appWidgetId,
                        mode = WidgetMode.RECENT_NOTES,
                    )
            }
            .distinctUntilChanged()
            .flatMapLatest { configuration ->
                when (configuration.mode) {
                    WidgetMode.RECENT_NOTES -> {
                        noteRepository.getAllNotes().map { notes ->
                            widgetContentMapper.recentNotes(configuration, notes)
                        }
                    }

                    WidgetMode.SELECTED_COLLECTION -> observeCollectionState(configuration)
                }
            }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCollectionState(
        configuration: WidgetConfiguration,
    ): Flow<WidgetContentState> =
        collectionRepository.getAllCollections().flatMapLatest { collections ->
            if (collections.isEmpty()) {
                return@flatMapLatest flowOf(
                    WidgetContentState.EmptyNoCollections(configuration = configuration),
                )
            }

            val collection = collections.firstOrNull { it.id == configuration.collectionId }
                ?: return@flatMapLatest flowOf(
                    WidgetContentState.EmptyMissingCollection(
                        title = configuration.collectionName ?: "Coleccion",
                        configuration = configuration,
                    ),
                )

            val resolvedConfiguration = configuration.copy(
                collectionId = collection.id,
                collectionName = collection.name,
            )
            collectionRepository.getNotesInCollection(collection.id).map { notes ->
                widgetContentMapper.collectionNotes(
                    configuration = resolvedConfiguration,
                    collectionName = collection.name,
                    notes = notes,
                )
            }
        }
}
