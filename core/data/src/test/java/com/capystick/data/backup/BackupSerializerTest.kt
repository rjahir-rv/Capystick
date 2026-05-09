package com.capystick.data.backup

import com.capystick.model.BackupData
import com.capystick.model.Collection
import com.capystick.model.Note
import com.capystick.model.NoteCollectionRef
import com.capystick.model.NoteType
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupSerializerTest {

    @Test
    fun `toJson and fromJson should preserve data integrity`() {
        // Arrange
        val notes = listOf(
            Note(
                id = 1,
                title = "Note 1",
                content = "Content 1",
                timestamp = 123L,
                colorHex = 0xFFFFFFFFL,
                isDeleted = false,
                isFavorite = true,
                isSecure = true,
            ),
            Note(
                id = 2,
                title = "Note 2",
                content = "Content 2",
                timestamp = 456L,
                colorHex = 0x00000000L,
                type = NoteType.CHECKLIST,
                isDeleted = true,
            ),
        )
        val collections = listOf(
            Collection(id = 1, name = "Col 1"),
            Collection(id = 2, name = "Col 2"),
        )
        val refs = listOf(
            NoteCollectionRef(noteId = 1, collectionId = 1),
            NoteCollectionRef(noteId = 2, collectionId = 2),
        )
        val originalData = BackupData(
            version = 2,
            createdAt = 999L,
            notes = notes,
            collections = collections,
            noteCollectionRefs = refs,
        )

        // Act
        val json = BackupSerializer.toJson(originalData)
        val restoredData = BackupSerializer.fromJson(json)

        // Assert
        assertEquals(originalData.version, restoredData.version)
        assertEquals(originalData.createdAt, restoredData.createdAt)
        assertEquals(originalData.notes.size, restoredData.notes.size)
        assertEquals(originalData.collections.size, restoredData.collections.size)
        assertEquals(originalData.noteCollectionRefs.size, restoredData.noteCollectionRefs.size)

        assertEquals(originalData.notes[0].title, restoredData.notes[0].title)
        assertEquals(originalData.notes[0].isFavorite, restoredData.notes[0].isFavorite)
        assertEquals(originalData.notes[0].isSecure, restoredData.notes[0].isSecure)
        assertEquals(NoteType.TEXT, restoredData.notes[0].type)
        assertEquals(NoteType.CHECKLIST, restoredData.notes[1].type)
        assertEquals(originalData.notes[1].isDeleted, restoredData.notes[1].isDeleted)
        assertEquals(originalData.collections[0].name, restoredData.collections[0].name)
        assertEquals(originalData.noteCollectionRefs[0].noteId, restoredData.noteCollectionRefs[0].noteId)
    }

    @Test
    fun `fromJson should default old backups to text notes`() {
        val restoredData = BackupSerializer.fromJson(
            """
            {
              "version": 1,
              "createdAt": 999,
              "notes": [
                {
                  "id": 1,
                  "title": "Legacy",
                  "content": "Content",
                  "timestamp": 123,
                  "colorHex": 4294967295
                }
              ],
              "collections": [],
              "noteCollectionRefs": []
            }
            """.trimIndent(),
        )

        assertEquals(NoteType.TEXT, restoredData.notes.single().type)
    }
}
