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

data class NotePreviewRoute(val noteId: Int)

data class EditNoteRoute(val noteId: Int)
