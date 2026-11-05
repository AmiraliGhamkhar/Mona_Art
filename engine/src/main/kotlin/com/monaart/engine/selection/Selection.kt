package com.monaart.engine.selection

import com.monaart.engine.document.Layer
import com.monaart.engine.paint.PixelOps
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf

/**
 * A 256-level coverage mask (0 = outside, 255 = fully inside) with an
 * anti-aliased edge. Selections gate painting, fills, filters and adjustments.
 */
class Selection(val width: Int, val height: Int) {
    val mask: ByteArray = ByteArray(width * height)
    var bounds: RectI = RectI.EMPTY
        private set

    val isEmpty: Boolean get() = bounds.isEmpty

    fun coverage(x: Int, y: Int): Float {
        if (x < 0 || y < 0 || x >= width || y >= height) return 0f
        return (mask[y * width + x].toInt() and 0xFF) / 255f
    }

    fun setCoverage(x: Int, y: Int, v: Float) {
        if (x < 0 || y < 0 || x >= width || y >= height) return
        mask[y * width + x] = (v.coerceIn(0f, 1f) * 255f).toInt().toByte()
    }

    fun recomputeBounds() {
        var l = Int.MAX_VALUE; var t = Int.MAX_VALUE; var r = -1; var b = -1
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                if (mask[row + x].toInt() != 0) {
                    if (x < l) l = x
                    if (x > r) r = x
                    if (y < t) t = y
                    if (y > b) b = y
                }
            }
        }
        bounds = if (r < 0) RectI.EMPTY else RectI(l, t, r + 1, b + 1)
    }

    fun copy(): Selection {
        val s = Selection(width, height)
        System.arraycopy(mask, 0, s.mask, 0, mask.size)
        s.bounds = bounds
        return s
    }

    fun clear() {
        java.util.Arrays.fill(mask, 0)
        bounds = RectI.EMPTY
    }

    fun selectAll() {
        java.util.Arrays.fill(mask, 0xFF.toByte())
        bounds = RectI.wh(width, height)
    }

    /** Inverts the selection (feathering information is preserved logically). */
    fun invert() {
        for (i in mask.indices) mask[i] = (255 - (mask[i].toInt() and 0xFF)).toByte()
        recomputeBounds()
    }

    /** Blur-based feathering with a box kernel of radius [radius] px. */
    fun feather(radius: Int) {
        if (radius <= 0) return
        val tmp = ByteArray(mask.size)
        boxBlurPass(radius, horizontal = true, dst = tmp)
        System.arraycopy(tmp, 0, mask, 0, mask.size)
        boxBlurPass(radius, horizontal = false, dst = tmp)
        System.arraycopy(tmp, 0, mask, 0, mask.size)
        recomputeBounds()
    }

    private fun boxBlurPass(r: Int, horizontal: Boolean, dst: ByteArray) {
        val w = width; val h = height
        val len = if (horizontal) w else h
        val lines = if (horizontal) h else w
        val window = 2 * r + 1
        for (line in 0 until lines) {
            var sum = 0
            for (k in -r..r) {
                val i = k.coerceIn(0, len - 1)
                sum += mask[if (horizontal) line * w + i else i * w + line].toInt() and 0xFF
            }
            for (i in 0 until len) {
                val v = sum / window
                if (horizontal) dst[line * w + i] = v.toByte() else dst[i * w + line] = v.toByte()
                val addIdx = (i + r + 1).coerceIn(0, len - 1)
                val remIdx = (i - r).coerceIn(0, len - 1)
                sum += (mask[if (horizontal) line * w + addIdx else addIdx * w + line].toInt() and 0xFF)
                sum -= (mask[if (horizontal) line * w + remIdx else remIdx * w + line].toInt() and 0xFF)
            }
        }
    }

    /** Grows the selection by [px] pixels via dilation. */
    fun expand(px: Int) {
        if (px <= 0) return
        val iterations = px.coerceAtMost(64)
        repeat(iterations) { dilateOnce() }
        recomputeBounds()
    }

    /** Shrinks the selection by [px] pixels via erosion. */
    fun contract(px: Int) {
        if (px <= 0) return
        val iterations = px.coerceAtMost(64)
        repeat(iterations) {
            val inv = copy()
            inv.invert()
            inv.dilateOnce()
            for (i in mask.indices) {
                mask[i] = if ((inv.mask[i].toInt() and 0xFF) > 0) 0 else mask[i]
            }
        }
        recomputeBounds()
    }

    private fun dilateOnce() {
        val src = mask.copyOf()
        for (y in 0 until height) for (x in 0 until width) {
            var v = src[y * width + x].toInt() and 0xFF
            if (x > 0) v = maxOf(v, src[y * width + x - 1].toInt() and 0xFF)
            if (x < width - 1) v = maxOf(v, src[y * width + x + 1].toInt() and 0xFF)
            if (y > 0) v = maxOf(v, src[(y - 1) * width + x].toInt() and 0xFF)
            if (y < height - 1) v = maxOf(v, src[(y + 1) * width + x].toInt() and 0xFF)
            mask[y * width + x] = v.toByte()
        }
    }

    companion object {
        fun rectangle(w: Int, h: Int, r: RectI, feather: Int = 0): Selection {
            val s = Selection(w, h)
            val rr = r.intersect(RectI.wh(w, h))
            for (y in rr.top until rr.bottom) for (x in rr.left until rr.right) {
                s.mask[y * w + x] = 0xFF.toByte()
            }
            if (feather > 0) s.feather(feather)
            s.recomputeBounds()
            return s
        }

        /** Anti-aliased ellipse inscribed in [r]. */
        fun ellipse(w: Int, h: Int, r: RectI, feather: Int = 0): Selection {
            val s = Selection(w, h)
            val rr = r.intersect(RectI.wh(w, h))
            if (rr.isEmpty) return s
            val cx = (rr.left + rr.right) * 0.5f
            val cy = (rr.top + rr.bottom) * 0.5f
            val rx = rr.width * 0.5f
            val ry = rr.height * 0.5f
            for (y in rr.top until rr.bottom) for (x in rr.left until rr.right) {
                val nx = (x + 0.5f - cx) / rx
                val ny = (y + 0.5f - cy) / ry
                val d = kotlin.math.sqrt(nx * nx + ny * ny)
                val cov = ((1f - d) * minOf(rx, ry) + 0.5f).coerceIn(0f, 1f)
                s.mask[y * w + x] = (cov * 255f).toInt().toByte()
            }
            if (feather > 0) s.feather(feather)
            s.recomputeBounds()
            return s
        }

        /** Freehand lasso from a polygon path. */
        fun polygon(w: Int, h: Int, points: List<Pair<Float, Float>>, feather: Int = 0): Selection {
            val s = Selection(w, h)
            if (points.size < 3) return s
            val minY = points.minOf { it.second }.toInt().coerceIn(0, h - 1)
            val maxY = points.maxOf { it.second }.toInt().coerceIn(0, h - 1)
            val xs = ArrayList<Float>()
            for (y in minY..maxY) {
                xs.clear()
                val fy = y + 0.5f
                for (i in points.indices) {
                    val a = points[i]
                    val b = points[(i + 1) % points.size]
                    if ((a.second <= fy && b.second > fy) || (b.second <= fy && a.second > fy)) {
                        val t = (fy - a.second) / (b.second - a.second)
                        xs.add(a.first + t * (b.first - a.first))
                    }
                }
                xs.sort()
                var i = 0
                while (i + 1 < xs.size) {
                    val x0 = kotlin.math.ceil(xs[i] - 0.5f).toInt().coerceIn(0, w - 1)
                    val x1 = kotlin.math.floor(xs[i + 1] - 0.5f).toInt().coerceIn(0, w - 1)
                    for (x in x0..x1) s.mask[y * w + x] = 0xFF.toByte()
                    i += 2
                }
            }
            if (feather > 0) s.feather(feather)
            s.recomputeBounds()
            return s
        }

        /** Magic wand: flood fill from a seed by color tolerance over the flattened layer. */
                fun magicWand(layer: Layer, w: Int, h: Int, seedX: Int, seedY: Int, tolerance: Float,
            contiguous: Boolean = true): Selection {
            val s = Selection(w, h)
            val surface = layer.surface ?: return s
            val seed = surface.getPixel(seedX, seedY)
            val tol = (tolerance.coerceIn(0f, 1f) * 255f * 3f)
            fun matches(c: Int): Boolean {
                val da = kotlin.math.abs(alphaOf(c) - alphaOf(seed))
                val dr = kotlin.math.abs(redOf(c) - redOf(seed))
                val dg = kotlin.math.abs(greenOf(c) - greenOf(seed))
                val db = kotlin.math.abs(blueOf(c) - blueOf(seed))
                return da + dr + dg + db <= tol
            }
            if (!contiguous) {
                for (y in 0 until h) for (x in 0 until w) {
                    if (matches(surface.getPixel(x, y))) s.mask[y * w + x] = 0xFF.toByte()
                }
                s.recomputeBounds()
                return s
            }
            val visited = BooleanArray(w * h)
            val stack = ArrayDeque<Int>()
            stack.addLast(seedY * w + seedX)
            while (stack.isNotEmpty()) {
                val idx = stack.removeLast()
                if (visited[idx]) continue
                visited[idx] = true
                val x = idx % w; val y = idx / w
                if (!matches(surface.getPixel(x, y))) continue
                s.mask[idx] = 0xFF.toByte()
                if (x > 0) stack.addLast(idx - 1)
                if (x < w - 1) stack.addLast(idx + 1)
                if (y > 0) stack.addLast(idx - w)
                if (y < h - 1) stack.addLast(idx + w)
            }
            s.recomputeBounds()
            return s
        }
    }
}

/** Composites [src] (premultiplied, size w*h at origin) into [dst] restricted to selection coverage. */
object SelectionOps {
        fun clipCompositeInto(dst: com.monaart.engine.document.TileSurface, src: com.monaart.engine.paint.Pixmap,
        sel: Selection?) {
        if (sel == null) {
            dst.drawPixmap(src)
            return
        }
        for (y in 0 until minOf(src.height, dst.height)) for (x in 0 until minOf(src.width, dst.width)) {
            val c = sel.coverage(x, y)
            if (c <= 0f) continue
            val p = src.pixels[y * src.width + x]
            if (p == 0) continue
            if (x >= dst.width || y >= dst.height) continue
            dst.setPixel(x, y, PixelOps.over(dst.getPixel(x, y), PixelOps.scaleAlpha(p, c)))
        }
        dst.touch()
    }
}
