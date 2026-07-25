package com.capystick.domain.widget

import com.capystick.model.ChecklistContent
import com.capystick.model.ChecklistContentSerializer
import com.capystick.model.ChecklistItem
import com.capystick.model.Note
import com.capystick.model.NoteType
import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetNotePreviewFormatterChecklistTest {

    private val formatter = WidgetNotePreviewFormatter()

    @Test
    fun `format should render checklist notes as plain checklist lines`() {
        val note = Note(
            id = 1,
            title = "Compras",
            content = ChecklistContentSerializer.toJson(
                ChecklistContent(
                    items = listOf(
                        ChecklistItem(id = "1", text = "Cafe", checked = false),
                        ChecklistItem(id = "2", text = "Pan", checked = true),
                    ),
                ),
            ),
            timestamp = 123L,
            colorHex = 0xFFFFFFFF,
            type = NoteType.CHECKLIST,
        )

        assertEquals("[ ] Cafe\n[x] Pan", formatter.format(note))
    }
}
