package com.capystick.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.capystick.database.dao.CollectionDao
import com.capystick.database.dao.NoteDao
import com.capystick.database.entities.CollectionEntity
import com.capystick.database.entities.NoteCollectionCrossRef
import com.capystick.database.entities.NoteEntity
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        NoteEntity::class,
        CollectionEntity::class,
        NoteCollectionCrossRef::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CapystickDB : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun collectionDao(): CollectionDao
}

class CreateDatabase(private val builder: RoomDatabase.Builder<CapystickDB>) {
    fun getDatabase(): CapystickDB{
        return builder
            .fallbackToDestructiveMigration(true)
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    }
}
