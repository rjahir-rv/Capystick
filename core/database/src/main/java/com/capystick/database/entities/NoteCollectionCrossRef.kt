package com.capystick.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "note_collection_cross_ref",
    primaryKeys = ["noteId", "collectionId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["noteId"]),
        Index(value = ["collectionId"])
    ]
)
data class NoteCollectionCrossRef(
    val noteId: Int,
    val collectionId: Int
)
