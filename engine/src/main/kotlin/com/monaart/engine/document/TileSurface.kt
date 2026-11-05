package com.monaart.engine.document

import com.monaart.engine.paint.PixelOps
import com.monaart.engine.paint.RectI

/**
 * Sparse 256x256 tile storage. Layers use it so a 4096x4096 document costs
 * nothing until pixels are actually painted, and undo snapshots only copy the
 * tiles a stroke touched.
 */
class TileSurface(val width: Int, val height: Int, val tileSize: Int = TILE_SIZE) {

    private val tiles = HashMap<Long, IntArray>()
    private var cachedRevision = 0L

    val tilesX: Int get() = (width + tileSize - 1) / tileSize
    val tilesY: Int get() = (height + tileSize - 1) / tileSize

    /** Bumped on every mutation; used by the renderer to invalidate cached GPU textures. */
    val revision: Long get() = cachedRevision

    fun touch() { cachedRevision++ }

    fun hasTile(tx: Int, ty: Int): Boolean = tiles.containsKey(key(tx, ty))

    /** Live tile array or null. Callers must not mutate without calling [touch]. */
    fun peekTile(tx: Int, ty: Int): IntArray? = tiles[key(tx, ty)]

    /** Live tile array, allocating a transparent one if absent. */
    fun obtainTile(tx: Int, ty: Int): IntArray {
        val k = key(tx, ty)
        return tiles.getOrPut(k) { IntArray(tileSize * tileSize) }
    }

    fun removeTile(tx: Int, ty: Int) { tiles.remove(key(tx, ty)) }

    fun tileKeys(): Set<Long> = tiles.keys

    fun tileKey(tx: Int, ty: Int): Long = key(tx, ty)

    fun tileCoords(k: Long): IntArray = intArrayOf((k ushr 32).toInt(), (k and 0xFFFFFFFFL).toInt())

    fun clear() {
        tiles.clear()
        touch()
    }

    fun getPixel(x: Int, y: Int): Int {
        if (x < 0 || y < 0 || x >= width || y >= height) return 0
        val tx = x / tileSize
        val ty = y / tileSize
        val t = tiles[key(tx, ty)] ?: return 0
        return t[(y % tileSize) * tileSize + (x % tileSize)]
    }

    /**
     * Copies [r] out of the surface into [out] (row-major, `r.width` per row) and
     * writes [out] back with [takePixels]. Per-pixel [getPixel]/[setPixel] costs a
     * hash lookup each, which dominated the cost of flushing a stroke; these
     * helpers resolve one tile per row segment instead.
     */
    fun copyRect(r: RectI, out: IntArray) {
        val rr = r.intersect(RectI.wh(width, height))
        if (rr.isEmpty) return
        for (y in rr.top until rr.bottom) {
            val ty = y / tileSize
            val rowBase = (y % tileSize) * tileSize
            var x = rr.left
            val rowOut = (y - rr.top) * rr.width - rr.left
            while (x < rr.right) {
                val tx = x / tileSize
                val segEnd = minOf(rr.right, (tx + 1) * tileSize)
                val t = tiles[key(tx, ty)]
                if (t == null) {
                    java.util.Arrays.fill(out, rowOut + x, rowOut + segEnd, 0)
                } else {
                    System.arraycopy(t, rowBase + (x - tx * tileSize), out, rowOut + x, segEnd - x)
                }
                x = segEnd
            }
        }
    }

    /** Writes a rect previously read with [copyRect] back into the surface. */
    fun writeRect(r: RectI, src: IntArray) {
        val rr = r.intersect(RectI.wh(width, height))
        if (rr.isEmpty) return
        for (y in rr.top until rr.bottom) {
            val ty = y / tileSize
            val rowBase = (y % tileSize) * tileSize
            var x = rr.left
            val rowIn = (y - rr.top) * rr.width - rr.left
            while (x < rr.right) {
                val tx = x / tileSize
                val segEnd = minOf(rr.right, (tx + 1) * tileSize)
                val t = obtainTile(tx, ty)
                System.arraycopy(src, rowIn + x, t, rowBase + (x - tx * tileSize), segEnd - x)
                x = segEnd
            }
        }
    }

    fun setPixel(x: Int, y: Int, color: Int) {
        if (x < 0 || y < 0 || x >= width || y >= height) return
        val tx = x / tileSize
        val ty = y / tileSize
        obtainTile(tx, ty)[(y % tileSize) * tileSize + (x % tileSize)] = color
    }

    fun fill(color: Int) {
        if (color == 0) { clear(); return }
        for (ty in 0 until tilesY) for (tx in 0 until tilesX) {
            val t = obtainTile(tx, ty)
            java.util.Arrays.fill(t, color)
        }
        touch()
    }

    fun ofColor(color: Int): TileSurface {
        fill(color)
        return this
    }

