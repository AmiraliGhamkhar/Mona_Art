package com.monaart.studio.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.monaart.engine.document.ArtDocument
import com.monaart.engine.io.ProjectCodec
import com.monaart.studio.data.db.ProjectIndex
import com.monaart.studio.data.db.ProjectRecord
import com.monaart.studio.domain.model.CanvasSpec
import com.monaart.studio.domain.model.ProjectSummary
import com.monaart.studio.domain.model.RecoveryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Owns project files on disk plus their metadata rows.
 *
 * Layout:
 * ```
 * files/projects/<id>/project.mona      current project
 * files/projects/<id>/project.mona.bak  previous good save
 * files/projects/<id>/recovery.mona     crash-recovery snapshot (autosave)
 * files/projects/<id>/thumb.png         gallery thumbnail
 * ```
 * Every save writes a temp file, fsyncs, then renames, so a process death can
 * never leave a half-written project behind. Autosave writes the recovery
 * snapshot only (cheap), and the full project is written on pause/export/close.
 */
class ProjectRepository(
    private val context: Context,
    private val index: ProjectIndex,
) {
    private val root: File by lazy {
        File(context.filesDir, "projects").apply { mkdirs() }
    }

    private val projectsRoot: File by lazy {
        root.apply { mkdirs() }
    }

    /** Ensures the index reflects what is actually on disk (recovers lost indexes). */
    suspend fun sync() {
        index.load()
        if (index.activeProjects.isEmpty()) {
            val dirs = projectsRoot.listFiles { f -> f.isDirectory }?.toList() ?: emptyList()
            if (dirs.isNotEmpty()) index.rebuildFromDisk(dirs)
        }
    }

    fun projectDir(id: Long): File = File(root, id.toString()).apply { mkdirs() }

    fun projectFile(id: Long): File = File(projectDir(id), "project.mona")

    private fun recoveryFile(id: Long): File = File(projectDir(id), "recovery.mona")

    private fun thumbFile(id: Long): File = File(projectDir(id), "thumb.png")

    val summaries: Flow<List<ProjectSummary>> =
        index.projects.map { rows -> rows.filter { it.deletedAt == 0L }.map { it.toSummary() } }

    suspend fun all(): List<ProjectSummary> = index.activeProjects.map { it.toSummary() }

    suspend fun get(id: Long): ProjectSummary? = index.byId(id)?.toSummary()

    suspend fun setFavorite(id: Long, favorite: Boolean) = index.setFavorite(id, favorite)

    suspend fun markOpened(id: Long) = index.markOpened(id, System.currentTimeMillis())

    suspend fun trash(id: Long) = index.softDelete(id, System.currentTimeMillis())

    suspend fun restore(id: Long) = index.restore(id)

    suspend fun storageBytes(): Long = index.totalBytes()

    /** Creates a new empty project on disk and indexes it. */
    suspend fun create(spec: CanvasSpec): ArtDocument = withContext(Dispatchers.IO) {
        val id = System.currentTimeMillis() * 1000 + (0..999).random()
        val doc = ArtDocument(
            id = id,
            name = spec.name.ifBlank { "Untitled" },
            width = spec.width,
            height = spec.height,
            dpi = spec.dpi,
        )
        doc.transparentBackground = spec.transparent
        doc.backgroundColor = spec.backgroundArgb
        val layer = com.monaart.engine.document.Layer(doc.newLayerId(), "Layer 1", com.monaart.engine.document.TileSurface(spec.width, spec.height))
        doc.layers.add(layer)
        doc.activeLayerId = layer.id
        doc.bumpStructure()
        if (!spec.transparent) {
            com.monaart.engine.document.LayerOps.fillLayer(doc, layer, spec.backgroundArgb, useSelection = false)
            doc.history.clear()
        }
        save(doc, makeThumbnail = true)
        doc
    }

    /** Writes the project atomically with its thumbnail, then updates metadata. */
    suspend fun save(doc: ArtDocument, makeThumbnail: Boolean = true): ProjectSummary = withContext(Dispatchers.IO) {
        val bytes = ProjectCodec.encode(doc, thumbnail = null)
        val file = projectFile(doc.id)
        ProjectCodec.writeAtomically(file, bytes)
        if (makeThumbnail) {
            writeThumbnail(doc)
        }
        // A completed save supersedes any pending recovery snapshot.
        recoveryFile(doc.id).delete()
        val summary = ProjectSummary(
            id = doc.id,
            name = doc.name,
            width = doc.width,
            height = doc.height,
            dpi = doc.dpi,
            layerCount = countLayers(doc),
            createdAt = index.byId(doc.id)?.createdAt ?: System.currentTimeMillis(),
            modifiedAt = System.currentTimeMillis(),
            thumbnailPath = thumbFile(doc.id).absolutePath.takeIf { it.isNotEmpty() },
            filePath = file.absolutePath,
            sizeBytes = file.length(),
            favorite = index.byId(doc.id)?.favorite ?: false,
        )
        index.upsert(summary.toRecord())
        summary
    }

    /** Cheap periodic autosave: writes only the recovery snapshot. */
    suspend fun autosaveRecovery(doc: ArtDocument): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val bytes = ProjectCodec.encode(doc, thumbnail = null)
            ProjectCodec.writeAtomically(recoveryFile(doc.id), bytes)
            true
        }.getOrDefault(false)
    }

    /** Loads the project, falling back to the recovery snapshot when the main file is damaged. */
    suspend fun load(id: Long): LoadResult = withContext(Dispatchers.IO) {
        val main = projectFile(id)
        if (main.exists()) {
            try {
                return@withContext LoadResult.Success(ProjectCodec.load(main), fromRecovery = false)
            } catch (t: Throwable) {
                val rec = recoveryFile(id)
                if (rec.exists()) {
                    try {
                        return@withContext LoadResult.Success(ProjectCodec.load(rec), fromRecovery = true)
                    } catch (_: Throwable) {
                        // fall through to Failure
                    }
                }
                return@withContext LoadResult.Failure("Project file is damaged and no recovery snapshot is available.", t)
            }
        }
        val rec = recoveryFile(id)
        if (rec.exists()) {
            return@withContext try {
                LoadResult.Success(ProjectCodec.load(rec), fromRecovery = true)
            } catch (t: Throwable) {
                LoadResult.Failure("Recovery snapshot is damaged.", t)
            }
        }
        LoadResult.Failure("Project file is missing.", null)
    }

    /** Imports a `.mona` file from an external location. */
    suspend fun importProject(source: File, displayName: String? = null): ArtDocument = withContext(Dispatchers.IO) {
        val doc = ProjectCodec.load(source)
        val id = System.currentTimeMillis() * 1000 + (0..999).random()
        doc.rename(displayName ?: doc.name, id)
        save(doc, makeThumbnail = true)
        doc
    }

    /** Imports a plain image as a single-layer project. */
    suspend fun importImage(bitmap: Bitmap, name: String): ArtDocument = withContext(Dispatchers.IO) {
        val id = System.currentTimeMillis() * 1000 + (0..999).random()
        val doc = ArtDocument(id, name, bitmap.width, bitmap.height, 72)
        val surface = com.monaart.engine.document.TileSurface(bitmap.width, bitmap.height)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        for (y in 0 until bitmap.height) for (x in 0 until bitmap.width) {
            val straight = pixels[y * bitmap.width + x]
            if ((straight ushr 24) != 0) surface.setPixel(x, y, com.monaart.engine.paint.straightToPremul(straight))
        }
        surface.touch()
        val layer = com.monaart.engine.document.Layer(doc.newLayerId(), "Imported image", surface)
        doc.layers.add(layer)
        doc.activeLayerId = layer.id
        doc.transparentBackground = true
        doc.bumpStructure()
        save(doc, makeThumbnail = true)
        doc
    }

    suspend fun deleteForever(id: Long) = withContext(Dispatchers.IO) {
        projectDir(id).deleteRecursively()
        index.remove(id)
    }

    suspend fun writeThumbnail(doc: ArtDocument) = withContext(Dispatchers.IO) {
        runCatching {
            val longest = thumbnailQuality().coerceIn(128, 2048)
            val preview = com.monaart.engine.document.Compositor().renderPreview(doc, longest, includeBackground = true)
            val file = thumbFile(doc.id)
            file.writeBytes(com.monaart.engine.io.PngCodec.encode(preview, 6))
        }
    }

    /** Longest edge of generated gallery thumbnails; supplied by the settings store. */
    var thumbnailQuality: () -> Int = { 512 }

    fun thumbnailFile(id: Long): File = thumbFile(id)

    private fun countLayers(doc: ArtDocument): Int {
        fun count(list: List<com.monaart.engine.document.Layer>): Int =
            list.sumOf { 1 + count(it.children) }
        return count(doc.layers)
    }

    companion object {
        fun documentThumbnail(doc: ArtDocument): Bitmap {
            val pixmap = com.monaart.engine.document.Compositor().renderPreview(doc, 512, includeBackground = true)
            val bmp = Bitmap.createBitmap(pixmap.width, pixmap.height, Bitmap.Config.ARGB_8888)
            bmp.setPixels(pixmap.straightPixels(), 0, pixmap.width, 0, 0, pixmap.width, pixmap.height)
            return bmp
        }
    }
}

