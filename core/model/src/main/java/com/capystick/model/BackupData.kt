package com.capystick.model

data class BackupData(
    val version: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: List<Note>,
    val collections: List<Collection>,
    val noteCollectionRefs: List<NoteCollectionRef>,
)

data class NoteCollectionRef(
    val noteId: Int,
    val collectionId: Int,
)
