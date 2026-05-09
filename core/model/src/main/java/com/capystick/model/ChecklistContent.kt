package com.capystick.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ChecklistContent(
    val items: List<ChecklistItem> = emptyList(),
)

data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val checked: Boolean = false,
)

object ChecklistContentSerializer {
    private const val VERSION = 1

    fun toJson(content: ChecklistContent): String {
        val items = JSONArray()
        content.items.forEach { item ->
            items.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("text", item.text)
                    put("checked", item.checked)
                },
            )
        }

        return JSONObject()
            .put("version", VERSION)
            .put("items", items)
            .toString()
    }

    fun fromJson(json: String): ChecklistContent {
        if (json.isBlank()) return ChecklistContent()

        return runCatching {
            val root = JSONObject(json)
            val items = root.optJSONArray("items") ?: JSONArray()
            ChecklistContent(
                items = buildList {
                    for (index in 0 until items.length()) {
                        val item = items.optJSONObject(index) ?: continue
                        add(
                            ChecklistItem(
                                id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                                text = item.optString("text"),
                                checked = item.optBoolean("checked", false),
                            ),
                        )
                    }
                },
            )
        }.getOrDefault(ChecklistContent())
    }
}

object ChecklistFormatter {
    fun toPlainText(content: ChecklistContent): String {
        return content.items
            .filter { it.text.isNotBlank() }
            .joinToString(separator = "\n") { item ->
                val marker = if (item.checked) "[x]" else "[ ]"
                "$marker ${item.text.trim()}"
            }
    }

    fun progressText(content: ChecklistContent): String {
        val total = content.items.size
        val completed = content.items.count(ChecklistItem::checked)
        return "$completed/$total completados"
    }

    fun plainTextFromJson(json: String): String =
        toPlainText(ChecklistContentSerializer.fromJson(json))

    fun progressTextFromJson(json: String): String =
        progressText(ChecklistContentSerializer.fromJson(json))
}
