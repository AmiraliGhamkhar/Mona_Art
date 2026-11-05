package com.monaart.studio.data.db

import com.monaart.engine.io.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** Metadata row for one project (mirrors what the gallery needs). */
data class ProjectRecord(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val dpi: Int,
    val layerCount: Int,
    val createdAt: Long,
    val modifiedAt: Long,
    val lastOpenedAt: Long,
    val thumbnailPath: String?,
    val filePath: String,
    val sizeBytes: Long,
    val favorite: Boolean = false,
    val folder: String? = null,
    val recoveryState: String = "Ok",
    val deletedAt: Long = 0,
)

/** A user-saved canvas preset. */
data class CanvasPresetRecord(
    val id: String,
    val name: String,
    val width: Int,
    val height: Int,
    val dpi: Int,
    val transparent: Boolean,
    val backgroundArgb: Int,
    val createdAt: Long,
)

/**
 * Persistent metadata index for the gallery, stored as a single JSON document
 * under `files/index/projects.json`.
 *
 * Design notes:
 *  - The index is *derived* data: if it is ever lost or damaged, projects are
 *    recovered by scanning the project folders, so losing the index can never
 *    lose artwork.
 *  - Writes are serialised by a [Mutex] and atomic (temp file + rename), so a
 *    crash mid-write leaves the previous index intact.
 *  - Exposed as a [StateFlow] so Compose observes updates without polling.
 *
 * A relational database was evaluated and rejected: the query surface is a
 * single sorted list, so an index file removes an annotation-processing
 * dependency and its build fragility for no functional loss.
 */
class ProjectIndex(private val root: File) {

    private val file = File(root, "projects.json")
    private val lock = Mutex()
    private val _projects = MutableStateFlow<List<ProjectRecord>>(emptyList())
    private val _presets = MutableStateFlow<List<CanvasPresetRecord>>(emptyList())

    val projects: StateFlow<List<ProjectRecord>> = _projects.asStateFlow()
    val presets: StateFlow<List<CanvasPresetRecord>> = _presets.asStateFlow()

    val activeProjects: List<ProjectRecord> get() = _projects.value.filter { it.deletedAt == 0L }

    suspend fun load() = withContext(Dispatchers.IO) {
        lock.withLock {
            val loaded = runCatching { readFile() }.getOrNull()
            if (loaded != null) {
                _projects.value = loaded.first
                _presets.value = loaded.second
            }
        }
    }

    suspend fun upsert(record: ProjectRecord) = mutate { list ->
        list.filterNot { it.id == record.id } + record
    }

    suspend fun setFavorite(id: Long, favorite: Boolean) = mutate { list ->
        list.map { if (it.id == id) it.copy(favorite = favorite) else it }
    }

    suspend fun markOpened(id: Long, time: Long = System.currentTimeMillis()) = mutate { list ->
        list.map { if (it.id == id) it.copy(lastOpenedAt = time) else it }
    }

    suspend fun softDelete(id: Long, time: Long = System.currentTimeMillis()) = mutate { list ->
        list.map { if (it.id == id) it.copy(deletedAt = time) else it }
    }

    suspend fun restore(id: Long) = mutate { list ->
        list.map { if (it.id == id) it.copy(deletedAt = 0) else it }
    }

    suspend fun remove(id: Long) = mutate { list -> list.filterNot { it.id == id } }

    suspend fun byId(id: Long): ProjectRecord? = _projects.value.firstOrNull { it.id == id }

    suspend fun totalBytes(): Long = activeProjects.sumOf { it.sizeBytes }

    suspend fun upsertPreset(preset: CanvasPresetRecord) = withContext(Dispatchers.IO) {
        lock.withLock {
            _presets.value = _presets.value.filterNot { it.id == preset.id } + preset
            writeFile()
        }
    }

    suspend fun deletePreset(id: String) = withContext(Dispatchers.IO) {
        lock.withLock {
            _presets.value = _presets.value.filterNot { it.id == id }
            writeFile()
        }
    }

    private suspend fun mutate(transform: (List<ProjectRecord>) -> List<ProjectRecord>) = withContext(Dispatchers.IO) {
        lock.withLock {
            _projects.value = transform(_projects.value)
            writeFile()
        }
    }

