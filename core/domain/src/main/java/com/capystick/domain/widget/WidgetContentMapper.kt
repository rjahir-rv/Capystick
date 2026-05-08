package com.capystick.domain.widget

import com.capystick.model.Note
import com.capystick.model.WidgetConfiguration
import com.capystick.model.WidgetContentState
import com.capystick.model.WidgetNoteSummary
import javax.inject.Inject

class WidgetContentMapper @Inject constructor(
    private val notePreviewFormatter: WidgetNotePreviewFormatter,
) {
    fun recentNotes(
        configuration: WidgetConfiguration,
        notes: List<Note>,
    ): WidgetContentState =
        notes.toWidgetNotes().toWidgetState(
            title = "Notas recientes",
            configuration = configuration,
            isCollectionContext = false,
        )

    fun collectionNotes(
        configuration: WidgetConfiguration,
        collectionName: String,
        notes: List<Note>,
    ): WidgetContentState =
        notes.toWidgetNotes().toWidgetState(
            title = collectionName,
            configuration = configuration,
            isCollectionContext = true,
        )

    private fun List<Note>.toWidgetNotes(): List<WidgetNoteSummary> =
        filterNot(Note::isSecure)
            .sortedByDescending(Note::timestamp)
            .take(MAX_WIDGET_NOTES)
            .map { it.toWidgetSummary() }

    private fun List<WidgetNoteSummary>.toWidgetState(
        title: String,
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
            preview = notePreviewFormatter.format(content),
            timestamp = timestamp,
        )

    private companion object {
        const val MAX_WIDGET_NOTES = 6
    }
}
