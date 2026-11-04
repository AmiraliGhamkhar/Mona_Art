package com.monaart.engine.paint

/** Color constants and helpers shared by the whole engine. */
object ColorInts {
    const val TRANSPARENT: Int = 0x00000000
    const val BLACK: Int = 0xFF000000.toInt()
    const val WHITE: Int = 0xFFFFFFFF.toInt()
}

/**
 * Converts normalized [0,1] color components into a premultiplied ARGB int.
 * Input is straight (non-premultiplied) alpha, which is what brush colors use.
 */
fun packPremul(r: Float, g: Float, b: Float, a: Float): Int {
    val aa = (a.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    if (aa == 0) return 0
    val rr = (r.coerceIn(0f, 1f) * a * 255f + 0.5f).toInt().coerceAtMost(aa)
    val gg = (g.coerceIn(0f, 1f) * a * 255f + 0.5f).toInt().coerceAtMost(aa)
    val bb = (b.coerceIn(0f, 1f) * a * 255f + 0.5f).toInt().coerceAtMost(aa)
    return (aa shl 24) or (rr shl 16) or (gg shl 8) or bb
}

/** Straight-alpha ARGB int (as used by Android's [android.graphics.Color]). */
fun packStraight(r: Float, g: Float, b: Float, a: Float): Int {
    val aa = (a.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    val rr = (r.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    val gg = (g.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    val bb = (b.coerceIn(0f, 1f) * 255f + 0.5f).toInt()
    return (aa shl 24) or (rr shl 16) or (gg shl 8) or bb
}

fun alphaOf(c: Int): Int = c ushr 24
fun redOf(c: Int): Int = (c shr 16) and 0xFF
fun greenOf(c: Int): Int = (c shr 8) and 0xFF
fun blueOf(c: Int): Int = c and 0xFF

fun withAlpha(c: Int, alpha: Int): Int = (alpha.coerceIn(0, 255) shl 24) or (c and 0x00FFFFFF)

/** Converts a premultiplied ARGB int into straight-alpha ARGB (needed for export/display round-trips). */
fun premulToStraight(c: Int): Int {
    val a = alphaOf(c)
    if (a == 0) return 0
    if (a == 255) return c
    val r = ((redOf(c) * 255 + a / 2) / a).coerceAtMost(255)
    val g = ((greenOf(c) * 255 + a / 2) / a).coerceAtMost(255)
    val b = ((blueOf(c) * 255 + a / 2) / a).coerceAtMost(255)
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

/** Converts a straight-alpha ARGB int into premultiplied ARGB. */
fun straightToPremul(c: Int): Int {
    val a = alphaOf(c)
    if (a == 0) return 0
    if (a == 255) return c
    val r = (redOf(c) * a + 127) / 255
    val g = (greenOf(c) * a + 127) / 255
    val b = (blueOf(c) * a + 127) / 255
    return (a shl 24) or (r shl 16) or (g shl 8) or b
}

/**
 * A coarse "is this packed rect fully inside the target" guard used by the
 * low-level raster routines. Kept here so call sites do not re-derive it.
 */
internal inline fun clampInt(v: Int, lo: Int, hi: Int): Int = if (v < lo) lo else if (v > hi) hi else v
