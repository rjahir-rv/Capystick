package com.capystick.domain.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class WidgetNotePreviewFormatterTest {
    private val formatter = WidgetNotePreviewFormatter()

    @Test
    fun `strips html and keeps plain text`() {
        val result = formatter.format("<p>Hola <b>mundo</b></p>")

        assertEquals("Hola mundo", result)
    }

    @Test
    fun `decodes html entities for accents and punctuation`() {
        val result = formatter.format(
            "<p>&iquest;Qu&eacute; tal? &quot;Adi&oacute;s&quot; &amp; caf&#233;</p>",
        )

        assertEquals("\u00BFQu\u00E9 tal? \"Adi\u00F3s\" & caf\u00E9", result)
    }

    @Test
    fun `decodes comma and plus html entities`() {
        val result = formatter.format("<p>uno&comma; dos &plus; tres</p>")

        assertEquals("uno, dos + tres", result)
    }

    @Test
    fun `decodes common punctuation html entities`() {
        val result = formatter.format(
            "<p>hola&period; espera&semi; suma &equals; a&lpar;b&rpar;&colon; ok&excl;</p>",
        )

        assertEquals("hola. espera; suma = a(b): ok!", result)
    }
}
