package com.monaart.engine.document

import com.monaart.engine.history.History
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf
import com.monaart.engine.selection.Selection

/**
 * Canvas-level transformations (crop, resize, rotate, flip) and content
 * transforms (translate/scale/rotate of a layer region or selection).
 */
object Transforms {

    fun crop(doc: ArtDocument, r: RectI): Boolean {
        val rr = r.intersect(doc.canvasRect)
        if (rr.isEmpty || (rr.width == doc.width && rr.height == doc.height)) return false
        val oldW = doc.width
        val oldH = doc.height
        val oldLayers = doc.layers.toList()
        val newLayers = doc.layers.map { cropLayer(it, rr) }
        doc.layers.clear()
        doc.layers.addAll(newLayers)
        doc.width = rr.width
        doc.height = rr.height
        doc.bumpStructure()
        doc.history.push(History.Lambda("Crop canvas",
            onUndo = {
                doc.layers.clear(); doc.layers.addAll(oldLayers)
                doc.width = oldW; doc.height = oldH
                doc.bumpStructure()
            },
            onRedo = {
                doc.layers.clear(); doc.layers.addAll(newLayers)
                doc.width = rr.width; doc.height = rr.height
                doc.bumpStructure()
            }))
        return true
    }

    private fun cropLayer(layer: Layer, r: RectI): Layer {
        val surface = layer.surface
        val copy = Layer(layer.id, layer.name, surface?.let { TileSurface(r.width, r.height, it.tileSize) }, layer.kind)
        copy.visible = layer.visible
        copy.locked = layer.locked
        copy.alphaLocked = layer.alphaLocked
        copy.clipping = layer.clipping
        copy.opacity = layer.opacity
        copy.blend = layer.blend
        if (surface != null) {
            for (y in 0 until r.height) for (x in 0 until r.width) {
                val p = surface.getPixel(r.left + x, r.top + y)
                if (p != 0) copy.surface!!.setPixel(x, y, p)
            }
            copy.surface!!.touch()
        }
        copy.children = layer.children.map { cropLayer(it, r) }.toMutableList()
        return copy
    }

    /** Changes canvas size; [anchor] is where existing content sits in the new canvas. */
    fun resizeCanvas(doc: ArtDocument, w: Int, h: Int, offsetX: Int = 0, offsetY: Int = 0): Boolean {
        require(w > 0 && h > 0)
        if (w == doc.width && h == doc.height && offsetX == 0 && offsetY == 0) return false
        val oldW = doc.width
        val oldH = doc.height
        val oldLayers = doc.layers.toList()
        fun resizeLayer(layer: Layer): Layer {
            val s = layer.surface
            val copy = Layer(layer.id, layer.name, s?.let { TileSurface(w, h, it.tileSize) }, layer.kind)
            copy.visible = layer.visible; copy.locked = layer.locked; copy.alphaLocked = layer.alphaLocked
            copy.clipping = layer.clipping; copy.opacity = layer.opacity; copy.blend = layer.blend
            if (s != null) {
                val t = copy.surface!!
                for (y in 0 until minOf(h - offsetY, s.height)) for (x in 0 until minOf(w - offsetX, s.width)) {
                    if (y + offsetY < 0 || x + offsetX < 0) continue
                    val p = s.getPixel(x, y)
                    if (p != 0) t.setPixel(x + offsetX, y + offsetY, p)
                }
                t.touch()
            }
            copy.children = layer.children.map { resizeLayer(it) }.toMutableList()
            return copy
        }
        val newLayers = doc.layers.map { resizeLayer(it) }
        doc.layers.clear(); doc.layers.addAll(newLayers)
        doc.width = w; doc.height = h
        doc.bumpStructure()
        doc.history.push(History.Lambda("Resize canvas",
            onUndo = { doc.layers.clear(); doc.layers.addAll(oldLayers); doc.width = oldW; doc.height = oldH; doc.bumpStructure() },
            onRedo = { doc.layers.clear(); doc.layers.addAll(newLayers); doc.width = w; doc.height = h; doc.bumpStructure() }))
        return true
    }

