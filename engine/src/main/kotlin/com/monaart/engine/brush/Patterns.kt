package com.monaart.engine.brush

import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf

/**
 * Procedural grain/noise patterns generated deterministically from a seed.
 * Keeping them procedural means textures need no binary assets in the APK and
 * are identical across devices, which makes rendering tests reproducible.
 */
object Patterns {

    /** Names of the built-in grain patterns. */
    val GRAIN_NAMES = listOf(
        "grain.paper", "grain.canvas", "grain.charcoal", "grain.pencil",
        "grain.sponge", "grain.chalk", "grain.rough", "grain.fine",
    )

    private val cache = HashMap<String, ByteArray>()

    const val TILE = 256

    /** Returns an 8-bit coverage tile for the named grain (values 0..255). */
    fun grain(name: String): ByteArray = cache.getOrPut(name) {
        when (name) {
            "grain.paper" -> fbm(1, 24f, 0.65f)
            "grain.canvas" -> canvasWeave(2)
            "grain.charcoal" -> fbm(3, 46f, 0.8f)
            "grain.pencil" -> fbm(4, 70f, 0.55f)
            "grain.sponge" -> cellular(5, 18)
            "grain.chalk" -> fbm(6, 30f, 0.9f)
            "grain.rough" -> fbm(7, 12f, 0.75f)
            else -> fbm(8, 128f, 0.4f) // fine
        }
    }

    /** Point/speck pattern used by particle brushes. */
    fun speck(seed: Int, size: Int = 64): ByteArray {
        val key = "speck.$seed.$size"
        return cache.getOrPut(key) {
            val out = ByteArray(size * size)
            val rnd = kotlin.random.Random(seed)
            for (i in out.indices) out[i] = if (rnd.nextFloat() < 0.25f) (rnd.nextInt(80, 256)).toByte() else 0
            out
        }
    }

    private fun fbm(seed: Int, frequency: Float, contrast: Float): ByteArray {
        val size = TILE
        val out = ByteArray(size * size)
        var amp = 1f
        var freq = frequency
        var totalAmp = 0f
        val acc = FloatArray(size * size)
        repeat(4) {
            addOctave(acc, size, freq, Math.min(0x7FFFFFFF, (seed * 7919 + it * 104729)), amp)
            totalAmp += amp
            amp *= 0.5f
            freq *= 2f
        }
        for (i in acc.indices) {
            var v = acc[i] / totalAmp
            // Contrast: push toward 0/1 around the midpoint.
            v = (v - 0.5f) * contrast * 2f + 0.5f
            v = v.coerceIn(0f, 1f)
            out[i] = (v * 255f).toInt().toByte()
        }
        return out
    }

    private fun addOctave(acc: FloatArray, size: Int, freq: Float, seed: Int, amp: Float) {
        val cell = (size / freq).coerceAtLeast(1.001f)
        val rnd = kotlin.random.Random(seed)
        val grid = (kotlin.math.ceil(size / cell).toInt() + 2)
        val pts = FloatArray(grid * grid) { rnd.nextFloat() }
        for (y in 0 until size) {
            val gy = y / cell
            val y0 = gy.toInt()
            val ty = smooth(gy - y0)
            for (x in 0 until size) {
                val gx = x / cell
                val x0 = gx.toInt()
                val tx = smooth(gx - x0)
                val a = pts[y0 * grid + x0]
                val b = pts[y0 * grid + x0 + 1]
                val c = pts[(y0 + 1) * grid + x0]
                val d = pts[(y0 + 1) * grid + x0 + 1]
                val top = a + (b - a) * tx
                val bottom = c + (d - c) * tx
                acc[y * size + x] += (top + (bottom - top) * ty) * amp
            }
        }
    }

    private fun smooth(t: Float): Float = t * t * (3f - 2f * t)

    private fun canvasWeave(seed: Int): ByteArray {
        val size = TILE
        val out = ByteArray(size * size)
        val rnd = kotlin.random.Random(seed)
        val noise = FloatArray(size * size) { rnd.nextFloat() }
        for (y in 0 until size) {
            for (x in 0 until size) {
                val warp = 0.5f + 0.5f * kotlin.math.sin(x * 0.9f)
                val weft = 0.5f + 0.5f * kotlin.math.sin(y * 0.9f)
                val v = 0.45f + 0.25f * warp * weft + 0.3f * noise[y * size + x]
                out[y * size + x] = (v.coerceIn(0f, 1f) * 255f).toInt().toByte()
            }
        }
        return out
    }

    private fun cellular(seed: Int, cells: Int): ByteArray {
        val size = TILE
        val out = ByteArray(size * size)
        val rnd = kotlin.random.Random(seed)
        val px = FloatArray(cells * cells) { rnd.nextFloat() * size }
        val py = FloatArray(cells * cells) { rnd.nextFloat() * size }
        for (y in 0 until size) {
            for (x in 0 until size) {
                var best = Float.MAX_VALUE
                for (i in 0 until cells * cells) {
                    val dx = px[i] - x
                    val dy = py[i] - y
                    val d = dx * dx + dy * dy
                    if (d < best) best = d
                }
                val v = (kotlin.math.sqrt(best) / (size.toFloat() / cells)).coerceIn(0f, 1f)
                out[y * size + x] = ((1f - v) * 255f).toInt().toByte()
            }
        }
        return out
    }

    /** Samples the coverage tile at integer coordinates with wrap-around. */
    inline fun sample(tile: ByteArray, x: Int, y: Int): Int {
        val size = TILE
        val xx = ((x % size) + size) % size
        val yy = ((y % size) + size) % size
        return tile[yy * size + xx].toInt() and 0xFF
    }
}

/** Minimal HSV<->RGB helpers used for brush color dynamics. */
object Hsv {
    /** color is straight ARGB. */
    fun shift(color: Int, hueDegrees: Float, satScale: Float, valueScale: Float): Int {
        val a = alphaOf(color)
        if (a == 0) return color
        val (h, s, v) = toHsv(color)
        val nh = ((h + hueDegrees) % 360f + 360f) % 360f
        val ns = (s * satScale).coerceIn(0f, 1f)
        val nv = (v * valueScale).coerceIn(0f, 1f)
        return fromHsv(a, nh, ns, nv)
    }

    fun toHsv(color: Int): Triple<Float, Float, Float> {
        val r = redOf(color) / 255f
        val g = greenOf(color) / 255f
        val b = blueOf(color) / 255f
        val mx = maxOf(r, g, b)
        val mn = minOf(r, g, b)
        val d = mx - mn
        var h = 0f
        if (d > 1e-6f) {
            h = when (mx) {
                r -> 60f * (((g - b) / d) % 6f)
                g -> 60f * (((b - r) / d) + 2f)
                else -> 60f * (((r - g) / d) + 4f)
            }
            if (h < 0f) h += 360f
        }
        val s = if (mx <= 1e-6f) 0f else d / mx
        return Triple(h, s, mx)
    }

    fun fromHsv(a: Int, h: Float, s: Float, v: Float): Int {
        val c = v * s
        val hp = (h % 360f) / 60f
        val x = c * (1f - kotlin.math.abs(hp % 2f - 1f))
        val (r1, g1, b1) = when (hp.toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val m = v - c
        val r = ((r1 + m) * 255f).toInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255f).toInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255f).toInt().coerceIn(0, 255)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
}
