package com.capystick.database.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.capystick.database.dao.NoteDao
import com.capystick.database.entities.NoteEntity
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CapystickDB : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}

class CreateDatabase(private val builder: RoomDatabase.Builder<CapystickDB>) {
    fun getDatabase(): CapystickDB{
        return builder
            .fallbackToDestructiveMigration(true)
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()

    }
}
