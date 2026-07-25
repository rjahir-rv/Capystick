package com.capystick.model

data class WidgetNoteSummary(
    val noteId: Int,
    val title: String,
    val preview: String?,
    val timestamp: Long,
)

sealed interface WidgetContentState {
    val title: WidgetTitle
    val configuration: WidgetConfiguration

    data class Content(
        override val title: WidgetTitle,
        override val configuration: WidgetConfiguration,
        val notes: List<WidgetNoteSummary>,
    ) : WidgetContentState

    data class EmptyNoNotes(
        override val title: WidgetTitle,
        override val configuration: WidgetConfiguration,
        val isCollectionContext: Boolean,
    ) : WidgetContentState

    data class EmptyNoCollections(
        override val configuration: WidgetConfiguration,
    ) : WidgetContentState {
        override val title: WidgetTitle = WidgetTitle.Collections
    }

    data class EmptyMissingCollection(
        override val title: WidgetTitle,
        override val configuration: WidgetConfiguration,
    ) : WidgetContentState
}

sealed interface WidgetTitle {
    data object RecentNotes : WidgetTitle
    data object Favorites : WidgetTitle
    data object Collections : WidgetTitle
    data object CollectionFallback : WidgetTitle
    data class Text(val value: String) : WidgetTitle
}
