package com.monaart.engine.brush

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Layer
import com.monaart.engine.document.PixelEdit
import com.monaart.engine.document.TileSurface
import com.monaart.engine.paint.PixelOps
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.redOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.blueOf

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

    /** Region of the document touched by the current stroke. */
    private var strokeBounds: RectI = RectI.EMPTY
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

    fun begin(doc: ArtDocument, layer: Layer, spec: BrushSpec, color: Int, seed: Int = (System.nanoTime() and 0xFFFF).toInt()) {
        require(!started) { "A stroke is already in progress" }
        require(!layer.locked) { "Layer is locked" }
        require(!layer.isGroup) { "Cannot paint on a group layer" }
        this.doc = doc
        this.layer = layer
        this.spec = spec
        this.color = color
        seedCounter = seed
        processor = StrokeProcessor(spec)
        val pad = (spec.size * 0.75f).toInt() + 4
        rasterizer = StrokeRasterizer(spec, pad)
        edit = PixelEdit(doc, strokeLabel(spec), layer)
        started = true
        strokeBounds = RectI.EMPTY
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
        started = false
        processor = null
        rasterizer = null
        edit = null
        pickup = null
        return committed
    }

    /** Cancels the stroke and rolls the layer back (e.g. gesture interrupt). */
    fun cancel() {
        if (!started) return
        edit?.rollback()
        started = false
        processor = null
        rasterizer = null
        edit = null
        pickup = null
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
            val effective = StrokeProcessor.Dab(x, y, radius.coerceAtLeast(0.2f), d.pressure, d.direction, d.index, d.speed)
            // Reserve room for the whole dab + scatter so ensure() never has to grow mid-dab.
            val extent = radius + s.size * 0.75f + 8f
            rz.reserve(x - extent, y - extent, x + extent, y + extent)
            rz.dab(effective, jitterColor, flow, seedCounter * 2654435761.toInt() + d.index, pickup)
            markDabBounds(x, y, radius + 2f)
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

    private fun markDabBounds(x: Float, y: Float, r: Float) {
        val b = RectI(
            kotlin.math.floor(x - r).toInt(), kotlin.math.floor(y - r).toInt(),
            kotlin.math.ceil(x + r).toInt() + 1, kotlin.math.ceil(y + r).toInt() + 1,
        )
        strokeBounds = strokeBounds.union(b)
        pendingBounds = pendingBounds.union(b)
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
                val payload = IntArray(r.width * r.height)
                writePristine(e, surface, r, payload)
                when (s.kind) {
                    BrushKind.ERASER -> rz.eraseFrom(payload, r.width, r.left, r.top, s.opacity, r)
                    BrushKind.SMUDGE -> rz.compositeInto(payload, r.width, r.left, r.top, s.opacity, r)
                    else -> rz.blendInto(payload, r.width, r.left, r.top, s.opacity, s.blend)
                }
                if (s.kind == BrushKind.WATERCOLOR || s.kind == BrushKind.OIL) {
                    applyWetLook(payload, r, s)
                }
                writeRegion(surface, r, payload, s)
            }
        }
        surface.touch()
        d.bumpRender()
        pendingBounds = RectI.EMPTY
        flushedBounds = flushedBounds.union(dirty)
        flushedNow = flushedNow.union(dirty)
    }

    /** Reads the pre-stroke pixels for [r], falling back to live pixels for strokes without an edit. */
    private fun writePristine(edit: PixelEdit, surface: TileSurface, r: RectI, out: IntArray) {
        val pristine = edit.pristineFor(r) ?: run {
            copyRegion(surface, r, out)
            return
        }
        for (y in 0 until r.height) {
            for (x in 0 until r.width) {
                out[y * r.width + x] = pristine.getPixel(r.left + x, r.top + y)
            }
        }
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
