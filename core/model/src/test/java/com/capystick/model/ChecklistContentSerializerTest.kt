package com.capystick.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistContentSerializerTest {

    @Test
    fun `toJson and fromJson preserve checklist items`() {
        val content = ChecklistContent(
            items = listOf(
                ChecklistItem(id = "first", text = "Comprar leche", checked = false),
                ChecklistItem(id = "second", text = "Pagar luz", checked = true),
            ),
        )

        val restored = ChecklistContentSerializer.fromJson(
            ChecklistContentSerializer.toJson(content),
        )

        assertEquals(content, restored)
    }

    @Test
    fun `fromJson returns empty content for blank or malformed json`() {
        assertTrue(ChecklistContentSerializer.fromJson("").items.isEmpty())
        assertTrue(ChecklistContentSerializer.fromJson("not-json").items.isEmpty())
    }

    @Test
    fun `formatter renders checklist preview lines and progress`() {
        val content = ChecklistContent(
            items = listOf(
                ChecklistItem(id = "first", text = "Comprar leche", checked = false),
                ChecklistItem(id = "second", text = "Pagar luz", checked = true),
                ChecklistItem(id = "empty", text = "", checked = false),
            ),
        )

        assertEquals("[ ] Comprar leche\n[x] Pagar luz", ChecklistFormatter.toPlainText(content))
        assertEquals("1/3 completados", ChecklistFormatter.progressText(content))
    }
}
