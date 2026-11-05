package com.monaart.engine.io

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Layer
import com.monaart.engine.document.LayerKind
import com.monaart.engine.document.TileSurface
import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.Pixmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Native editable project format (`.mona`).
 *
 * A ZIP container holding:
 *  - `manifest.json`  versioned document + layer metadata (human inspectable)
 *  - `layers/<id>.tiles` DEFLATE-compressed premultiplied tile data
 *  - `thumb.png`      gallery thumbnail
 *  - `checksum.txt`   SHA-256 of the manifest and tile streams for corruption detection
 *
 * Saving is atomic: the caller writes to a temp file and renames, so a crash
 * mid-write can never destroy the previous good project. Saving runs off the UI
 * thread and never mutates the live document.
 */
object ProjectCodec {
    const val FORMAT_VERSION = 3
    const val EXTENSION = "mona"
    private const val MAGIC = "MONA-ART"

    /** Document metadata, stored as `manifest.json` inside the archive. */
    data class Manifest(
        val magic: String = MAGIC,
        val formatVersion: Int = FORMAT_VERSION,
        val appVersion: String = "1.0",
        val id: Long,
        val name: String,
        val width: Int,
        val height: Int,
        val dpi: Int,
        val backgroundColor: Int,
        val transparentBackground: Boolean,
        val activeLayerId: Long,
        val createdAt: Long = 0,
        val modifiedAt: Long = 0,
        val layers: List<LayerInfo>,
        val lastBrushId: String? = null,
        val palette: List<Int> = emptyList(),
    )

    data class LayerInfo(
        val id: Long,
        val name: String,
        val kind: String,
        val visible: Boolean,
        val locked: Boolean,
        val alphaLocked: Boolean,
        val clipping: Boolean,
        val opacity: Float,
        val blend: String,
        val children: List<LayerInfo> = emptyList(),
    )

    fun manifestToJson(m: Manifest): Json = Json.obj(
        "magic" to Json.of(m.magic),
        "formatVersion" to Json.of(m.formatVersion),
        "appVersion" to Json.of(m.appVersion),
        "id" to Json.of(m.id),
        "name" to Json.of(m.name),
        "width" to Json.of(m.width),
        "height" to Json.of(m.height),
        "dpi" to Json.of(m.dpi),
        "backgroundColor" to Json.of(m.backgroundColor),
        "transparentBackground" to Json.of(m.transparentBackground),
        "activeLayerId" to Json.of(m.activeLayerId),
        "createdAt" to Json.of(m.createdAt),
        "modifiedAt" to Json.of(m.modifiedAt),
        "lastBrushId" to Json.of(m.lastBrushId),
        "palette" to Json.arr(m.palette.map { Json.of(it) }),
        "layers" to Json.arr(m.layers.map { layerInfoToJson(it) }),
    )

    private fun layerInfoToJson(l: LayerInfo): Json = Json.obj(
        "id" to Json.of(l.id),
        "name" to Json.of(l.name),
        "kind" to Json.of(l.kind),
        "visible" to Json.of(l.visible),
        "locked" to Json.of(l.locked),
        "alphaLocked" to Json.of(l.alphaLocked),
        "clipping" to Json.of(l.clipping),
        "opacity" to Json.of(l.opacity),
        "blend" to Json.of(l.blend),
        "children" to Json.arr(l.children.map { layerInfoToJson(it) }),
    )

    fun manifestFromJson(j: Json): Manifest {
        fun layerFrom(jl: Json): LayerInfo = LayerInfo(
            id = jl["id"]?.asLong() ?: 0L,
            name = jl["name"]?.asString() ?: "Layer",
            kind = jl["kind"]?.asString() ?: "RASTER",
            visible = jl["visible"]?.asBool() ?: true,
            locked = jl["locked"]?.asBool() ?: false,
            alphaLocked = jl["alphaLocked"]?.asBool() ?: false,
            clipping = jl["clipping"]?.asBool() ?: false,
            opacity = jl["opacity"]?.asFloat() ?: 1f,
            blend = jl["blend"]?.asString() ?: "Normal",
            children = jl["children"]?.array()?.map { layerFrom(it) } ?: emptyList(),
        )
        return Manifest(
            magic = j["magic"]?.asString() ?: MAGIC,
            formatVersion = j["formatVersion"]?.asInt() ?: 1,
            appVersion = j["appVersion"]?.asString() ?: "1.0",
            id = j["id"]?.asLong() ?: 0L,
            name = j["name"]?.asString() ?: "Untitled",
            width = j["width"]?.asInt() ?: 0,
            height = j["height"]?.asInt() ?: 0,
            dpi = j["dpi"]?.asInt() ?: 72,
            backgroundColor = j["backgroundColor"]?.asInt() ?: 0xFFFFFFFF.toInt(),
            transparentBackground = j["transparentBackground"]?.asBool() ?: false,
            activeLayerId = j["activeLayerId"]?.asLong() ?: -1L,
            createdAt = j["createdAt"]?.asLong() ?: 0L,
            modifiedAt = j["modifiedAt"]?.asLong() ?: 0L,
            layers = j["layers"]?.array()?.map { layerFrom(it) } ?: emptyList(),
            lastBrushId = j["lastBrushId"]?.asString(),
            palette = j["palette"]?.array()?.mapNotNull { it.asInt() } ?: emptyList(),
        )
    }

