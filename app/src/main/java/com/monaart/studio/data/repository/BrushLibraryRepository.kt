package com.monaart.studio.data.repository

import android.content.Context
import com.monaart.engine.brush.BrushCodec
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.BrushSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Brush presets: built-ins from the engine plus user presets stored as JSON in
 * `files/brushes/<id>.json`. Keeps the in-memory list hot so the editor never
 * blocks on disk while the artist is picking a brush.
 */
class BrushLibraryRepository(private val context: Context) {
    private val dir: File by lazy { File(context.filesDir, "brushes").apply { mkdirs() } }

    private val _presets = MutableStateFlow<List<BrushSpec>>(BrushPresets.all)
    val presets: StateFlow<List<BrushSpec>> = _presets.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    fun byId(id: String): BrushSpec? = _presets.value.firstOrNull { it.id == id } ?: BrushPresets.byId(id)

    /** Loads user presets (called once at startup). */
    suspend fun ensureBuiltIns() = withContext(Dispatchers.IO) {
        val user = dir.listFiles { f -> f.extension == "json" }?.mapNotNull { f ->
            runCatching { BrushCodec.decode(f.readText()) }.getOrNull()
        } ?: emptyList()
        val builtInIds = BrushPresets.all.map { it.id }.toSet()
        _presets.value = BrushPresets.all + user.filter { it.id !in builtInIds }
        readFavorites()
    }

    suspend fun save(preset: BrushSpec): BrushSpec = withContext(Dispatchers.IO) {
        // A saved preset is always a user preset: give it a stable user id if it
        // is a copy of a built-in brush.
        val id = if (preset.id.startsWith("user.") || preset.id.startsWith("custom.")) {
            preset.id
        } else {
            "user.${preset.name.lowercase().replace(Regex("[^a-z0-9]+"), "-")}-${System.currentTimeMillis() % 100000}"
        }
        val spec = preset.copy(id = id)
        dir.resolve("$id.json").writeText(BrushCodec.encode(spec))
        val current = _presets.value.filterNot { it.id == id }
        _presets.value = current + spec
        spec
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        if (!id.startsWith("user.")) return@withContext // built-ins are not deletable, only hideable
        dir.resolve("$id.json").delete()
        _presets.value = _presets.value.filterNot { it.id == id }
    }

    suspend fun importPresets(text: String): List<BrushSpec> = withContext(Dispatchers.IO) {
        // Accept either a single preset or a JSON array of presets.
        val json = com.monaart.engine.io.Json.parse(text)
        val items = if (json is com.monaart.engine.io.Json.Arr) json.items else listOf(json)
        items.mapNotNull { runCatching { BrushCodec.fromJson(it) }.getOrNull() }.map { save(it) }
    }

    fun exportPreset(preset: BrushSpec): String = BrushCodec.encode(preset)

    fun exportAll(): String = _presets.value.joinToString(prefix = "[", postfix = "]") { BrushCodec.encode(it) }

    suspend fun toggleFavorite(id: String): Boolean = withContext(Dispatchers.IO) {
        val next = _favorites.value.toMutableSet()
        val added = if (next.contains(id)) {
            next.remove(id)
            false
        } else {
            next.add(id)
            true
        }
        _favorites.value = next
        context.getSharedPreferences("brush_favorites", Context.MODE_PRIVATE).edit()
            .putStringSet("ids", next).apply()
        added
    }

    private fun readFavorites() {
        val saved = context.getSharedPreferences("brush_favorites", Context.MODE_PRIVATE)
            .getStringSet("ids", emptySet()) ?: emptySet()
        _favorites.value = saved.toSet()
    }
}
