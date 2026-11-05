package com.monaart.engine.brush

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Layer
import com.monaart.engine.document.PixelEdit
import com.monaart.engine.document.TileSurface
import com.monaart.engine.paint.PixelOps
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf

/**
 * The drawing engine entry point. Owns an in-progress stroke, accumulates dab
 * coverage in a stroke buffer so stroke opacity is uniform, and commits one
 * undoable edit per stroke.
 *
 * Usage:
 * ```
 * engine.begin(doc, layer, spec, color)
 * engine.move(point)   // any number of times
 * engine.end()
 * ```
 * The host must call [flush] after `move` (it is cheap and idempotent) so pixels
 * reach the layer for display.
 */
class BrushEngine {

    /**
     * One symmetry axis. A [vertical] axis mirrors across a vertical line at
     * document coordinate [pivot]; a horizontal one across a horizontal line.
     */
    data class Axis(val vertical: Boolean, val pivot: Float)

    private var spec: BrushSpec? = null
    private var doc: ArtDocument? = null
    private var layer: Layer? = null
    private var processor: StrokeProcessor? = null
    private var rasterizer: StrokeRasterizer? = null
    private var edit: PixelEdit? = null
    private var color: Int = com.monaart.engine.paint.ColorInts.BLACK
    private var seedCounter = 0
    private var started = false
    private var pickup: Pickup? = null
    /** Active symmetry axes; empty for a plain stroke. */
    private var axes: List<Axis> = emptyList()
    /** The half of the canvas the primary stroke may paint (null = everywhere). */
    private var primaryClip: StrokeRasterizer.DabClip? = null

    /** Region of the document touched by the current stroke. */
    private var strokeBounds: RectI = RectI.EMPTY
    /** Cached pre-stroke pixels for the current stroke window. */
    private var baseline: Baseline? = null

    /**
     * Reused per-tile pixel buffer. A flush walks one tile at a time, so allocating
     * a fresh array per tile per flush was pure garbage: a dozen allocations per
     * pointer sample, which showed up as multi-millisecond pauses when the
     * collector ran mid-stroke. Kept for the lifetime of the stroke.
     */
    private var flushPayload: IntArray? = null

    private companion object {
        /**
         * ~1.6 MB of scratch buffer. Past this, flushed coverage is dropped and the
         * buffer is rebuilt around the live tail. The threshold is deliberately low:
         * the cost that matters is the reallocation, which is proportional to the
         * whole buffer, so a smaller window trades a cheap row copy for a cheap
         * array instead of a 16 MB allocation in the middle of a stroke.
         */
        const val RECYCLE_AREA = 400_000

        /** ~24 MB cap on the cached pre-stroke window. */
        const val BASELINE_MAX_AREA = 6_000_000L
    }

    private var lastDabX = 0f
    private var lastDabY = 0f
    /** Pixels whose stroke coverage was flushed and then dropped from the buffer. */
    private var finalized: RectI = RectI.EMPTY
    private var flushedBounds: RectI = RectI.EMPTY
    /** Pixels affected by dabs drawn since the last flush; the flush recomputes exactly these. */
    private var pendingBounds: RectI = RectI.EMPTY

    val isActive: Boolean get() = started

    /** Bounds that changed since the last call (for minimal invalidation). */
    fun takeDirty(): RectI {
        val b = flushedBounds
        flushedBounds = RectI.EMPTY
        return b
    }

