package com.capystick.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.capystick.model.Collection

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)

fun CollectionEntity.toDomain(): Collection = Collection(
    id = id,
    name = name
)

fun Collection.toEntity(): CollectionEntity = CollectionEntity(
    id = id,
    name = name
)
