package com.capystick.database.di

import android.content.Context
import androidx.room.Room
import com.capystick.database.dao.NoteDao
import com.capystick.database.db.CapystickDB
import com.capystick.database.db.CreateDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CapystickDB {
        val builder = Room.databaseBuilder(
            context,
            CapystickDB::class.java,
            "capystick.db"
        )
        return CreateDatabase(builder).getDatabase()
    }

    @Provides
    fun provideNoteDao(database: CapystickDB): NoteDao {
        return database.noteDao()
    }
}