        fun begin(doc: ArtDocument, layer: Layer, spec: BrushSpec, color: Int, seed: Int = (System.nanoTime(
        ) and 0xFFFF).toInt(), axes: List<Axis> = emptyList()) {
        require(!started) { "A stroke is already in progress" }
        require(!layer.locked) { "Layer is locked" }
        require(!layer.isGroup) { "Cannot paint on a group layer" }
        require(axes.all { it.pivot in 0f..Float.MAX_VALUE }) { "Axis pivot must be a finite document coordinate" }
        this.doc = doc
        this.layer = layer
        this.spec = spec
        this.color = color
        this.axes = axes
        primaryClip = primaryClipOf(axes)
        seedCounter = seed
        processor = StrokeProcessor(spec)
        val pad = (spec.size * 0.75f).toInt() + 4
        rasterizer = StrokeRasterizer(spec, pad)
        edit = PixelEdit(doc, strokeLabel(spec), layer)
        started = true
        strokeBounds = RectI.EMPTY
        baseline = null
        finalized = RectI.EMPTY
        flushedBounds = RectI.EMPTY
        pendingBounds = RectI.EMPTY
        pickup = if (spec.kind == BrushKind.SMUDGE || spec.wetness > 0f) {
            Pickup(layer.requireSurface(), spec)
        } else null
    }

    private fun strokeLabel(spec: BrushSpec): String = when (spec.kind) {
        BrushKind.ERASER -> "Erase"
        BrushKind.SMUDGE -> "Smudge"
        BrushKind.AIRBRUSH -> "Airbrush stroke"
        else -> "Brush stroke"
    }

    fun move(p: StrokePoint) {
        val proc = processor ?: return
        val dabs = proc.accept(p)
        drawDabs(dabs)
    }

    fun end(): Boolean {
        if (!started) return false
        val proc = processor
        val dabs = proc?.finish() ?: emptyList()
        drawDabs(dabs)
        flush()
        val committed = edit?.commit() ?: false
        baseline = null
        started = false
        processor = null
        rasterizer = null
        edit = null
        pickup = null
        axes = emptyList()
        primaryClip = null
        return committed
    }

    /** Cancels the stroke and rolls the layer back (e.g. gesture interrupt). */
    fun cancel() {
        if (!started) return
        edit?.rollback()
        baseline = null
        started = false
        processor = null
        rasterizer = null
        edit = null
        pickup = null
        axes = emptyList()
        primaryClip = null
    }

    private fun drawDabs(dabs: List<StrokeProcessor.Dab>) {
        val rz = rasterizer ?: return
        val s = spec ?: return
        for (d in dabs) {
            val jitterColor = if (s.hueJitter > 0f || s.saturationJitter > 0f || s.brightnessJitter > 0f) {
                val rnd = kotlin.random.Random(seedCounter * 31 + d.index)
                val hue = (rnd.nextFloat() * 2f - 1f) * s.hueJitter
                val sat = 1f + (rnd.nextFloat() * 2f - 1f) * s.saturationJitter
                val bri = 1f + (rnd.nextFloat() * 2f - 1f) * s.brightnessJitter
                Hsv.shift(color, hue, sat, bri)
            } else color

            var x = d.x
            var y = d.y
            var radius = d.radius
            if (s.scatter > 0f) {
                val rnd = kotlin.random.Random(seedCounter * 17 + d.index * 7)
                val ang = rnd.nextFloat() * (Math.PI * 2).toFloat()
                val dist = rnd.nextFloat() * s.scatter * s.size
                x += kotlin.math.cos(ang) * dist
                y += kotlin.math.sin(ang) * dist
            }
            if (s.sizeJitter > 0f) {
                val rnd = kotlin.random.Random(seedCounter * 13 + d.index * 11)
                radius *= 1f + (rnd.nextFloat() * 2f - 1f) * s.sizeJitter * 0.5f
            }
            if (s.rotationJitter > 0f && s.kind == BrushKind.STAMP) {
                // rotation jitter applied through a temporary spec is out of scope for v1 stamps
            }
            val flow = dabFlow(s, d.pressure)
                        val effective = StrokeProcessor.Dab(x, y, radius.coerceAtLeast(0.2f), d.pressure, d.direction,
                d.index, d.speed)
            // Reserve room for the whole dab + scatter on both sides of every
            // axis so ensure() never has to grow mid-dab.
            val extent = radius + s.size * 0.75f + 8f
            rz.reserve(x - extent, y - extent, x + extent, y + extent)
            for (a in axes) {
                val (mx, my) = mirrorPosition(a, x, y)
                rz.reserve(mx - extent, my - extent, mx + extent, my + extent)
            }
            // Sub-dabs (particles) reuse the same clip; symmetry applies per sub-dab.
            emitDab(rz, effective, jitterColor, flow, primaryClip)
            for (a in axes) {
                val (mx, my) = mirrorPosition(a, x, y)
                val mirrorDab = StrokeProcessor.Dab(
                    x = mx,
                    y = my,
                    radius = effective.radius,
                    pressure = d.pressure,
                    // Direction mirrors with the movement so grain-following stays
                    // consistent with the mirrored path.
                    direction = mirrorDirection(a, d.direction),
                    index = d.index,
                    speed = d.speed,
                )
                emitDab(rz, mirrorDab, jitterColor, flow, mirrorClip(a, axes))
            }
            // With both axes active the diagonal quadrant is mirrored too, as it
            // would be in any two-axis kaleidoscope symmetry.
            val v = axes.firstOrNull { it.vertical }
            val h = axes.firstOrNull { !it.vertical }
            if (v != null && h != null) {
                val bothDab = StrokeProcessor.Dab(
                    x = 2f * v.pivot - x,
                    y = 2f * h.pivot - y,
                    radius = effective.radius,
                    pressure = d.pressure,
                    direction = normalizeAngle(d.direction + Math.PI.toFloat()),
                    index = d.index,
                    speed = d.speed,
                )
                emitDab(rz, bothDab, jitterColor, flow, StrokeRasterizer.DabClip(minX = v.pivot, minY = h.pivot))
            }
            lastDabX = x
            lastDabY = y
        }
    }