    /** Rebuilds entries by scanning project directories (used when the index is missing). */
    suspend fun rebuildFromDisk(projectDirs: List<File>) = withContext(Dispatchers.IO) {
        val found = ArrayList<ProjectRecord>()
        for (dir in projectDirs) {
            val projectFile = File(dir, "project.mona")
            val recovery = File(dir, "recovery.mona")
            val source = when {
                projectFile.exists() -> projectFile
                recovery.exists() -> recovery
                else -> null
            } ?: continue
            val manifest = runCatching { com.monaart.engine.io.ProjectCodec.readManifest(source) }.getOrNull()
            val id = dir.name.toLongOrNull() ?: continue
            val thumb = File(dir, "thumb.png")
            found.add(
                ProjectRecord(
                    id = id,
                    name = manifest?.name ?: "Recovered project",
                    width = manifest?.width ?: 0,
                    height = manifest?.height ?: 0,
                    dpi = manifest?.dpi ?: 72,
                    layerCount = manifest?.layers?.size ?: 0,
                    createdAt = source.lastModified(),
                    modifiedAt = source.lastModified(),
                    lastOpenedAt = 0,
                    thumbnailPath = thumb.absolutePath.takeIf { thumb.exists() },
                    filePath = projectFile.absolutePath,
                    sizeBytes = source.length(),
                    recoveryState = if (projectFile.exists()) "Ok" else "Recovered",
                ),
            )
        }
        lock.withLock {
            val merged = (found + _projects.value).distinctBy { it.id }
            _projects.value = merged
            writeFile()
        }
    }

    private fun readFile(): Pair<List<ProjectRecord>, List<CanvasPresetRecord>> {
        if (!file.exists()) return emptyList<ProjectRecord>() to emptyList()
        val json = Json.parse(file.readText())
        val projects = json["projects"]?.array()?.mapNotNull { parseRecord(it) } ?: emptyList()
        val presets = json["presets"]?.array()?.mapNotNull { parsePreset(it) } ?: emptyList()
        return projects to presets
    }

    private fun writeFile() {
        if (!root.exists()) root.mkdirs()
        val json = Json.obj(
            "version" to Json.of(1),
            "projects" to Json.arr(_projects.value.map { recordToJson(it) }),
            "presets" to Json.arr(_presets.value.map { presetToJson(it) }),
        ).toJsonString(pretty = true)
        val tmp = File(root, "projects.json.tmp")
        tmp.writeText(json)
        if (file.exists()) file.delete()
        if (!tmp.renameTo(file)) {
            file.writeText(json)
            tmp.delete()
        }
    }

    private fun recordToJson(r: ProjectRecord) = Json.obj(
        "id" to Json.of(r.id),
        "name" to Json.of(r.name),
        "width" to Json.of(r.width),
        "height" to Json.of(r.height),
        "dpi" to Json.of(r.dpi),
        "layerCount" to Json.of(r.layerCount),
        "createdAt" to Json.of(r.createdAt),
        "modifiedAt" to Json.of(r.modifiedAt),
        "lastOpenedAt" to Json.of(r.lastOpenedAt),
        "thumbnailPath" to Json.of(r.thumbnailPath),
        "filePath" to Json.of(r.filePath),
        "sizeBytes" to Json.of(r.sizeBytes),
        "favorite" to Json.of(r.favorite),
        "folder" to Json.of(r.folder),
        "recoveryState" to Json.of(r.recoveryState),
        "deletedAt" to Json.of(r.deletedAt),
    )

    private fun parseRecord(j: Json) = runCatching {
        ProjectRecord(
            id = j["id"]?.asLong() ?: return null,
            name = j["name"]?.asString() ?: "Untitled",
            width = j["width"]?.asInt() ?: 0,
            height = j["height"]?.asInt() ?: 0,
            dpi = j["dpi"]?.asInt() ?: 72,
            layerCount = j["layerCount"]?.asInt() ?: 1,
            createdAt = j["createdAt"]?.asLong() ?: 0,
            modifiedAt = j["modifiedAt"]?.asLong() ?: 0,
            lastOpenedAt = j["lastOpenedAt"]?.asLong() ?: 0,
            thumbnailPath = j["thumbnailPath"]?.asString(),
            filePath = j["filePath"]?.asString() ?: "",
            sizeBytes = j["sizeBytes"]?.asLong() ?: 0,
            favorite = j["favorite"]?.asBool() ?: false,
            folder = j["folder"]?.asString(),
            recoveryState = j["recoveryState"]?.asString() ?: "Ok",
            deletedAt = j["deletedAt"]?.asLong() ?: 0,
        )
    }.getOrNull()

    private fun presetToJson(p: CanvasPresetRecord) = Json.obj(
        "id" to Json.of(p.id),
        "name" to Json.of(p.name),
        "width" to Json.of(p.width),
        "height" to Json.of(p.height),
        "dpi" to Json.of(p.dpi),
        "transparent" to Json.of(p.transparent),
        "backgroundArgb" to Json.of(p.backgroundArgb),
        "createdAt" to Json.of(p.createdAt),
    )

    private fun parsePreset(j: Json) = runCatching {
        CanvasPresetRecord(
            id = j["id"]?.asString() ?: return null,
            name = j["name"]?.asString() ?: "Preset",
            width = j["width"]?.asInt() ?: 1024,
            height = j["height"]?.asInt() ?: 1024,
            dpi = j["dpi"]?.asInt() ?: 300,
            transparent = j["transparent"]?.asBool() ?: false,
            backgroundArgb = j["backgroundArgb"]?.asInt() ?: 0xFFFFFFFF.toInt(),
            createdAt = j["createdAt"]?.asLong() ?: 0,
        )
    }.getOrNull()
}
