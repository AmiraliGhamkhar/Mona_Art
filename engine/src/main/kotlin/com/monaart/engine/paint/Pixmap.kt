package com.monaart.engine.paint

/** Immutable integer rectangle: [left, right) x [top, bottom). */
data class RectI(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val isEmpty: Boolean get() = right <= left || bottom <= top
    val area: Long get() = width.toLong() * height.toLong()

    fun intersect(o: RectI): RectI {
        val l = maxOf(left, o.left); val t = maxOf(top, o.top)
        val r = minOf(right, o.right); val b = minOf(bottom, o.bottom)
        return if (r <= l || b <= t) EMPTY else RectI(l, t, r, b)
    }

    fun union(o: RectI): RectI {
        if (isEmpty) return o
        if (o.isEmpty) return this
        return RectI(minOf(left, o.left), minOf(top, o.top), maxOf(right, o.right), maxOf(bottom, o.bottom))
    }

    fun contains(x: Int, y: Int): Boolean = x in left until right && y in top until bottom

    fun contains(o: RectI): Boolean =
        o.isEmpty || (o.left >= left && o.top >= top && o.right <= right && o.bottom <= bottom)

    fun inset(dx: Int, dy: Int = dx): RectI {
        val l = left + dx; val t = top + dy; val r = right - dx; val b = bottom - dy
        return if (r <= l || b <= t) EMPTY else RectI(l, t, r, b)
    }

    fun expand(d: Int): RectI = RectI(left - d, top - d, right + d, bottom + d)

    companion object {
        val EMPTY = RectI(0, 0, 0, 0)
        fun of(l: Int, t: Int, r: Int, b: Int) = RectI(l, t, r, b)
        fun wh(w: Int, h: Int) = RectI(0, 0, w, h)
        fun around(x: Float, y: Float, r: Float): RectI =
            RectI(
                kotlin.math.floor(x - r).toInt(), kotlin.math.floor(y - r).toInt(),
                kotlin.math.ceil(x + r).toInt() + 1, kotlin.math.ceil(y + r).toInt() + 1
            )
    }
}

/**
 * A tightly packed premultiplied ARGB_8888 bitmap. This is the engine's plain
 * memory image type: it does not know about Android, tiling, or GPU textures.
 */
class Pixmap(val width: Int, val height: Int, val pixels: IntArray = IntArray(width * height)) {
    init {
        require(width > 0 && height > 0) { "Pixmap must be non-empty" }
        require(pixels.size >= width * height) { "Pixel buffer too small" }
    }

    val rect: RectI get() = RectI.wh(width, height)

    fun index(x: Int, y: Int): Int = y * width + x

    fun get(x: Int, y: Int): Int =
        if (x in 0 until width && y in 0 until height) pixels[y * width + x] else 0

    fun set(x: Int, y: Int, c: Int) {
        if (x in 0 until width && y in 0 until height) pixels[y * width + x] = c
    }

    fun fill(color: Int): Pixmap {
        java.util.Arrays.fill(pixels, 0, width * height, color)
        return this
    }

    fun clear(): Pixmap = fill(0)

    fun copy(): Pixmap = Pixmap(width, height, pixels.copyOf())

    /** Copies a rectangle from [src] (same coordinate space) into this pixmap. */
    fun blit(src: Pixmap, dstLeft: Int = 0, dstTop: Int = 0) {
        val r = RectI(dstLeft, dstTop, dstLeft + src.width, dstTop + src.height).intersect(rect)
        if (r.isEmpty) return
        for (y in r.top until r.bottom) {
            val srcRow = (y - dstTop) * src.width + (r.left - dstLeft)
            System.arraycopy(src.pixels, srcRow, pixels, y * width + r.left, r.width)
        }
    }

    fun copyRect(r: RectI): Pixmap {
        val rr = r.intersect(rect)
        if (rr.isEmpty) return Pixmap(1, 1)
        val out = Pixmap(rr.width, rr.height)
        for (y in 0 until rr.height) {
            System.arraycopy(pixels, (rr.top + y) * width + rr.left, out.pixels, y * rr.width, rr.width)
        }
        return out
    }

    /** Bilinear sample at pixel-center coordinates; returns premultiplied ARGB. */
    fun sampleBilinear(x: Float, y: Float): Int {
        val fx = x - 0.5f
        val fy = y - 0.5f
        val x0 = kotlin.math.floor(fx).toInt()
        val y0 = kotlin.math.floor(fy).toInt()
        val tx = fx - x0
        val ty = fy - y0
        val c00 = get(x0, y0); val c10 = get(x0 + 1, y0)
        val c01 = get(x0, y0 + 1); val c11 = get(x0 + 1, y0 + 1)
        return lerp4(c00, c10, c01, c11, tx, ty)
    }

    private fun lerp4(c00: Int, c10: Int, c01: Int, c11: Int, tx: Float, ty: Float): Int {
        val a = lerp(lerp(alphaOf(c00), alphaOf(c10), tx), lerp(alphaOf(c01), alphaOf(c11), tx), ty)
        val r = lerp(lerp(redOf(c00), redOf(c10), tx), lerp(redOf(c01), redOf(c11), tx), ty)
        val g = lerp(lerp(greenOf(c00), greenOf(c10), tx), lerp(greenOf(c01), greenOf(c11), tx), ty)
        val b = lerp(lerp(blueOf(c00), blueOf(c10), tx), lerp(blueOf(c01), blueOf(c11), tx), ty)
        val ai = (a + 0.5f).toInt().coerceIn(0, 255)
        return (ai shl 24) or
            ((r + 0.5f).toInt().coerceIn(0, ai) shl 16) or
            ((g + 0.5f).toInt().coerceIn(0, ai) shl 8) or
            ((b + 0.5f).toInt().coerceIn(0, ai))
    }

    private fun lerp(a: Int, b: Int, t: Float): Float = a.toFloat() + (b - a) * t

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    /** Box-less bilinear resize (adequate for previews and thumbnails). */
    fun scaledTo(newW: Int, newH: Int): Pixmap {
        val w = newW.coerceAtLeast(1)
        val h = newH.coerceAtLeast(1)
        val out = Pixmap(w, h)
        val sx = width.toFloat() / w
        val sy = height.toFloat() / h
        for (y in 0 until h) {
            val fy = (y + 0.5f) * sy
            for (x in 0 until w) {
                out.pixels[y * w + x] = sampleBilinear((x + 0.5f) * sx, fy)
            }
        }
        return out
    }

    /** Aspect-preserving thumbnail that fits within [maxDim]. */
    fun thumbnail(maxDim: Int): Pixmap {
        val scale = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height, 1f)
        if (scale >= 1f) return copy()
        return scaledTo((width * scale).toInt().coerceAtLeast(1), (height * scale).toInt().coerceAtLeast(1))
    }

    /** Returns straight (non-premultiplied) ARGB pixels, e.g. for Android Bitmap interop. */
    fun straightPixels(): IntArray = IntArray(width * height) { premulToStraight(pixels[it]) }

    companion object {
        fun filled(width: Int, height: Int, color: Int): Pixmap = Pixmap(width, height).fill(color)

        /** Builds a premultiplied pixmap from straight-alpha ARGB pixels. */
        fun fromStraight(width: Int, height: Int, straight: IntArray): Pixmap {
            val p = Pixmap(width, height)
            for (i in straight.indices.take(width * height)) p.pixels[i] = straightToPremul(straight[i])
            return p
        }
    }
}
