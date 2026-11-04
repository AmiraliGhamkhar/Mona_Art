package com.monaart.engine.document

import com.monaart.engine.history.History
import com.monaart.engine.paint.RectI

/**
 * Records an undoable pixel edit on one layer.
 *
 * The before-snapshot is taken lazily on the first write of a stroke (only the
 * tiles the stroke touches), and the after-snapshot on commit. This keeps undo
 * cost proportional to the painted area instead of the canvas size.
 */
class PixelEdit(
    private val doc: ArtDocument,
    private val label: String,
    private val layer: Layer? = doc.activeLayer(),
) {
    private val surface: TileSurface? = layer?.surface
    private var before: TileSnapshot? = null
    private var covered: RectI = RectI.EMPTY

    val bounds: RectI get() = covered

    /** Expands the edit rectangle; captures tiles the first time they are touched. */
    fun touchRect(r: RectI) {
        val s = surface ?: return
        val rr = r.intersect(RectI.wh(s.width, s.height))
        if (rr.isEmpty) return
        if (before == null) {
            before = s.snapshot(rr)
        } else if (!covered.isEmpty) {
            // Capture any tiles that were not part of the previously covered rect.
            val extra = tilesMinus(rr, covered)
            if (extra != null) {
                val snap = s.snapshot(extra)
                before = mergeSnapshots(before!!, snap)
            }
        }
        covered = covered.union(rr)
        s.touch()
        doc.bumpRender()
    }

    private fun tilesMinus(r: RectI, other: RectI): RectI? {
        if (other.isEmpty) return r
        if (other.intersect(r) == r) return null
        return r
    }

    private fun mergeSnapshots(a: TileSnapshot, b: TileSnapshot): TileSnapshot {
        val m = HashMap<Long, IntArray>(a.tiles.size + b.tiles.size)
        m.putAll(a.tiles)
        m.putAll(b.tiles)
        return TileSnapshot(a.rect.union(b.rect), m)
    }

    /** Commits the edit to history. Returns false (and writes nothing) for empty edits. */
    fun commit(): Boolean {
        val s = surface ?: return false
        val b = before ?: return false
        if (covered.isEmpty) return false
        val after = s.snapshot(covered)
        doc.history.push(History.PixelDiff(label, covered, b, after, s))
        before = null
        covered = RectI.EMPTY
        doc.bumpRender()
        return true
    }

    /** Rolls the surface back and discards the edit without touching history. */
    fun rollback() {
        val s = surface ?: return
        before?.let { s.restore(it) }
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