/** Outcome of opening a project file. */
sealed class LoadResult {
    data class Success(val document: ArtDocument, val fromRecovery: Boolean) : LoadResult()
    data class Failure(val message: String, val cause: Throwable?) : LoadResult()
}

private fun ArtDocument.rename(newName: String, newId: Long) {
    this.id = newId
    this.name = newName
}

private fun ProjectSummary.toRecord() = ProjectRecord(
    id = id, name = name, width = width, height = height, dpi = dpi,
    layerCount = layerCount, createdAt = createdAt, modifiedAt = modifiedAt,
    lastOpenedAt = lastOpenedAt, thumbnailPath = thumbnailPath, filePath = filePath,
    sizeBytes = sizeBytes, favorite = favorite, folder = folder,
    recoveryState = recoveryState.name, deletedAt = 0,
)

private fun ProjectRecord.toSummary() = ProjectSummary(
    id = id, name = name, width = width, height = height, dpi = dpi, layerCount = layerCount,
    createdAt = createdAt, modifiedAt = modifiedAt, thumbnailPath = thumbnailPath,
    filePath = filePath, sizeBytes = sizeBytes, favorite = favorite, folder = folder,
    lastOpenedAt = lastOpenedAt,
    recoveryState = runCatching { RecoveryState.valueOf(recoveryState) }.getOrDefault(RecoveryState.Ok),
)