    fun flip(doc: ArtDocument, horizontal: Boolean): Boolean {
        val oldLayers = doc.layers.toList()
        val newLayers = doc.layers.map { flipLayer(it, doc.width, doc.height, horizontal) }
        doc.layers.clear(); doc.layers.addAll(newLayers)
        doc.bumpStructure()
        doc.history.push(History.Lambda(if (horizontal) "Flip horizontal" else "Flip vertical",
            onUndo = { doc.layers.clear(); doc.layers.addAll(oldLayers); doc.bumpStructure() },
            onRedo = { doc.layers.clear(); doc.layers.addAll(newLayers); doc.bumpStructure() }))
        return true
    }

    private fun flipLayer(layer: Layer, w: Int, h: Int, horizontal: Boolean): Layer {
        val s = layer.surface
        val copy = Layer(layer.id, layer.name, s?.let { TileSurface(it.width, it.height, it.tileSize) }, layer.kind)
        copy.visible = layer.visible; copy.locked = layer.locked; copy.alphaLocked = layer.alphaLocked
        copy.clipping = layer.clipping; copy.opacity = layer.opacity; copy.blend = layer.blend
        if (s != null) {
            val t = copy.surface!!
            for (y in 0 until s.height) for (x in 0 until s.width) {
                val p = s.getPixel(x, y)
                if (p == 0) continue
                val nx = if (horizontal) s.width - 1 - x else x
                val ny = if (horizontal) y else s.height - 1 - y
                t.setPixel(nx, ny, p)
            }
            t.touch()
        }
        copy.children = layer.children.map { flipLayer(it, w, h, horizontal) }.toMutableList()
        return copy
    }

    /** Rotates the canvas by 90/180/270 degrees clockwise (dimensions swap for 90/270). */
    fun rotateCanvas(doc: ArtDocument, degrees: Int): Boolean {
        val d = ((degrees % 360) + 360) % 360
        if (d == 0) return false
        val oldW = doc.width
        val oldH = doc.height
        val newW = if (d == 180) oldW else oldH
        val newH = if (d == 180) oldH else oldW
        val oldLayers = doc.layers.toList()
        fun rot(layer: Layer): Layer {
            val s = layer.surface
            val copy = Layer(layer.id, layer.name, s?.let { TileSurface(newW, newH, it.tileSize) }, layer.kind)
            copy.visible = layer.visible; copy.locked = layer.locked; copy.alphaLocked = layer.alphaLocked
            copy.clipping = layer.clipping; copy.opacity = layer.opacity; copy.blend = layer.blend
            if (s != null) {
                val t = copy.surface!!
                for (y in 0 until s.height) for (x in 0 until s.width) {
                    val p = s.getPixel(x, y)
                    if (p == 0) continue
                    val (nx, ny) = when (d) {
                        90 -> (s.height - 1 - y) to x
                        180 -> (s.width - 1 - x) to (s.height - 1 - y)
                        else -> y to (s.width - 1 - x)
                    }
                    t.setPixel(nx, ny, p)
                }
                t.touch()
            }
            copy.children = layer.children.map { rot(it) }.toMutableList()
            return copy
        }
        val newLayers = doc.layers.map { rot(it) }
        doc.layers.clear(); doc.layers.addAll(newLayers)
        doc.width = newW; doc.height = newH
        doc.bumpStructure()
        doc.history.push(History.Lambda("Rotate canvas $d°",
            onUndo = {
                doc.layers.clear(); doc.layers.addAll(oldLayers); doc.width = oldW; doc.height = oldH; doc.bumpStructure()
            },
            onRedo = {
                doc.layers.clear(); doc.layers.addAll(newLayers); doc.width = newW; doc.height = newH; doc.bumpStructure()
            }))
        return true
    }