    private fun dabFlow(s: BrushSpec, pressure: Float): Float {
        val base = s.flow.coerceIn(0f, 1f)
        if (s.pressureTarget == PressureTarget.SIZE) return base
        val shaped = s.curve.apply(pressure)
        val p = base * (1f - s.pressureFlow * (1f - shaped))
        // Technical pens should be uniform regardless of build-up.
        return when (s.kind) {
            BrushKind.PEN -> base
            else -> p.coerceIn(0.01f, 1f)
        }
    }

    /** Draws one dab (and accounts for its bounds) into the rasteriser. */
    private fun emitDab(
        rz: StrokeRasterizer,
        d: StrokeProcessor.Dab,
        color: Int,
        flow: Float,
        clip: StrokeRasterizer.DabClip?,
    ) {
        rz.dab(d, color, flow, seedCounter * 2654435761.toInt() + d.index, pickup, clip)
        markDabBounds(d.x, d.y, d.radius + 2f, finalized)
    }

    /** Reflection of a point across one symmetry axis (in document coordinates). */
    private fun mirrorPosition(a: Axis, x: Float, y: Float): Pair<Float, Float> =
        if (a.vertical) (2f * a.pivot - x) to y else x to (2f * a.pivot - y)

    private fun normalizeAngle(angle: Float): Float {
        val tau = (2.0 * Math.PI).toFloat()
        var a = angle % tau
        if (a < 0f) a += tau
        return a
    }

    /**
     * The movement direction mirrored across an axis. Grain that follows the
     * stroke direction then renders as the true reflection of the original.
     */
    private fun mirrorDirection(a: Axis, direction: Float): Float = when {
        a.vertical -> Math.PI.toFloat() - direction
        else -> -direction
    }

    /**
     * Clip for a mirrored dab: it draws only on the far side of its own axis,
     * and stays on the primary side of every other axis (which is what prevents
     * a stroke and its mirror from ever painting the same pixel twice).
     */
    private fun mirrorClip(axis: Axis, all: List<Axis>): StrokeRasterizer.DabClip {
        var minX: Float? = null
        var maxX: Float? = null
        var minY: Float? = null
        var maxY: Float? = null
        if (axis.vertical) minX = axis.pivot else minY = axis.pivot
        for (o in all) {
            if (o === axis) continue
            if (o.vertical) maxX = o.pivot else maxY = o.pivot
        }
        return StrokeRasterizer.DabClip(minX, maxX, minY, maxY)
    }

