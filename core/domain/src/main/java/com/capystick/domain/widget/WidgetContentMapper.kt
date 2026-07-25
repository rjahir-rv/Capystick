package com.capystick.domain.widget

import com.capystick.model.Note
import com.capystick.model.WidgetConfiguration
import com.capystick.model.WidgetContentState
import com.capystick.model.WidgetNoteSummary
import com.capystick.model.WidgetTitle
import javax.inject.Inject

class WidgetContentMapper @Inject constructor(
    private val notePreviewFormatter: WidgetNotePreviewFormatter,
) {
    fun recentNotes(
        configuration: WidgetConfiguration,
        notes: List<Note>,
    ): WidgetContentState =
        notes.toWidgetNotes().toWidgetState(
            title = WidgetTitle.RecentNotes,
            configuration = configuration,
            isCollectionContext = false,
        )

    fun collectionNotes(
        configuration: WidgetConfiguration,
        collectionName: String,
        notes: List<Note>,
    ): WidgetContentState =
        notes.toWidgetNotes().toWidgetState(
            title = if (configuration.collectionId == FAVORITES_COLLECTION_ID) {
                WidgetTitle.Favorites
            } else {
                WidgetTitle.Text(collectionName)
            },
            configuration = configuration,
            isCollectionContext = true,
        )

    private fun List<Note>.toWidgetNotes(): List<WidgetNoteSummary> =
        filterNot(Note::isSecure)
            .sortedByDescending(Note::timestamp)
            .take(MAX_WIDGET_NOTES)
            .map { it.toWidgetSummary() }

    private fun List<WidgetNoteSummary>.toWidgetState(
        title: WidgetTitle,
        configuration: WidgetConfiguration,
        isCollectionContext: Boolean,
    ): WidgetContentState =
        if (isEmpty()) {
            WidgetContentState.EmptyNoNotes(
                title = title,
                configuration = configuration,
                isCollectionContext = isCollectionContext,
            )
        } else {
            WidgetContentState.Content(
                title = title,
                configuration = configuration,
                notes = this,
            )
        }

    private fun Note.toWidgetSummary(): WidgetNoteSummary =
        WidgetNoteSummary(
            noteId = id,
            title = title,
            preview = notePreviewFormatter.format(this),
            timestamp = timestamp,
        )

    private companion object {
        const val MAX_WIDGET_NOTES = 6
        const val FAVORITES_COLLECTION_ID = -1
    }
}