    /**
     * Free transform of the active layer's content (or the selection) by an
     * affine matrix applied about the canvas centre: [scaleX]/[scaleY] and
     * [rotationDeg], plus translation. Bilinear sampling keeps edges smooth.
     */
    fun transformLayer(
        doc: ArtDocument,
        layer: Layer,
        scaleX: Float = 1f,
        scaleY: Float = 1f,
        rotationDeg: Float = 0f,
        tx: Float = 0f,
        ty: Float = 0f,
        selection: Selection? = null,
        label: String = "Transform",
    ): Boolean {
        val s = layer.surface ?: return false
        if (scaleX == 1f && scaleY == 1f && rotationDeg == 0f && tx == 0f && ty == 0f) return false
        val region = selection?.bounds ?: RectI.wh(doc.width, doc.height)
        val cx = doc.width * 0.5f
        val cy = doc.height * 0.5f
        val rad = rotationDeg * (Math.PI.toFloat() / 180f)
        val cos = kotlin.math.cos(rad)
        val sin = kotlin.math.sin(rad)
        // Source snapshot of the whole layer for sampling.
        val src = Pixmap(s.width, s.height)
        for (y in 0 until s.height) for (x in 0 until s.width) src.pixels[y * s.width + x] = s.getPixel(x, y)
        val edit = PixelEdit(doc, label, layer)
        // Clear the transformed area first for a genuine move rather than smearing.
        edit.touchRect(region)
        for (y in region.top until region.bottom) for (x in region.left until region.right) {
            if (selection != null && selection.coverage(x, y) <= 0f) continue
            s.setPixel(x, y, 0)
        }
        for (y in region.top until region.bottom) {
            if (y < 0 || y >= doc.height) continue
            for (x in region.left until region.right) {
                if (x < 0 || x >= doc.width) continue
                // Inverse map destination -> source.
                var dx = x + 0.5f - cx - tx
                var dy = y + 0.5f - cy - ty
                val rx = (dx * cos + dy * sin) / (if (scaleX == 0f) 1e-4f else scaleX)
                val ry = (-dx * sin + dy * cos) / (if (scaleY == 0f) 1e-4f else scaleY)
                dx = rx + cx
                dy = ry + cy
                if (dx < 0f || dy < 0f || dx >= s.width || dy >= s.height) continue
                val p = src.sampleBilinear(dx, dy)
                if (p == 0) continue
                val cov = selection?.coverage(x, y) ?: 1f
                val cur = s.getPixel(x, y)
                s.setPixel(x, y, com.monaart.engine.paint.PixelOps.over(cur, com.monaart.engine.paint.PixelOps.scaleAlpha(p, cov)))
            }
        }
        s.touch()
        doc.bumpRender()
        return edit.commit()
    }

    /** Pastes a pixmap into a layer at the given offset (used by import / duplicate-from-selection). */
    fun pasteInto(doc: ArtDocument, layer: Layer, src: Pixmap, dx: Int, dy: Int, opacity: Float = 1f): Boolean {
        val s = layer.surface ?: return false
        val region = RectI(dx, dy, dx + src.width, dy + src.height).intersect(doc.canvasRect)
        if (region.isEmpty) return false
        val edit = PixelEdit(doc, "Paste", layer)
        edit.touchRect(region)
        for (y in region.top until region.bottom) for (x in region.left until region.right) {
            val p = src.get(x - dx, y - dy)
            if (p == 0) continue
            val cov = doc.selection?.coverage(x, y) ?: 1f
            if (cov <= 0f) continue
            s.setPixel(x, y, com.monaart.engine.paint.PixelOps.over(s.getPixel(x, y), com.monaart.engine.paint.PixelOps.scaleAlpha(p, cov * opacity)))
        }
        s.touch()
        doc.bumpRender()
        return edit.commit()
    }

    /** Copies the selected region of the active layer into a new pixmap (for copy/cut). */
    fun extractSelection(doc: ArtDocument, layer: Layer): Pair<Pixmap, RectI>? {
        val s = layer.surface ?: return null
        val r = doc.selection?.bounds?.intersect(doc.canvasRect) ?: return null
        if (r.isEmpty) return null
        val out = Pixmap(r.width, r.height)
        for (y in 0 until r.height) for (x in 0 until r.width) {
            val cov = doc.selection?.coverage(r.left + x, r.top + y) ?: 1f
            if (cov <= 0f) continue
            out.pixels[y * r.width + x] = com.monaart.engine.paint.PixelOps.scaleAlpha(s.getPixel(r.left + x, r.top + y), cov)
        }
        return out to r
    }