    /** Clip of the primary stroke: the near side of every active axis. */
    private fun primaryClipOf(axes: List<Axis>): StrokeRasterizer.DabClip? {
        if (axes.isEmpty()) return null
        var minX: Float? = null
        var maxX: Float? = null
        var minY: Float? = null
        var maxY: Float? = null
        for (a in axes) {
            if (a.vertical) maxX = a.pivot else maxY = a.pivot
        }
        return StrokeRasterizer.DabClip(minX, maxX, minY, maxY)
    }

    private fun markDabBounds(x: Float, y: Float, r: Float, final: RectI) {
        val b = RectI(
            kotlin.math.floor(x - r).toInt(), kotlin.math.floor(y - r).toInt(),
            kotlin.math.ceil(x + r).toInt() + 1, kotlin.math.ceil(y + r).toInt() + 1,
        )
        strokeBounds = strokeBounds.union(b)
        // Coverage already dropped from the buffer must not be flushed again: the
        // tile is final, so flushing it would write transparent pixels over it.
        if (final.isEmpty || !b.intersect(final).isEmpty) pendingBounds = pendingBounds.union(b)
    }

    /**
     * Composites the accumulated stroke buffer into the layer.
     *
     * The buffer holds cumulative coverage for the whole stroke, so it cannot be
     * simply alpha-composited again on top of the previous flush (that would
     * compound a 50 % brush into 100 %). It is instead composited against the
     * **pristine** pre-stroke pixels and the result replaces what the stroke has
     * written so far. Work is done per 256 px tile so `copyRegion`/`writeRegion`
     * touch only the pixels the stroke actually covers.
     */
    fun flush() {
        val rz = rasterizer ?: return
        val d = doc ?: return
        val l = layer ?: return
        val e = edit ?: return
        val s = spec ?: return
        val surface = l.surface ?: return
        val dirty = pendingBounds.intersect(d.canvasRect)
        if (dirty.isEmpty) return
        e.touchRect(dirty)
        // One baseline read per flush, sized to the whole dirty rect, so every tile
        // in the loop below hits the cache. `pristineFor` always returns pre-stroke
        // pixels, so rebuilding the cache never re-reads painted content.
        val want = padRect(dirty).intersect(d.canvasRect)
        val cached = baseline
        if (cached == null || !cached.window.contains(want)) {
            baseline = if (want.area <= BASELINE_MAX_AREA) {
                val pixels = IntArray(want.width * want.height)
                writePristine(e, surface, want, pixels)
                Baseline(want, pixels)
            } else null
        }
        val tile = TileSurface.TILE_SIZE
        val tx0 = dirty.left / tile
        val tx1 = (dirty.right - 1) / tile
        val ty0 = dirty.top / tile
        val ty1 = (dirty.bottom - 1) / tile
        for (ty in ty0..ty1) {
            for (tx in tx0..tx1) {
                val r = RectI(
                    maxOf(dirty.left, tx * tile),
                    maxOf(dirty.top, ty * tile),
                    minOf(dirty.right, tx * tile + tile),
                    minOf(dirty.bottom, ty * tile + tile),
                )
                if (r.isEmpty) continue
                val need = r.width * r.height
                val current = flushPayload
                if (current == null || current.size < need) flushPayload = IntArray(need)
                val payload = requireNotNull(flushPayload)
                writeBaseline(e, surface, r, payload)
                when (s.kind) {
                    BrushKind.ERASER -> rz.eraseFrom(payload, r.width, r.left, r.top, s.opacity, r)
                    BrushKind.SMUDGE -> rz.compositeInto(payload, r.width, r.left, r.top, s.opacity, r)
                    else -> rz.blendInto(payload, r.width, r.left, r.top, s.opacity, s.blend)
                }
                if (s.kind == BrushKind.WATERCOLOR || s.kind == BrushKind.OIL) {
                    applyWetLook(payload, r, s)
                }
                writeRegionFast(surface, r, payload, s)
            }
        }
        surface.touch()
        d.bumpRender()

        // A long stroke keeps every pixel it has painted in the scratch buffer,
        // because each flush re-composites the whole stroke against pristine
        // pixels. Only the newest tail of the stroke can still change after a
        // flush: dabs are drawn in order, and later dabs overlap only near the
        // current position. Once the buffer is big enough to matter, coverage
        // outside a generous window behind the dab is cleared and the pixels the
        // stroke already wrote are final. The window must cover every pixel the
        // next dabs can reach *and* every pixel this flush still needs to write,
        // otherwise the tail is either clipped or its coverage lost.
        if (rz.width.toLong() * rz.height > RECYCLE_AREA) {
            val reach = ((s.size * 2.5f) + 32f).toInt()
            val keep = RectI(
                minOf(dirty.left, kotlin.math.floor(lastDabX).toInt() - reach),
                minOf(dirty.top, kotlin.math.floor(lastDabY).toInt() - reach),
                maxOf(dirty.right, kotlin.math.ceil(lastDabX).toInt() + reach),
                maxOf(dirty.bottom, kotlin.math.ceil(lastDabY).toInt() + reach),
            )
            rz.recycleTo(keep)
            finalized = finalized.union(subtract(dirty, keep))
        }

        pendingBounds = RectI.EMPTY
        flushedBounds = flushedBounds.union(dirty)
        flushedNow = flushedNow.union(dirty)
    }

