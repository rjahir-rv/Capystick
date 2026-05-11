package com.capystick.app.widget

import android.content.Context
import android.content.Intent
import com.capystick.app.MainActivity
import com.capystick.navigation.ExternalNavigationCommand

object WidgetNavigationIntents {
    private const val EXTRA_DESTINATION = "widget_destination"
    private const val EXTRA_NOTE_ID = "widget_note_id"
    private const val EXTRA_COLLECTION_ID = "widget_collection_id"
    private const val EXTRA_COLLECTION_NAME = "widget_collection_name"
    private const val EXTRA_APP_WIDGET_ID = "widget_app_widget_id"

    private const val DESTINATION_NOTES = "notes"
    private const val DESTINATION_CREATE_NOTE = "create_note"
    private const val DESTINATION_COLLECTIONS = "collections"
    private const val DESTINATION_COLLECTION = "collection"
    private const val DESTINATION_EDIT_NOTE = "edit_note"
    private const val DESTINATION_EDIT_COLLECTION_NOTE = "edit_collection_note"
    private const val DESTINATION_WIDGETS = "widgets"
    private const val DESTINATION_WIDGET_EDITOR = "widget_editor"

    fun openNotes(context: Context): Intent =
        baseIntent(context).putExtra(
            EXTRA_DESTINATION,
            DESTINATION_NOTES,
        )

    fun openCreateNote(context: Context): Intent =
        baseIntent(context).putExtra(
            EXTRA_DESTINATION,
            DESTINATION_CREATE_NOTE,
        )

    fun openCollections(context: Context): Intent =
        baseIntent(context).putExtra(
            EXTRA_DESTINATION,
            DESTINATION_COLLECTIONS,
        )

    fun openCollection(
        context: Context,
        collectionId: Int,
        collectionName: String,
    ): Intent =
        baseIntent(context)
            .putExtra(EXTRA_DESTINATION, DESTINATION_COLLECTION)
            .putExtra(EXTRA_COLLECTION_ID, collectionId)
            .putExtra(EXTRA_COLLECTION_NAME, collectionName)

    fun editRecentNote(
        context: Context,
        noteId: Int,
    ): Intent =
        baseIntent(context)
            .putExtra(EXTRA_DESTINATION, DESTINATION_EDIT_NOTE)
            .putExtra(EXTRA_NOTE_ID, noteId)

    fun editCollectionNote(
        context: Context,
        noteId: Int,
        collectionId: Int,
        collectionName: String,
    ): Intent =
        baseIntent(context)
            .putExtra(EXTRA_DESTINATION, DESTINATION_EDIT_COLLECTION_NOTE)
            .putExtra(EXTRA_NOTE_ID, noteId)
            .putExtra(EXTRA_COLLECTION_ID, collectionId)
            .putExtra(EXTRA_COLLECTION_NAME, collectionName)

    fun openWidgetManagement(context: Context): Intent =
        baseIntent(context).putExtra(
            EXTRA_DESTINATION,
            DESTINATION_WIDGETS,
        )

    fun openWidgetEditor(
        context: Context,
        appWidgetId: Int,
    ): Intent =
        baseIntent(context)
            .putExtra(EXTRA_DESTINATION, DESTINATION_WIDGET_EDITOR)
            .putExtra(EXTRA_APP_WIDGET_ID, appWidgetId)

    fun parseIntent(intent: Intent?): ExternalNavigationCommand? {
        intent ?: return null
        return when (intent.getStringExtra(EXTRA_DESTINATION)) {
            DESTINATION_NOTES -> ExternalNavigationCommand.OpenNotes
            DESTINATION_CREATE_NOTE -> ExternalNavigationCommand.OpenCreateNote
            DESTINATION_COLLECTIONS -> ExternalNavigationCommand.OpenCollections
            DESTINATION_COLLECTION -> {
                val collectionId = intent.getIntExtra(EXTRA_COLLECTION_ID, -2)
                val collectionName = intent.getStringExtra(EXTRA_COLLECTION_NAME)
                if (collectionId == -2 || collectionName.isNullOrBlank()) {
                    null
                } else {
                    ExternalNavigationCommand.OpenCollection(collectionId, collectionName)
                }
            }

            DESTINATION_EDIT_NOTE -> {
                val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1)
                if (noteId == -1) null else ExternalNavigationCommand.OpenEditRecentNote(noteId)
            }

            DESTINATION_EDIT_COLLECTION_NOTE -> {
                val noteId = intent.getIntExtra(EXTRA_NOTE_ID, -1)
                val collectionId = intent.getIntExtra(EXTRA_COLLECTION_ID, -2)
                val collectionName = intent.getStringExtra(EXTRA_COLLECTION_NAME)
                if (noteId == -1 || collectionId == -2 || collectionName.isNullOrBlank()) {
                    null
                } else {
                    ExternalNavigationCommand.OpenEditCollectionNote(
                        noteId = noteId,
                        collectionId = collectionId,
                        collectionName = collectionName,
                    )
                }
            }

            DESTINATION_WIDGETS -> ExternalNavigationCommand.OpenWidgetManagement
            DESTINATION_WIDGET_EDITOR -> {
                val appWidgetId = intent.getIntExtra(EXTRA_APP_WIDGET_ID, -1)
                if (appWidgetId == -1) null else ExternalNavigationCommand.OpenWidgetEditor(appWidgetId)
            }

            else -> null
        }
    }

    private fun baseIntent(context: Context): Intent =
        Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
}
