package com.capystick.domain.widget

import com.capystick.model.ChecklistFormatter
import com.capystick.model.Note
import com.capystick.model.NoteType
import javax.inject.Inject

class WidgetNotePreviewFormatter @Inject constructor() {
    fun format(note: Note): String? {
        val plainText = when (note.type) {
            NoteType.TEXT -> format(note.content)
            NoteType.CHECKLIST -> ChecklistFormatter.plainTextFromJson(note.content).takeIf { it.isNotBlank() }
        }

        return plainText?.take(MAX_PREVIEW_LENGTH)
    }

    fun format(html: String): String? {
        val plainText = html
            .replace(HTML_TAG_REGEX, " ")
            .decodeHtmlEntities()
            .replace(WHITESPACE_REGEX, " ")
            .trim()

        return plainText.takeIf { it.isNotBlank() }?.take(MAX_PREVIEW_LENGTH)
    }

    private fun String.decodeHtmlEntities(): String =
        replace(HTML_ENTITY_REGEX) { match ->
            val entity = match.groupValues[1]
            HTML_ENTITY_REPLACEMENTS[entity]
                ?: HTML_ENTITY_CODE_POINTS[entity]?.toCharacter()
                ?: entity.decodeNumericHtmlEntity()
                ?: match.value
        }

    private fun Int.toCharacter(): String? =
        if (Character.isValidCodePoint(this)) {
            String(Character.toChars(this))
        } else {
            null
        }

    private fun String.decodeNumericHtmlEntity(): String? {
        val codePoint = when {
            startsWith("#x", ignoreCase = true) -> drop(2).toIntOrNull(radix = 16)
            startsWith("#") -> drop(1).toIntOrNull(radix = 10)
            else -> null
        } ?: return null

        return codePoint.toCharacter()
    }

    @Suppress("SpellCheckingInspection")
    private companion object {
        const val MAX_PREVIEW_LENGTH = 80
        val HTML_TAG_REGEX = Regex("<[^>]+>")
        val HTML_ENTITY_REGEX = Regex("&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z][a-zA-Z0-9]+);")
        val WHITESPACE_REGEX = Regex("\\s+")
        val HTML_ENTITY_REPLACEMENTS = mapOf(
            "Tab" to "\t",
            "NewLine" to "\n",
            "excl" to "!",
            "quot" to "\"",
            "num" to "#",
            "dollar" to "$",
            "percnt" to "%",
            "amp" to "&",
            "apos" to "'",
            "lpar" to "(",
            "rpar" to ")",
            "ast" to "*",
            "midast" to "*",
            "comma" to ",",
            "period" to ".",
            "sol" to "/",
            "colon" to ":",
            "semi" to ";",
            "lt" to "<",
            "equals" to "=",
            "gt" to ">",
            "quest" to "?",
            "commat" to "@",
            "lsqb" to "[",
            "lbrack" to "[",
            "bsol" to "\\",
            "rsqb" to "]",
            "rbrack" to "]",
            "Hat" to "^",
            "lowbar" to "_",
            "grave" to "`",
            "lcub" to "{",
            "lbrace" to "{",
            "verbar" to "|",
            "vert" to "|",
            "rcub" to "}",
            "rbrace" to "}",
            "hyphen" to "-",
            "dash" to "-",
            "minus" to "-",
            "plus" to "+",
            "nbsp" to " ",
            "shy" to "",
            "ndash" to "-",
            "mdash" to "-",
            "lsquo" to "'",
            "rsquo" to "'",
            "ldquo" to "\"",
            "rdquo" to "\"",
            "hellip" to "...",
            "ensp" to " ",
            "emsp" to " ",
            "thinsp" to " ",
        )
        val HTML_ENTITY_CODE_POINTS = mapOf(
            "iexcl" to 0x00A1,
            "cent" to 0x00A2,
            "pound" to 0x00A3,
            "curren" to 0x00A4,
            "yen" to 0x00A5,
            "brvbar" to 0x00A6,
            "sect" to 0x00A7,
            "uml" to 0x00A8,
            "copy" to 0x00A9,
            "ordf" to 0x00AA,
            "laquo" to 0x00AB,
            "not" to 0x00AC,
            "reg" to 0x00AE,
            "macr" to 0x00AF,
            "deg" to 0x00B0,
            "plusmn" to 0x00B1,
            "sup2" to 0x00B2,
            "sup3" to 0x00B3,
            "acute" to 0x00B4,
            "micro" to 0x00B5,
            "para" to 0x00B6,
            "middot" to 0x00B7,
            "cedil" to 0x00B8,
            "sup1" to 0x00B9,
            "ordm" to 0x00BA,
            "raquo" to 0x00BB,
            "frac14" to 0x00BC,
            "frac12" to 0x00BD,
            "frac34" to 0x00BE,
            "iquest" to 0x00BF,
            "Agrave" to 0x00C0,
            "Aacute" to 0x00C1,
            "Acirc" to 0x00C2,
            "Atilde" to 0x00C3,
            "Auml" to 0x00C4,
            "Aring" to 0x00C5,
            "AElig" to 0x00C6,
            "Ccedil" to 0x00C7,
            "Egrave" to 0x00C8,
            "Eacute" to 0x00C9,
            "Ecirc" to 0x00CA,
            "Euml" to 0x00CB,
            "Igrave" to 0x00CC,
            "Iacute" to 0x00CD,
            "Icirc" to 0x00CE,
            "Iuml" to 0x00CF,
            "ETH" to 0x00D0,
            "Ntilde" to 0x00D1,
            "Ograve" to 0x00D2,
            "Oacute" to 0x00D3,
            "Ocirc" to 0x00D4,
            "Otilde" to 0x00D5,
            "Ouml" to 0x00D6,
            "times" to 0x00D7,
            "Oslash" to 0x00D8,
            "Ugrave" to 0x00D9,
            "Uacute" to 0x00DA,
            "Ucirc" to 0x00DB,
            "Uuml" to 0x00DC,
            "Yacute" to 0x00DD,
            "THORN" to 0x00DE,
            "szlig" to 0x00DF,
            "agrave" to 0x00E0,
            "aacute" to 0x00E1,
            "acirc" to 0x00E2,
            "atilde" to 0x00E3,
            "auml" to 0x00E4,
            "aring" to 0x00E5,
            "aelig" to 0x00E6,
            "ccedil" to 0x00E7,
            "egrave" to 0x00E8,
            "eacute" to 0x00E9,
            "ecirc" to 0x00EA,
            "euml" to 0x00EB,
            "igrave" to 0x00EC,
            "iacute" to 0x00ED,
            "icirc" to 0x00EE,
            "iuml" to 0x00EF,
            "eth" to 0x00F0,
            "ntilde" to 0x00F1,
            "ograve" to 0x00F2,
            "oacute" to 0x00F3,
            "ocirc" to 0x00F4,
            "otilde" to 0x00F5,
            "ouml" to 0x00F6,
            "divide" to 0x00F7,
            "oslash" to 0x00F8,
            "ugrave" to 0x00F9,
            "uacute" to 0x00FA,
            "ucirc" to 0x00FB,
            "uuml" to 0x00FC,
            "yacute" to 0x00FD,
            "thorn" to 0x00FE,
            "yuml" to 0x00FF,
            "bull" to 0x2022,
            "euro" to 0x20AC,
            "trade" to 0x2122,
        )
    }
}
