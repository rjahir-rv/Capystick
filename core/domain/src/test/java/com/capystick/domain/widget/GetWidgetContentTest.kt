package com.capystick.domain.widget

import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import com.capystick.domain.repository.WidgetRepository
import com.capystick.model.Collection
import com.capystick.model.Note
import com.capystick.model.WidgetConfiguration
import com.capystick.model.WidgetContentState
import com.capystick.model.WidgetMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetWidgetContentTest {

    @Test
    fun `recent notes are returned sorted by descending timestamp`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(appWidgetId = 7, mode = WidgetMode.RECENT_NOTES),
            ),
            noteRepository = FakeNoteRepository(
                notes = listOf(
                    note(id = 1, timestamp = 100L),
                    note(id = 2, timestamp = 300L),
                    note(id = 3, timestamp = 200L),
                ),
            ),
            collectionRepository = FakeCollectionRepository(),
        )

        val result = useCase(appWidgetId = 7)

        val content = result as WidgetContentState.Content
        assertEquals(listOf(2, 3, 1), content.notes.map { it.noteId })
    }

    @Test
    fun `recent widget without notes returns empty state`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(appWidgetId = 7, mode = WidgetMode.RECENT_NOTES),
            ),
            noteRepository = FakeNoteRepository(notes = emptyList()),
            collectionRepository = FakeCollectionRepository(),
        )

        val result = useCase(appWidgetId = 7)

        assertTrue(result is WidgetContentState.EmptyNoNotes)
        assertEquals("Notas recientes", result.title)
    }

    @Test
    fun `recent widget excludes secure notes`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(appWidgetId = 7, mode = WidgetMode.RECENT_NOTES),
            ),
            noteRepository = FakeNoteRepository(
                notes = listOf(
                    note(id = 1, timestamp = 100L, isSecure = true),
                    note(id = 2, timestamp = 300L),
                    note(id = 3, timestamp = 200L, isSecure = true),
                ),
            ),
            collectionRepository = FakeCollectionRepository(),
        )

        val result = useCase(appWidgetId = 7)

        val content = result as WidgetContentState.Content
        assertEquals(listOf(2), content.notes.map { it.noteId })
    }

    @Test
    fun `collection widget without collections returns no collections state`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(
                    appWidgetId = 8,
                    mode = WidgetMode.SELECTED_COLLECTION,
                    collectionId = 4,
                    collectionName = "Trabajo",
                ),
            ),
            noteRepository = FakeNoteRepository(),
            collectionRepository = FakeCollectionRepository(collections = emptyList()),
        )

        val result = useCase(appWidgetId = 8)

        assertTrue(result is WidgetContentState.EmptyNoCollections)
    }

    @Test
    fun `collection widget with missing collection returns missing collection state`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(
                    appWidgetId = 8,
                    mode = WidgetMode.SELECTED_COLLECTION,
                    collectionId = 4,
                    collectionName = "Trabajo",
                ),
            ),
            noteRepository = FakeNoteRepository(),
            collectionRepository = FakeCollectionRepository(
                collections = listOf(Collection(id = 2, name = "Ideas")),
            ),
        )

        val result = useCase(appWidgetId = 8)

        assertTrue(result is WidgetContentState.EmptyMissingCollection)
        assertEquals("Trabajo", result.title)
    }

    @Test
    fun `collection widget without notes returns empty collection state`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(
                    appWidgetId = 8,
                    mode = WidgetMode.SELECTED_COLLECTION,
                    collectionId = 4,
                    collectionName = "Trabajo",
                ),
            ),
            noteRepository = FakeNoteRepository(),
            collectionRepository = FakeCollectionRepository(
                collections = listOf(Collection(id = 4, name = "Trabajo")),
                notesByCollection = mapOf(4 to emptyList()),
            ),
        )

        val result = useCase(appWidgetId = 8)

        assertTrue(result is WidgetContentState.EmptyNoNotes)
        assertEquals("Trabajo", result.title)
    }

    @Test
    fun `collection widget returns collection notes sorted by descending timestamp`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(
                    appWidgetId = 8,
                    mode = WidgetMode.SELECTED_COLLECTION,
                    collectionId = 4,
                    collectionName = "Trabajo",
                ),
            ),
            noteRepository = FakeNoteRepository(),
            collectionRepository = FakeCollectionRepository(
                collections = listOf(Collection(id = 4, name = "Trabajo")),
                notesByCollection = mapOf(
                    4 to listOf(
                        note(id = 10, timestamp = 1L),
                        note(id = 11, timestamp = 5L),
                    ),
                ),
            ),
        )

        val result = useCase(appWidgetId = 8)

        val content = result as WidgetContentState.Content
        assertEquals("Trabajo", content.title)
        assertEquals(listOf(11, 10), content.notes.map { it.noteId })
    }

    @Test
    fun `collection widget excludes secure notes`() = runBlocking {
        val useCase = getWidgetContent(
            widgetRepository = FakeWidgetRepository(
                WidgetConfiguration(
                    appWidgetId = 8,
                    mode = WidgetMode.SELECTED_COLLECTION,
                    collectionId = 4,
                    collectionName = "Trabajo",
                ),
            ),
            noteRepository = FakeNoteRepository(),
            collectionRepository = FakeCollectionRepository(
                collections = listOf(Collection(id = 4, name = "Trabajo")),
                notesByCollection = mapOf(
                    4 to listOf(
                        note(id = 10, timestamp = 1L, isSecure = true),
                        note(id = 11, timestamp = 5L),
                    ),
                ),
            ),
        )

        val result = useCase(appWidgetId = 8)

        val content = result as WidgetContentState.Content
        assertEquals(listOf(11), content.notes.map { it.noteId })
    }

    private fun note(
        id: Int,
        timestamp: Long,
        isSecure: Boolean = false,
    ) = Note(
        id = id,
        title = "Nota $id",
        content = "",
        timestamp = timestamp,
        colorHex = 0L,
        isSecure = isSecure,
    )
}