    class CorruptProjectException(message: String, cause: Throwable? = null) : Exception(message, cause)

    /** Serializes a document to bytes without touching the live document. */
    fun encode(doc: ArtDocument, thumbnail: Pixmap? = null, appVersion: String = "1.0"): ByteArray {
        val manifest = Manifest(
            id = doc.id,
            name = doc.name,
            width = doc.width,
            height = doc.height,
            dpi = doc.dpi,
            backgroundColor = doc.backgroundColor,
            transparentBackground = doc.transparentBackground,
            activeLayerId = doc.activeLayerId,
            modifiedAt = System.currentTimeMillis(),
            layers = doc.layers.map { toInfo(it) },
            palette = emptyList(),
            appVersion = appVersion,
        )
        val manifestText = manifestToJson(manifest).toJsonString()
        val tileStreams = LinkedHashMap<Long, ByteArray>()
        fun collect(list: List<Layer>) {
            for (l in list) {
                l.surface?.let { tileStreams[l.id] = encodeSurface(it) }
                collect(l.children)
            }
        }
        collect(doc.layers)

        val out = ByteArrayOutputStream(manifestText.length + tileStreams.values.sumOf { it.size } + 4096)
        ZipOutputStream(out).use { zip ->
            // Tiles are already deflated; store classes so damage is surfaced by our
            // own checksum rather than silently absorbed/retried inside the zip layer.
            zip.setLevel(Deflater.NO_COMPRESSION)
            zip.putNextEntry(ZipEntry("manifest.json"))
            zip.write(manifestText.toByteArray())
            zip.closeEntry()
            for ((id, data) in tileStreams) {
                zip.putNextEntry(ZipEntry("layers/$id.tiles"))
                zip.write(data)
                zip.closeEntry()
            }
            if (thumbnail != null) {
                zip.putNextEntry(ZipEntry("thumb.png"))
                zip.write(PngCodec.encode(thumbnail, 4))
                zip.closeEntry()
            }
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(manifestText.toByteArray())
            for ((id, data) in tileStreams) {
                digest.update(id.toString().toByteArray())
                digest.update(data)
            }
            thumbnail?.let { digest.update(PngCodec.encode(it, 4)) }
            zip.putNextEntry(ZipEntry("checksum.txt"))
            zip.write(hex(digest.digest()).toByteArray())
            zip.closeEntry()
        }
        return out.toByteArray()
    }

    fun save(doc: ArtDocument, file: File, thumbnail: Pixmap? = null) {
        val bytes = encode(doc, thumbnail)
        writeAtomically(file, bytes)
    }

    fun writeAtomically(file: File, bytes: ByteArray) {
        val parent = file.parentFile ?: throw IllegalArgumentException("No parent dir for $file")
        if (!parent.exists()) parent.mkdirs()
        val tmp = File(parent, "${file.name}.tmp")
        tmp.writeBytes(bytes)
        // Best-effort fsync of the payload before the rename makes it visible.
        java.io.FileOutputStream(tmp, true).use {
            it.flush()
            it.fd.sync()
        }
        val backup = File(parent, "${file.name}.bak")
        if (file.exists()) {
            if (backup.exists()) backup.delete()
            file.renameTo(backup)
        }
        if (!tmp.renameTo(file)) {
            // rename can fail across filesystems; fall back to copy then delete.
            file.writeBytes(bytes)
            tmp.delete()
        }
    }

