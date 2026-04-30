package com.capystick.database.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CollectionWithNotes(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteCollectionCrossRef::class,
            parentColumn = "collectionId",
            entityColumn = "noteId"
        )
    )
    val notes: List<NoteEntity>
)
