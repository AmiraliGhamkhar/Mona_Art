package com.monaart.engine.document

import com.monaart.engine.history.History
import com.monaart.engine.paint.RectI

/**
 * Records an undoable pixel edit on one layer.
 *
 * Guarantees that matter for painting:
 *  - The *before* state is captured exactly once per tile, the first time the
 *    edit touches it. Re-touching an already-captured tile never overwrites the
 *    snapshot, so an edit that writes the same pixels several times (a stroke
 *    flushed repeatedly) still undoes to the pre-stroke state.
 *  - Tiles that did not exist before the edit are remembered as "absent", so undo
 *    removes them again instead of leaving transparent-but-allocated tiles.
 *  - [pristineFor] exposes the pre-edit pixels, which the brush engine needs to
 *    composite a stroke's cumulative coverage exactly once.
 */
class PixelEdit(
    private val doc: ArtDocument,
    private val label: String,
    private val layer: Layer? = doc.activeLayer(),
) {
    private val surface: TileSurface? = layer?.surface
    private var before: MutableMap<Long, IntArray?>? = null
    private var covered: RectI = RectI.EMPTY

    val bounds: RectI get() = covered

    private fun key(tx: Int, ty: Int): Long = (tx.toLong() shl 32) or (ty.toLong() and 0xFFFFFFFFL)

    /** Expands the edit rectangle; captures tiles the first time they are touched. */
    fun touchRect(r: RectI) {
        val s = surface ?: return
        val rr = r.intersect(RectI.wh(s.width, s.height))
        if (rr.isEmpty) return
        val tiles = before ?: HashMap<Long, IntArray?>(8).also { before = it }
        val size = TileSurface.TILE_SIZE
        val tx0 = rr.left / size
        val tx1 = (rr.right - 1) / size
        val ty0 = rr.top / size
        val ty1 = (rr.bottom - 1) / size
        for (ty in ty0..ty1) {
            for (tx in tx0..tx1) {
                val k = key(tx, ty)
                if (tiles.containsKey(k)) continue
                tiles[k] = s.peekTile(tx, ty)?.copyOf()
            }
        }
        covered = covered.union(rr)
        doc.bumpRender()
    }

    /**
     * The layer content as it was before this edit, for the tiles the edit covers.
     * Tiles that did not exist read as fully transparent.
     */
    fun pristineFor(r: RectI): TileSurface? {
        val snap = before ?: return null
        val s = surface ?: return null
        val rr = r.intersect(RectI.wh(s.width, s.height))
        if (rr.isEmpty) return null
        val out = TileSurface(s.width, s.height)
        val size = TileSurface.TILE_SIZE
        val tx0 = rr.left / size
        val tx1 = (rr.right - 1) / size
        val ty0 = rr.top / size
        val ty1 = (rr.bottom - 1) / size
        for (ty in ty0..ty1) {
            for (tx in tx0..tx1) {
                val data = snap[key(tx, ty)] ?: continue
                val dest = out.obtainTile(tx, ty)
                System.arraycopy(data, 0, dest, 0, dest.size)
            }
        }
        return out
    }

    /** Commits the edit to history. Returns false (and writes nothing) for empty edits. */
    fun commit(): Boolean {
        val s = surface ?: return false
        val beforeMap = before ?: return false
        if (covered.isEmpty || beforeMap.isEmpty()) return false
        val afterTiles = HashMap<Long, IntArray?>(beforeMap.size)
        for (k in beforeMap.keys) {
            afterTiles[k] = s.tileByKey(k)?.copyOf()
        }
        doc.history.push(History.TileDiff(label, covered, beforeMap.toMap(), afterTiles, s))
        before = null
        covered = RectI.EMPTY
        doc.bumpRender()
        return true
    }

    /** Rolls the surface back and discards the edit without touching history. */
    fun rollback() {
        val s = surface ?: return
        val beforeMap = before ?: return
        for ((k, data) in beforeMap) {
            if (data == null) s.dropTileByKey(k) else s.putTileByKey(k, data)
        }
        before = null
        covered = RectI.EMPTY
        doc.bumpRender()
    }

    companion object {
        /** Convenience for one-shot edits (fill, filter, paste). */
        inline fun run(doc: ArtDocument, label: String, region: RectI, block: () -> Unit): Boolean {
            val edit = PixelEdit(doc, label)
            val r = region.intersect(doc.canvasRect)
            edit.touchRect(r)
            block()
            return edit.commit()
        }
    }
}