    private var flushedNow: RectI = RectI.EMPTY

    /** Bounds flushed since the previous [takeFlushed]. */
    fun takeFlushed(): RectI {
        val r = flushedNow
        flushedNow = RectI.EMPTY
        return r
    }

    private fun applyWetLook(payload: IntArray, r: RectI, s: BrushSpec) {
        // Darken the paint rim slightly to suggest pigment pooling; keeps the
        // effect bounded and does not alter alpha outside the stroke.
        val w = r.width
        for (y in 1 until r.height - 1) for (x in 1 until w - 1) {
            val i = y * w + x
            val p = payload[i]
            if (p == 0) continue
            val l = payload[i - 1]; val rr = payload[i + 1]; val up = payload[i - w]; val dn = payload[i + w]
            val edge = (alphaOf(p) - minOf(alphaOf(l), alphaOf(rr), alphaOf(up), alphaOf(dn))) / 255f
            if (edge > 0.15f) {
                val f = 1f - edge * 0.18f * s.wetness
                val a = (alphaOf(p) * f).toInt()
                payload[i] = (a shl 24) or (redOf(p) shl 16) or (greenOf(p) shl 8) or blueOf(p)
            }
        }
    }

    /** Reads the pre-stroke pixels for [r], falling back to live pixels for strokes without an edit. */
    private fun writePristine(edit: PixelEdit, surface: TileSurface, r: RectI, out: IntArray) {
        val pristine = edit.pristineFor(r)
        if (pristine == null) surface.copyRect(r, out) else pristine.copyRect(r, out)
    }

    /** Copies the pre-stroke pixels for [r] into [out]. */
    private fun writeBaseline(edit: PixelEdit, surface: TileSurface, r: RectI, out: IntArray) {
        val cache = baseline
        if (cache == null || !cache.window.contains(r)) {
            // Too large to cache: read straight from the pristine snapshot.
            writePristine(edit, surface, r, out)
            return
        }
        copyWindow(cache.pixels, cache.window, r, out)
    }

    private class Baseline(val window: RectI, val pixels: IntArray)

    private fun copyWindow(src: IntArray, window: RectI, r: RectI, out: IntArray) {
        for (y in 0 until r.height) {
            val from = (r.top + y - window.top) * window.width + (r.left - window.left)
            System.arraycopy(src, from, out, y * r.width, r.width)
        }
    }