    /** Builds a "perspective-ish" warp by shearing rows proportionally (non-destructive-free v1 warp). */
    fun warp(doc: ArtDocument, layer: Layer, strength: Float): Boolean {
        val s = layer.surface ?: return false
        if (strength == 0f) return false
        val src = Pixmap(s.width, s.height)
        for (y in 0 until s.height) for (x in 0 until s.width) src.pixels[y * s.width + x] = s.getPixel(x, y)
        val edit = PixelEdit(doc, "Warp", layer)
        edit.touchRect(doc.canvasRect)
        for (y in 0 until s.height) {
            val t = y.toFloat() / s.height
            val shift = (kotlin.math.sin(t * Math.PI.toFloat()) * strength * s.width * 0.25f)
            for (x in 0 until s.width) {
                val sx = x - shift
                val p = if (sx >= 0f && sx < s.width - 1) src.sampleBilinear(sx + 0.5f, y + 0.5f) else 0
                s.setPixel(x, y, p)
            }
        }
        s.touch()
        doc.bumpRender()
        return edit.commit()
    }

    /** Liquify: local push of pixels along a drag direction. */
    fun liquify(doc: ArtDocument, layer: Layer, cx: Int, cy: Int, dx: Float, dy: Float, radius: Int, amount: Float): Boolean {
        val s = layer.surface ?: return false
        val r = RectI.around(cx.toFloat(), cy.toFloat(), radius.toFloat()).intersect(doc.canvasRect)
        if (r.isEmpty) return false
        val src = Pixmap(r.width, r.height)
        for (y in 0 until r.height) for (x in 0 until r.width) src.pixels[y * r.width + x] = s.getPixel(r.left + x, r.top + y)
        val edit = PixelEdit(doc, "Liquify", layer)
        edit.touchRect(r)
        for (y in 0 until r.height) for (x in 0 until r.width) {
            val gx = r.left + x
            val gy = r.top + y
            val ddx = gx - cx
            val ddy = gy - cy
            val dist = kotlin.math.sqrt((ddx * ddx + ddy * ddy).toFloat())
            if (dist > radius) continue
            val falloff = 1f - dist / radius
            val f = falloff * falloff * amount
            val sampleX = x - dx * f
            val sampleY = y - dy * f
            val p = if (sampleX >= 0f && sampleY >= 0f && sampleX < r.width && sampleY < r.height)
                src.sampleBilinear(sampleX + 0.5f, sampleY + 0.5f) else 0
            s.setPixel(gx, gy, p)
        }
        s.touch()
        doc.bumpRender()
        return edit.commit()
    }

    // -----------------------------------------------------------------------
    // Interactive transform / liquify sessions
    //
    // Both tools let the user drag and preview the result live, which means the
    // cumulative transform has to be re-rendered from the pixels captured when
    // the gesture started. A [PixelEditSession] does exactly that: it owns one
    // undoable edit for the whole gesture (one undo step, not one per sample).
    // -----------------------------------------------------------------------

    /** One undoable edit shared by a whole interactive gesture. */
    class PixelEditSession(doc: ArtDocument, label: String, layer: Layer) {
        private val surface = layer.surface
        private val edit = PixelEdit(doc, label, layer)
        private var src: Pixmap? = null
        private var bounds: RectI = RectI.EMPTY
        private val docRef = doc

        /** Captures the pixels the session will rewrite. Returns false when there is nothing to edit. */
        fun begin(region: RectI): Boolean {
            val s = surface ?: return false
            val r = region.intersect(docRef.canvasRect)
            if (r.isEmpty) return false
            bounds = r
            edit.touchRect(r)
            val snapshot = Pixmap(r.width, r.height)
            for (y in 0 until r.height) for (x in 0 until r.width) {
                snapshot.pixels[y * r.width + x] = s.getPixel(r.left + x, r.top + y)
            }
            src = snapshot
            return true
        }

