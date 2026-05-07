package com.capystick.model

data class WidgetNoteSummary(
    val noteId: Int,
    val title: String,
    val preview: String?,
    val timestamp: Long,
)

sealed interface WidgetContentState {
    val title: String
    val configuration: WidgetConfiguration

    data class Content(
        override val title: String,
        override val configuration: WidgetConfiguration,
        val notes: List<WidgetNoteSummary>,
    ) : WidgetContentState

    data class EmptyNoNotes(
        override val title: String,
        override val configuration: WidgetConfiguration,
        val isCollectionContext: Boolean,
    ) : WidgetContentState

    data class EmptyNoCollections(
        override val configuration: WidgetConfiguration,
    ) : WidgetContentState {
        override val title: String = "Colecciones"
    }

    data class EmptyMissingCollection(
        override val title: String,
        override val configuration: WidgetConfiguration,
    ) : WidgetContentState
}