private fun getWidgetContent(
    widgetRepository: WidgetRepository,
    noteRepository: NoteRepository,
    collectionRepository: CollectionRepository,
): GetWidgetContent =
    GetWidgetContent(
        widgetRepository = widgetRepository,
        noteRepository = noteRepository,
        collectionRepository = collectionRepository,
        widgetContentMapper = WidgetContentMapper(WidgetNotePreviewFormatter()),
    )

private class FakeWidgetRepository(
    private val configuration: WidgetConfiguration? = null,
) : WidgetRepository {
    override fun getWidgetConfigurations(): Flow<List<WidgetConfiguration>> =
        flowOf(listOfNotNull(configuration))

    override suspend fun getWidgetConfiguration(appWidgetId: Int): WidgetConfiguration? = configuration

    override suspend fun saveWidgetConfiguration(configuration: WidgetConfiguration) = Unit

    override suspend fun deleteWidgetConfiguration(appWidgetId: Int) = Unit
}

private class FakeNoteRepository(
    private val notes: List<Note> = emptyList(),
) : NoteRepository {
    override fun getAllNotes(): Flow<List<Note>> = flowOf(notes)
    override fun getNoteById(id: Int): Flow<Note?> = flowOf(notes.firstOrNull { it.id == id })
    override fun getFavoriteNotes(): Flow<List<Note>> = flowOf(notes.filter { it.isFavorite })
    override fun getFavoriteNoteCount(): Flow<Int> = flowOf(notes.count { it.isFavorite })
    override suspend fun saveNote(note: Note): Long = note.id.toLong()
    override suspend fun deleteNote(note: Note) = Unit
    override suspend fun updateFavoriteStatus(noteId: Int, isFavorite: Boolean) = Unit
    override suspend fun updateSecureStatus(noteId: Int, isSecure: Boolean) = Unit
    override fun getDeletedNotes(): Flow<List<Note>> = flowOf(emptyList())
    override suspend fun softDeleteNote(noteId: Int) = Unit
    override suspend fun restoreNote(noteId: Int) = Unit
    override suspend fun restoreAllNotes() = Unit
    override suspend fun permanentlyDeleteNote(note: Note) = Unit
    override suspend fun permanentlyDeleteAllTrashed() = Unit
}

private class FakeCollectionRepository(
    private val collections: List<Collection> = emptyList(),
    private val notesByCollection: Map<Int, List<Note>> = emptyMap(),
) : CollectionRepository {
    override fun getAllCollections(): Flow<List<Collection>> = flowOf(collections)
    override fun getNotesInCollection(collectionId: Int): Flow<List<Note>> =
        flowOf(notesByCollection[collectionId].orEmpty())

    override suspend fun saveCollection(collection: Collection): Long = collection.id.toLong()
    override suspend fun deleteCollection(collection: Collection) = Unit
    override suspend fun addNoteToCollection(noteId: Int, collectionId: Int) = Unit
    override suspend fun removeNoteFromCollection(noteId: Int, collectionId: Int) = Unit
}

