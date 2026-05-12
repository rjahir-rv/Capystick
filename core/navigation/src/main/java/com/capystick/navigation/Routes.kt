package com.capystick.navigation

import androidx.annotation.StringRes

sealed interface TopLevelRoute {
    @get:StringRes
    val titleRes: Int
}

data object NotepadRoute : TopLevelRoute {
    override val titleRes = R.string.route_create_note
}

data class ChecklistRoute(val initialTitle: String = "") : TopLevelRoute {
    override val titleRes = R.string.route_create_checklist
}

data object CollectionsRoute : TopLevelRoute {
    override val titleRes = R.string.route_collections
}

data object SettingsRoute : TopLevelRoute {
    override val titleRes = R.string.route_settings
}

data object NotesRoute : TopLevelRoute {
    override val titleRes = R.string.route_all_notes
}

data object ScanRoute : TopLevelRoute {
    override val titleRes = R.string.route_scan_note
}

data class NotePreviewRoute(val noteId: Int, val isUnlocked: Boolean = false)

data class EditNoteRoute(val noteId: Int, val isUnlocked: Boolean = false)

data class EditChecklistRoute(val noteId: Int, val isUnlocked: Boolean = false)

data class CollectionNotesRoute(
    val collectionId: Int,
    val collectionName: String
)

data object FavoriteNotesRoute

data class CreateCollectionNoteRoute(val collectionId: Int)

data class CreateCollectionChecklistRoute(
    val collectionId: Int,
    val initialTitle: String = "",
)

data object TrashRoute

data object BackupRoute

data object WidgetManagementRoute

data class WidgetConfigurationRoute(val appWidgetId: Int)
