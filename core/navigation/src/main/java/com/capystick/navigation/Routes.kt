package com.capystick.navigation

sealed interface TopLevelRoute {
    val title: String
}

data object NotepadRoute : TopLevelRoute {
    override val title = "Crear nota"
}

data object CollectionsRoute : TopLevelRoute {
    override val title = "Colecciones"
}

data object SettingsRoute : TopLevelRoute {
    override val title = "Ajustes"
}

data object NotesRoute : TopLevelRoute {
    override val title = "Todas las notas"
}

data object ScanRoute : TopLevelRoute {
    override val title = "Escanear nota"
}

data class NotePreviewRoute(val noteId: Int)

data class EditNoteRoute(val noteId: Int)

data class CollectionNotesRoute(
    val collectionId: Int,
    val collectionName: String
)

data object FavoriteNotesRoute

data class CreateCollectionNoteRoute(val collectionId: Int)

data object TrashRoute

data object BackupRoute

data object WidgetManagementRoute

data class WidgetConfigurationRoute(val appWidgetId: Int)