    private fun padRect(r: RectI): RectI {
        val p = (requireNotNull(spec).size * 2f).toInt() + 64
        return RectI(r.left - p, r.top - p, r.right + p, r.bottom + p)
    }

    /**
     * Writes the payload back. The fast path copies whole tile segments; alpha lock
     * needs per-pixel inspection, so it keeps the slow path.
     */
    private fun writeRegionFast(surface: TileSurface, r: RectI, payload: IntArray, s: BrushSpec) {
        if (layer?.alphaLocked == true) {
            writeRegion(surface, r, payload, s)
            return
        }
        surface.writeRect(r, payload)
    }

    private fun copyRegion(surface: TileSurface, r: RectI, out: IntArray) {
        for (y in 0 until r.height) {
            for (x in 0 until r.width) {
                out[y * r.width + x] = surface.getPixel(r.left + x, r.top + y)
            }
        }
    }

    private fun writeRegion(surface: TileSurface, r: RectI, payload: IntArray, s: BrushSpec) {
        val alphaLock = layer?.alphaLocked == true
        for (y in 0 until r.height) {
            for (x in 0 until r.width) {
                val p = payload[y * r.width + x]
                val old = surface.getPixel(r.left + x, r.top + y)
                if (p == old) continue
                val finalP = if (alphaLock) {
                    // Alpha lock: keep original alpha, take new color.
                    val a = alphaOf(old)
                    if (a == 0) old else {
                        val na = alphaOf(p).coerceAtMost(a)
                        (a shl 24) or (redOf(p) shl 16) or (greenOf(p) shl 8) or blueOf(p)
                    }
                } else p
                surface.setPixel(r.left + x, r.top + y, finalP)
            }
        }
    }

    /**
     * Returns the part of [a] that is not yet covered by the flushed rect [b].
     *
     * A stroke grows in one direction at a time, so the unflushed remainder is
     * almost always the right-hand strip (horizontal stroke) or the bottom strip
     * (vertical stroke). If the remainder is neither, the whole rect is returned:
     * re-writing an already-flushed pixel is idempotent once the rasteriser has
     * been cleared, because the source buffer there is transparent.
     */
    private fun subtract(a: RectI, b: RectI): RectI {
        if (b.isEmpty) return a
        val i = a.intersect(b)
        if (i.isEmpty) return a
        val coversFullHeight = i.top <= a.top && i.bottom >= a.bottom
        val coversFullWidth = i.left <= a.left && i.right >= a.right
        if (coversFullHeight && i.left <= a.left) {
            return if (i.right >= a.right) RectI.EMPTY else RectI(i.right, a.top, a.right, a.bottom)
        }
        if (coversFullWidth && i.top <= a.top) {
            return if (i.bottom >= a.bottom) RectI.EMPTY else RectI(a.left, i.bottom, a.right, a.bottom)
        }
        return a
    }

    /** Reads pixels for smudge/wet brushes and writes back the picked-up paint. */
    private class Pickup(private val surface: TileSurface, private val spec: BrushSpec) : StrokeRasterizer.ColorPickup {
        private val cache = HashMap<Long, Int>()
        override fun sample(x: Int, y: Int): Int {
            val k = (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)
            cache[k]?.let { return it }
            val p = surface.getPixel(x, y)
            cache[k] = p
            return p
        }

        override fun feedback(x: Int, y: Int, color: Int, amount: Float) {
            val k = (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)
            val prev = cache[k] ?: surface.getPixel(x, y)
            // Blend picked-up color toward the new paint (smudge transport).
            cache[k] = PixelOps.composite(
                com.monaart.engine.paint.BlendMode.Normal, prev,
                com.monaart.engine.paint.premulToStraight(color), 1f, spec.smudgeRate * amount,
            ).let { com.monaart.engine.paint.straightToPremul(com.monaart.engine.paint.premulToStraight(it)) }
        }
    }
}