        /** Full-canvas variant for transforms that may move pixels anywhere. */
        fun beginCanvas(): Boolean = begin(RectI.wh(docRef.width, docRef.height))

        /** Restores the captured pixels, then applies [map]; called on every drag sample. */
        fun render(map: (dstX: Int, dstY: Int, write: (Int) -> Unit) -> Unit) {
            val s = surface ?: return
            val snapshot = src ?: return
            val o = bounds
            for (y in 0 until o.height) {
                for (x in 0 until o.width) {
                    s.setPixel(o.left + x, o.top + y, snapshot.pixels[y * o.width + x])
                }
            }
            for (y in 0 until o.height) {
                for (x in 0 until o.width) {
                    map(o.left + x, o.top + y) { value -> s.setPixel(o.left + x, o.top + y, value) }
                }
            }
            s.touch()
            docRef.bumpRender()
        }

        /** Source pixels captured at the start of the gesture. */
        @PublishedApi internal fun snapshot(): Pixmap? = src
        @PublishedApi internal fun origin(): RectI = bounds
        @PublishedApi internal fun surfaceRef(): TileSurface? = surface
        @PublishedApi internal fun docRef(): ArtDocument = docRef

        fun commit(): Boolean = edit.commit()
        fun cancel() {
            edit.rollback()
            src = null
        }
    }

    /**
     * Re-renders [session] with an affine transform applied around [pivot].
     * `tx`/`ty` are in document pixels.
     */
    fun renderAffine(
        session: PixelEditSession,
        pivotX: Float,
        pivotY: Float,
        scaleX: Float,
        scaleY: Float,
        rotationDeg: Float,
        tx: Float,
        ty: Float,
    ) {
        val src = session.snapshot() ?: return
        val o = session.origin()
        val s = session.surfaceRef() ?: return
        val rad = rotationDeg * (Math.PI.toFloat() / 180f)
        val cos = kotlin.math.cos(rad)
        val sin = kotlin.math.sin(rad)
        val sx = if (kotlin.math.abs(scaleX) < 1e-4f) 1e-4f else scaleX
        val sy = if (kotlin.math.abs(scaleY) < 1e-4f) 1e-4f else scaleY
        session.render { x, y, write ->
            // Inverse map: destination -> captured snapshot.
            val dx = x + 0.5f - pivotX - tx
            val dy = y + 0.5f - pivotY - ty
            val rx = (dx * cos + dy * sin) / sx + pivotX
            val ry = (-dx * sin + dy * cos) / sy + pivotY
            val lx = rx - o.left
            val ly = ry - o.top
            val value = if (lx < 0f || ly < 0f || lx >= src.width || ly >= src.height) 0
            else src.sampleBilinear(lx, ly)
            write(value)
        }
    }

    /** Accumulated liquify push; [pushes] holds (x, y, dx, dy) samples in document space. */
    fun renderLiquify(
        session: PixelEditSession,
        pushes: List<FloatArray>,
        radius: Float,
    ): Boolean {
        if (pushes.isEmpty()) return false
        val src = session.snapshot() ?: return false
        val o = session.origin()
        val s = session.surfaceRef() ?: return false
        val r = radius.coerceAtLeast(1f)
        session.render { x, y, write ->
            var sx = x + 0.5f
            var sy = y + 0.5f
            // Walk the push history backwards so the newest drag dominates.
            for (i in pushes.indices.reversed()) {
                val p = pushes[i]
                val ddx = sx - p[0]
                val ddy = sy - p[1]
                val dist = kotlin.math.sqrt(ddx * ddx + ddy * ddy)
                if (dist > r) continue
                val falloff = 1f - dist / r
                val f = falloff * falloff
                sx -= p[2] * f
                sy -= p[3] * f
            }
            val lx = sx - o.left
            val ly = sy - o.top
            val value = if (lx < 0f || ly < 0f || lx >= src.width || ly >= src.height) 0
            else src.sampleBilinear(lx, ly)
            write(value)
        }
        s.touch()
        session.docRef().bumpRender()
        return true
    }
}
