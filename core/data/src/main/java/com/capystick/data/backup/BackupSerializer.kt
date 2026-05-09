package com.capystick.data.backup

import com.capystick.model.BackupData
import com.capystick.model.Collection
import com.capystick.model.Note
import com.capystick.model.NoteCollectionRef
import com.capystick.model.NoteType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializes and deserializes [BackupData] to/from JSON using Android's built-in
 * [org.json] API — no extra dependencies required.
 */
object BackupSerializer {

    // ── Serialization ─────────────────────────────────────────────────────────

    fun toJson(data: BackupData): String {
        val root = JSONObject().apply {
            put("version", data.version)
            put("createdAt", data.createdAt)
            put("notes", notesToJson(data.notes))
            put("collections", collectionsToJson(data.collections))
            put("noteCollectionRefs", refsToJson(data.noteCollectionRefs))
        }
        return root.toString(2)
    }

    private fun notesToJson(notes: List<Note>): JSONArray {
        val arr = JSONArray()
        notes.forEach { note ->
            arr.put(
                JSONObject().apply {
                    put("id", note.id)
                    put("title", note.title)
                    put("content", note.content)
                    put("timestamp", note.timestamp)
                    put("colorHex", note.colorHex)
                    put("type", note.type.name)
                    put("isDeleted", note.isDeleted)
                    put("isFavorite", note.isFavorite)
                    put("isSecure", note.isSecure)
                },
            )
        }
        return arr
    }

    private fun collectionsToJson(collections: List<Collection>): JSONArray {
        val arr = JSONArray()
        collections.forEach { col ->
            arr.put(
                JSONObject().apply {
                    put("id", col.id)
                    put("name", col.name)
                },
            )
        }
        return arr
    }

    private fun refsToJson(refs: List<NoteCollectionRef>): JSONArray {
        val arr = JSONArray()
        refs.forEach { ref ->
            arr.put(
                JSONObject().apply {
                    put("noteId", ref.noteId)
                    put("collectionId", ref.collectionId)
                },
            )
        }
        return arr
    }

    // ── Deserialization ───────────────────────────────────────────────────────

    fun fromJson(json: String): BackupData {
        val root = JSONObject(json)
        val version = root.optInt("version", 1)
        val createdAt = root.optLong("createdAt", 0L)

        val notes = parseNotes(root.optJSONArray("notes") ?: JSONArray())
        val collections = parseCollections(root.optJSONArray("collections") ?: JSONArray())
        val refs = parseRefs(root.optJSONArray("noteCollectionRefs") ?: JSONArray())

        return BackupData(
            version = version,
            createdAt = createdAt,
            notes = notes,
            collections = collections,
            noteCollectionRefs = refs,
        )
    }

    private fun parseNotes(arr: JSONArray): List<Note> {
        val notes = mutableListOf<Note>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            notes += Note(
                id = obj.getInt("id"),
                title = obj.getString("title"),
                content = obj.getString("content"),
                timestamp = obj.getLong("timestamp"),
                colorHex = obj.getLong("colorHex"),
                type = runCatching {
                    NoteType.valueOf(obj.optString("type", NoteType.TEXT.name))
                }.getOrDefault(NoteType.TEXT),
                isDeleted = obj.optBoolean("isDeleted", false),
                isFavorite = obj.optBoolean("isFavorite", false),
                isSecure = obj.optBoolean("isSecure", false),
            )
        }
        return notes
    }

    private fun parseCollections(arr: JSONArray): List<Collection> {
        val cols = mutableListOf<Collection>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            cols += Collection(
                id = obj.getInt("id"),
                name = obj.getString("name"),
            )
        }
        return cols
    }

    private fun parseRefs(arr: JSONArray): List<NoteCollectionRef> {
        val refs = mutableListOf<NoteCollectionRef>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            refs += NoteCollectionRef(
                noteId = obj.getInt("noteId"),
                collectionId = obj.getInt("collectionId"),
            )
        }
        return refs
    }
}