    /** Composites a whole [src] pixmap over this surface at the origin (Normal). */
    fun drawPixmap(src: com.monaart.engine.paint.Pixmap, dx: Int = 0, dy: Int = 0) {
        forEachPixelIn(RectI(dx, dy, dx + src.width, dy + src.height)) { x, y ->
            val s = src.get(x - dx, y - dy)
            if (s != 0) setPixel(x, y, PixelOps.over(getPixel(x, y), s))
        }
        touch()
    }

    inline fun forEachPixelIn(r: RectI, fn: (Int, Int) -> Unit) {
        val rr = r.intersect(RectI.wh(width, height))
        if (rr.isEmpty) return
        for (y in rr.top until rr.bottom) for (x in rr.left until rr.right) fn(x, y)
    }

    /** Tile indices covered by [r], clipped to the surface. */
    fun tilesFor(r: RectI): List<Long> {
        val rr = r.intersect(RectI.wh(width, height))
        if (rr.isEmpty) return emptyList()
        val out = ArrayList<Long>()
        val tx0 = rr.left / tileSize; val tx1 = (rr.right - 1) / tileSize
        val ty0 = rr.top / tileSize; val ty1 = (rr.bottom - 1) / tileSize
        for (ty in ty0..ty1) for (tx in tx0..tx1) out.add(key(tx, ty))
        return out
    }

    /** Direct tile access for the edit/undo layer (null when the tile is absent). */
    fun tileByKey(k: Long): IntArray? = tiles[k]

    fun putTileByKey(k: Long, data: IntArray) {
        tiles[k] = data.copyOf()
        touch()
    }

    fun dropTileByKey(k: Long) {
        if (tiles.remove(k) != null) touch()
    }

    /** In-place tile data capture for undo. Only tiles that exist are stored. */
    fun snapshot(r: RectI): TileSnapshot {
        val data = HashMap<Long, IntArray>()
        for (k in tilesFor(r)) {
            tiles[k]?.let { data[k] = it.copyOf() }
        }
        return TileSnapshot(r.intersect(RectI.wh(width, height)), data)
    }

    /** Applies a previously captured snapshot (restores both existing and missing tiles). */
    fun restore(snap: TileSnapshot) {
        val needed = tilesFor(snap.rect).toHashSet()
        for (k in needed) {
            val before = snap.tiles[k]
            if (before == null) tiles.remove(k) else tiles[k] = before.copyOf()
        }
        touch()
    }

    /** True when every pixel in the surface is fully transparent (and no tiles exist). */
    fun isBlank(): Boolean {
        if (tiles.isEmpty()) return true
        for (t in tiles.values) {
            for (p in t) if (p != 0) return false
        }
        return true
    }

    fun usedTileCount(): Int = tiles.size

    /**
     * Tight bounds of the non-transparent pixels, or [RectI.EMPTY] when blank.
     * Scans tiles, not the canvas, so it stays cheap on a mostly empty layer.
     */
    fun contentBounds(): RectI {
        if (tiles.isEmpty()) return RectI.EMPTY
        var l = Int.MAX_VALUE; var t = Int.MAX_VALUE
        var r = Int.MIN_VALUE; var b = Int.MIN_VALUE
        for ((k, px) in tiles) {
            var localL = -1; var localT = -1; var localR = -1; var localB = -1
            for (y in 0 until tileSize) {
                val row = y * tileSize
                var left = -1
                var right = -1
                for (x in 0 until tileSize) {
                    if (px[row + x] != 0) { if (left < 0) left = x; right = x }
                }
                if (left < 0) continue
                if (localT < 0) localT = y
                localB = y
                if (localL < 0 || left < localL) localL = left
                if (right > localR) localR = right
            }
            if (localL < 0) continue
            val tx = (k shr 32).toInt()
            val ty = k.toInt()
            val x0 = tx * tileSize + localL
            val y0 = ty * tileSize + localT
            val x1 = tx * tileSize + localR + 1
            val y1 = ty * tileSize + localB + 1
            if (x0 < l) l = x0
            if (y0 < t) t = y0
            if (x1 > r) r = x1
            if (y1 > b) b = y1
        }
        if (l > r || t > b) return RectI.EMPTY
        return RectI(l, t, r, b)
    }

    private fun key(tx: Int, ty: Int): Long = (tx.toLong() shl 32) or (ty.toLong() and 0xFFFFFFFFL)

    companion object {
        const val TILE_SIZE = 256
    }
}

/** Pixel data of the tiles intersecting a rectangle, at a specific point in time. */
class TileSnapshot(val rect: RectI, val tiles: Map<Long, IntArray>) {
    val tileCount: Int get() = tiles.size
    fun byteSize(): Long = tiles.size.toLong() * TileSurface.TILE_SIZE * TileSurface.TILE_SIZE * 4L
}