    fun decode(bytes: ByteArray): ArtDocument {
        val entries = HashMap<String, ByteArray>()
        try {
            ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
                var e: ZipEntry? = zip.nextEntry
                while (e != null) {
                    entries[e.name] = zip.readBytes()
                    zip.closeEntry()
                    e = zip.nextEntry
                }
            }
        } catch (t: Exception) {
            // Deliberately Exception, not Throwable: an OutOfMemoryError while
            // inflating a hostile archive is a memory failure, not a corrupt
            // file, and reporting it as corruption lets the caller carry on.
            throw CorruptProjectException("Project archive is unreadable", t)
        }
        val manifestBytes = entries["manifest.json"] ?: throw CorruptProjectException("Missing manifest.json")
        val manifest = try {
            manifestFromJson(Json.parse(String(manifestBytes, Charsets.UTF_8)))
        } catch (t: Exception) {
            throw CorruptProjectException("Manifest is not valid JSON", t)
        }
        if (manifest.magic != MAGIC) throw CorruptProjectException("Not a Mona Art project")
        if (manifest.formatVersion > FORMAT_VERSION) {
            throw CorruptProjectException(
                "Project was written by a newer version (format ${manifest.formatVersion}, supported $FORMAT_VERSION)",
            )
        }
        // Integrity check
        entries["checksum.txt"]?.let { expected ->
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(manifestBytes)
            // Same insertion order as the writer: depth-first layer order.
            val ordered = orderedTileEntries(manifest)
            for ((id, key) in ordered) {
                entries[key]?.let {
                    digest.update(id.toString().toByteArray())
                    digest.update(it)
                }
            }
            entries["thumb.png"]?.let { digest.update(it) }
            val actual = hex(digest.digest())
            if (String(expected).trim() != actual) {
                throw CorruptProjectException("Project checksum mismatch (file is damaged)")
            }
        }
        return fromManifest(manifest, entries)
    }

    fun load(file: File): ArtDocument = decode(file.readBytes())
    fun load(input: InputStream): ArtDocument = decode(input.readBytes())

    /** Depth-first (id, entryName) pairs in the same order the writer produced them. */
    private fun orderedTileEntries(m: Manifest): List<Pair<Long, String>> {
        val out = ArrayList<Pair<Long, String>>()
        fun walk(list: List<LayerInfo>) {
            for (l in list) {
                if (l.kind != "GROUP") out.add(l.id to "layers/${l.id}.tiles")
                walk(l.children)
            }
        }
        walk(m.layers)
        return out
    }

    private fun fromManifest(m: Manifest, entries: Map<String, ByteArray>): ArtDocument {
        if (m.width !in 1..16384 || m.height !in 1..16384) {
            throw CorruptProjectException("Invalid canvas size ${m.width}x${m.height}")
        }
        val doc = ArtDocument(m.id, m.name, m.width, m.height, m.dpi)
        doc.backgroundColor = m.backgroundColor
        doc.transparentBackground = m.transparentBackground
        fun build(list: List<LayerInfo>): MutableList<Layer> = list.map { info ->
            val kind = if (info.kind == "GROUP") LayerKind.GROUP else LayerKind.RASTER
            val surface = if (kind == LayerKind.RASTER) {
                val s = TileSurface(m.width, m.height)
                entries["layers/${info.id}.tiles"]?.let { decodeSurfaceInto(it, s) }
                s
            } else {
                null
            }
            val layer = Layer(info.id, info.name, surface, kind)
            layer.visible = info.visible
            layer.locked = info.locked
            layer.alphaLocked = info.alphaLocked
            layer.clipping = info.clipping
            layer.opacity = info.opacity
            layer.blend = runCatching { BlendMode.valueOf(info.blend) }.getOrDefault(BlendMode.Normal)
            layer.children = build(info.children)
            layer
        }.toMutableList()
        doc.layers.clear()
        doc.layers.addAll(build(m.layers))
        if (doc.layers.isEmpty()) doc.layers.add(Layer(1, "Layer 1", TileSurface(m.width, m.height)))
        doc.activeLayerId = if (doc.findLayer(m.activeLayerId) != null) m.activeLayerId else doc.layers.first().id
        doc.bumpStructure()
        return doc
    }

    private fun toInfo(l: Layer): LayerInfo = LayerInfo(
        id = l.id,
        name = l.name,
        kind = l.kind.name,
        visible = l.visible,
        locked = l.locked,
        alphaLocked = l.alphaLocked,
        clipping = l.clipping,
        opacity = l.opacity,
        blend = l.blend.name,
        children = l.children.map { toInfo(it) },
    )

    /**
     * Tile payload: [tileSize int][tileCount int] then per tile
     * [key long][deflated ints]. Empty surfaces produce a tiny payload.
     */
    private fun encodeSurface(surface: TileSurface): ByteArray {
        val out = ByteArrayOutputStream()
        val keys = surface.tileKeys().toList()
        out.write(intBytes(surface.tileSize))
        out.write(intBytes(keys.size))
        for (k in keys) {
            val coords = surface.tileCoords(k)
            val tile = surface.peekTile(coords[0], coords[1]) ?: continue
            out.write(longBytes(k))
            val raw = ByteArray(tile.size * 4)
            for (i in tile.indices) {
                val v = tile[i]
                raw[i * 4] = (v ushr 24).toByte()
                raw[i * 4 + 1] = (v ushr 16).toByte()
                raw[i * 4 + 2] = (v ushr 8).toByte()
                raw[i * 4 + 3] = v.toByte()
            }
            val comp = ByteArrayOutputStream(raw.size / 2 + 64)
            DeflaterOutputStream(comp, Deflater(Deflater.BEST_SPEED)).use { it.write(raw) }
            val cb = comp.toByteArray()
            out.write(intBytes(cb.size))
            out.write(cb)
        }
        return out.toByteArray()
    }

    private fun decodeSurfaceInto(data: ByteArray, surface: TileSurface) {
        var pos = 0
        require(data.size >= 8) { "Tile stream truncated" }
        fun readInt(): Int {
            val v = ((data[pos].toInt() and 0xFF) shl 24) or ((data[pos + 1].toInt() and 0xFF) shl 16) or
                ((data[pos + 2].toInt() and 0xFF) shl 8) or (data[pos + 3].toInt() and 0xFF)
            pos += 4
            return v
        }
        fun readLong(): Long {
            var v = 0L
            for (i in 0 until 8) v = (v shl 8) or (data[pos + i].toLong() and 0xFF)
            pos += 8
            return v
        }
        readInt() // tile size (current format always uses TileSurface.TILE_SIZE)
        val count = readInt()
        for (i in 0 until count) {
            val key = readLong()
            val len = readInt()
            if (len < 0 || pos + len > data.size) throw CorruptProjectException("Tile stream is truncated")
            val comp = data.copyOfRange(pos, pos + len)
            pos += len
            val coords = surface.tileCoords(key)
            if (coords[0] < 0 || coords[1] < 0 || coords[0] >= surface.tilesX || coords[1] >= surface.tilesY) {
                throw CorruptProjectException("Tile out of canvas bounds (${coords[0]}, ${coords[1]})")
            }
            val tile = surface.obtainTile(coords[0], coords[1])
            val inflated = inflate(comp)
            if (inflated.size < tile.size * 4) throw CorruptProjectException("Tile data is truncated")
            for (j in tile.indices) {
                val a = inflated[j * 4].toInt() and 0xFF
                val r = inflated[j * 4 + 1].toInt() and 0xFF
                val g = inflated[j * 4 + 2].toInt() and 0xFF
                val b = inflated[j * 4 + 3].toInt() and 0xFF
                tile[j] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        surface.touch()
    }

    private fun inflate(data: ByteArray): ByteArray {
        val inf = Inflater()
        inf.setInput(data)
        val out = ByteArrayOutputStream(data.size * 4)
        val buf = ByteArray(64 * 1024)
        while (!inf.finished()) {
            val n = inf.inflate(buf)
            if (n == 0) break
            out.write(buf, 0, n)
        }
        inf.end()
        return out.toByteArray()
    }

    private fun intBytes(v: Int): ByteArray = byteArrayOf(
        (v ushr 24).toByte(), (v ushr 16).toByte(), (v ushr 8).toByte(), v.toByte(),
    )

    private fun longBytes(v: Long): ByteArray = ByteArray(8) { i -> (v ushr (56 - 8 * i)).toByte() }

    private fun hex(b: ByteArray): String = b.joinToString("") { "%02x".format(it) }

    /** Reads only the manifest (used by the gallery without loading pixels). */
    fun readManifest(file: File): Manifest {
        ZipInputStream(file.inputStream().buffered()).use { zip ->
            var e: ZipEntry? = zip.nextEntry
            while (e != null) {
                if (e.name == "manifest.json") {
                    val text = zip.readBytes().toString(Charsets.UTF_8)
                    return manifestFromJson(Json.parse(text))
                }
                e = zip.nextEntry
            }
        }
        throw CorruptProjectException("Missing manifest.json")
    }

    fun writeManifestJson(m: Manifest): String = manifestToJson(m).toJsonString(pretty = true)
}
