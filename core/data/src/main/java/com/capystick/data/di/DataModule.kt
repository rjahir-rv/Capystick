package com.capystick.data.di

import com.capystick.data.repository.BackupRepositoryImpl
import com.capystick.data.repository.CollectionRepositoryImpl
import com.capystick.data.repository.NoteRepositoryImpl
import com.capystick.domain.repository.BackupRepository
import com.capystick.domain.repository.CollectionRepository
import com.capystick.domain.repository.NoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryImpl,
    ): NoteRepository

    @Binds
    abstract fun bindCollectionRepository(
        collectionRepositoryImpl: CollectionRepositoryImpl,
    ): CollectionRepository

    @Binds
    abstract fun bindBackupRepository(
        backupRepositoryImpl: BackupRepositoryImpl,
    ): BackupRepository
}
