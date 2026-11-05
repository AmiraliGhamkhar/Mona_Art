package com.monaart.engine.paint

/**
 * Layer blend modes. All math is defined for straight (non-premultiplied) color
 * channels in 0..1 space, which is the reference definition used by
 * Porter-Duff / PDF / Photoshop. Fast integer versions live in [BlendMath].
 */
enum class BlendMode(val label: String, val group: BlendGroup) {
    Normal("Normal", BlendGroup.NORMAL),
    Multiply("Multiply", BlendGroup.DARKEN),
    Darken("Darken", BlendGroup.DARKEN),
    ColorBurn("Color Burn", BlendGroup.DARKEN),
    Lighten("Lighten", BlendGroup.LIGHTEN),
    Screen("Screen", BlendGroup.LIGHTEN),
    ColorDodge("Color Dodge", BlendGroup.LIGHTEN),
    Add("Add", BlendGroup.LIGHTEN),
    Overlay("Overlay", BlendGroup.CONTRAST),
    SoftLight("Soft Light", BlendGroup.CONTRAST),
    HardLight("Hard Light", BlendGroup.CONTRAST),
    Difference("Difference", BlendGroup.INVERSION),
    Exclusion("Exclusion", BlendGroup.INVERSION),
    Hue("Hue", BlendGroup.COMPONENT),
    Saturation("Saturation", BlendGroup.COMPONENT),
    Color("Color", BlendGroup.COMPONENT),
    Luminosity("Luminosity", BlendGroup.COMPONENT),
}

enum class BlendGroup { NORMAL, DARKEN, LIGHTEN, CONTRAST, INVERSION, COMPONENT }

/**
 * Reference (float) implementation of every blend formula. Used by tests as the
 * source of truth, and by the integer fast path as its documented definition.
 */
object BlendMath {
    /** Blend function B(cb, cs) for one channel, cb = backdrop, cs = source, both 0..1. */
    fun blendChannel(mode: BlendMode, cb: Float, cs: Float): Float = when (mode) {
        BlendMode.Normal -> cs
        BlendMode.Multiply -> cb * cs
        BlendMode.Darken -> minOf(cb, cs)
        BlendMode.Lighten -> maxOf(cb, cs)
        BlendMode.Screen -> cb + cs - cb * cs
        BlendMode.Add -> (cb + cs).coerceAtMost(1f)
        BlendMode.ColorDodge -> if (cb <= 0f) {
            0f
        } else if (cs >= 1f) {
            1f
        } else {
            minOf(1f, cb / (1f - cs))
        }
        BlendMode.ColorBurn -> if (cb >= 1f) {
            1f
        } else if (cs <= 0f) {
            0f
        } else {
            1f - minOf(1f, (1f - cb) / cs)
        }
        BlendMode.Overlay -> hardLight(cs, cb)
        BlendMode.HardLight -> hardLight(cb, cs)
        BlendMode.SoftLight -> softLight(cb, cs)
        BlendMode.Difference -> kotlin.math.abs(cb - cs)
        BlendMode.Exclusion -> cb + cs - 2f * cb * cs
        BlendMode.Hue, BlendMode.Saturation, BlendMode.Color, BlendMode.Luminosity ->
            // Component modes operate on the whole color triplet; single-channel
            // fallback is only reached by callers that pass a single channel.
            cs
    }

    private fun hardLight(cb: Float, cs: Float): Float =
        if (cs <= 0.5f) {
            cb * (2f * cs)
        } else {
            val d = 2f * cs - 1f
            cb + d - cb * d
        }

    private fun softLight(cb: Float, cs: Float): Float {
        if (cs <= 0.5f) return cb - (1f - 2f * cs) * cb * (1f - cb)
        val d = if (cb <= 0.25f) ((16f * cb - 12f) * cb + 4f) * cb else kotlin.math.sqrt(cb)
        return cb + (2f * cs - 1f) * (d - cb)
    }

    /** Separable blend of an RGB triplet. */
    fun blendRgb(mode: BlendMode, backdrop: FloatArray, source: FloatArray, out: FloatArray) {
        when (mode) {
            BlendMode.Hue, BlendMode.Saturation, BlendMode.Color, BlendMode.Luminosity ->
                blendComponentMode(mode, backdrop, source, out)
            else -> {
                out[0] = blendChannel(mode, backdrop[0], source[0])
                out[1] = blendChannel(mode, backdrop[1], source[1])
                out[2] = blendChannel(mode, backdrop[2], source[2])
            }
        }
    }

    private fun lum(r: Float, g: Float, b: Float): Float = 0.3f * r + 0.59f * g + 0.11f * b

    private fun sat(r: Float, g: Float, b: Float): Float =
        maxOf(r, g, b) - minOf(r, g, b)

    private fun clipColor(c: FloatArray) {
        val l = lum(c[0], c[1], c[2])
        val n = minOf(c[0], c[1], c[2])
        val x = maxOf(c[0], c[1], c[2])
        if (n < 0f) {
            val d = l - n
            if (d > 1e-6f) {
                c[0] = l + (c[0] - l) * l / d
                c[1] = l + (c[1] - l) * l / d
                c[2] = l + (c[2] - l) * l / d
            }
        }
        if (x > 1f) {
            val d = x - l
            if (d > 1e-6f) {
                c[0] = l + (c[0] - l) * (1f - l) / d
                c[1] = l + (c[1] - l) * (1f - l) / d
                c[2] = l + (c[2] - l) * (1f - l) / d
            }
        }
    }

    private fun setLum(src: FloatArray, l: Float, out: FloatArray) {
        val d = l - lum(src[0], src[1], src[2])
        out[0] = src[0] + d
        out[1] = src[1] + d
        out[2] = src[2] + d
        clipColor(out)
    }

    private fun setSat(src: FloatArray, s: Float, out: FloatArray) {
        // Standard "set saturation" per PDF spec: replace the saturation of src.
        val r = src[0]
        val g = src[1]
        val b = src[2]
        val mn = minOf(r, g, b)
        val mx = maxOf(r, g, b)
        val range = mx - mn
        if (range <= 1e-6f) {
            out[0] = 0f
            out[1] = 0f
            out[2] = 0f
            return
        }
        val scale = s / range
        out[0] = (r - mn) * scale
        out[1] = (g - mn) * scale
        out[2] = (b - mn) * scale
    }

    private fun blendComponentMode(mode: BlendMode, cb: FloatArray, cs: FloatArray, out: FloatArray) {
        val tmp = FloatArray(3)
        when (mode) {
            // Hue = SetLum(SetSat(Cs, Sat(Cb)), Lum(Cb))
            BlendMode.Hue -> {
                setSat(cs, sat(cb[0], cb[1], cb[2]), tmp)
                setLum(tmp, lum(cb[0], cb[1], cb[2]), out)
            }
            // Saturation = SetLum(SetSat(Cb, Sat(Cs)), Lum(Cb))
            BlendMode.Saturation -> {
                setSat(cb, sat(cs[0], cs[1], cs[2]), tmp)
                setLum(tmp, lum(cb[0], cb[1], cb[2]), out)
            }
            BlendMode.Color -> setLum(cs, lum(cb[0], cb[1], cb[2]), out)
            BlendMode.Luminosity -> setLum(cb, lum(cs[0], cs[1], cs[2]), out)
            else -> {
                out[0] = cs[0]
                out[1] = cs[1]
                out[2] = cs[2]
            }
        }
    }
}
