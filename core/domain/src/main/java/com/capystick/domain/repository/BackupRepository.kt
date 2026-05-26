package com.capystick.domain.repository

import com.capystick.model.BackupData

interface BackupRepository {
    suspend fun exportBackup(): BackupData

    suspend fun exportBackupJson(): String

    suspend fun importBackup(backupData: BackupData)

    suspend fun importBackupJson(json: String)

    /** Returns true if there is at least one non-deleted note in the database. */
    suspend fun hasActiveNotes(): Boolean
}
