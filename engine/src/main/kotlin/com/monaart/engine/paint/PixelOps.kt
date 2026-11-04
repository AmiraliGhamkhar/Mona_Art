package com.monaart.engine.paint

/**
 * Integer fast path for layer compositing, matching [BlendMath] to within 1/255.
 *
 * All pixel buffers in the engine are premultiplied ARGB_8888 ints. Compositing
 * "src over dst" with a blend mode uses the W3C compositing formula
 *   co = as*(1-ab)*Cs + as*ab*B(Cb,Cs) + (1-as)*ab*Cb
 * which is already expressed in premultiplied space, so no normalization is
 * needed except for the component (non-separable) modes.
 */
object PixelOps {

    // 8-bit -> 0..1 lookup for blend formulas
    private val UNIT = FloatArray(256) { it / 255f }

    /**
     * Composites straight-alpha color [src] (packed ARGB, alpha in high byte) with
     * coverage [coverage] (0..1) onto [dst] premultiplied pixel, honoring [mode]
     * and layer opacity. Returns premultiplied ARGB.
     */
    fun composite(mode: BlendMode, dst: Int, src: Int, coverage: Float, opacity: Float): Int {
        if (coverage <= 0f || opacity <= 0f) return dst
        val sa8 = alphaOf(src)
        if (sa8 == 0) return dst
        val aS = (sa8 / 255f) * coverage * opacity
        if (aS <= 0f) return dst
        val ab = alphaOf(dst) / 255f

        val sr = redOf(src) / 255f
        val sg = greenOf(src) / 255f
        val sb = blueOf(src) / 255f

        val ao = aS + ab * (1f - aS)
        if (ao <= 0f) return 0

        val cr: Float
        val cg: Float
        val cbOut: Float
        if (mode == BlendMode.Normal) {
            // Fast path: co = as*Cs + (1-as)*Cb   (premultiplied)
            val inv = 1f - aS
            cr = sr * aS + (redOf(dst) / 255f) * inv
            cg = sg * aS + (greenOf(dst) / 255f) * inv
            cbOut = sb * aS + (blueOf(dst) / 255f) * inv
        } else if (mode.group == BlendGroup.COMPONENT) {
            // Non-separable: needs straight backdrop and source colors.
            val bR = if (ab > 0f) redOf(dst) / 255f / ab else 0f
            val bG = if (ab > 0f) greenOf(dst) / 255f / ab else 0f
            val bB = if (ab > 0f) blueOf(dst) / 255f / ab else 0f
            val back = floatArrayOf(bR, bG, bB)
            val srcC = floatArrayOf(sr, sg, sb)
            val blended = FloatArray(3)
            BlendMath.blendRgb(mode, back, srcC, blended)
            // co = as*(1-ab)*Cs + as*ab*B + (1-as)*ab*Cb
            val k1 = aS * (1f - ab)
            val k2 = aS * ab
            val k3 = (1f - aS) * ab
            cr = k1 * sr + k2 * blended[0] + k3 * bR
            cg = k1 * sg + k2 * blended[1] + k3 * bG
            cbOut = k1 * sb + k2 * blended[2] + k3 * bB
        } else {
            val bR = redOf(dst) / 255f
            val bG = greenOf(dst) / 255f
            val bB = blueOf(dst) / 255f
            // Work in straight space for the blend function, then premultiply back.
            val bsR = if (ab > 0f) bR / ab else 0f
            val bsG = if (ab > 0f) bG / ab else 0f
            val bsB = if (ab > 0f) bB / ab else 0f
            val b0 = BlendMath.blendChannel(mode, bsR, sr)
            val b1 = BlendMath.blendChannel(mode, bsG, sg)
            val b2 = BlendMath.blendChannel(mode, bsB, sb)
            val k1 = aS * (1f - ab)
            val k2 = aS * ab
            val k3 = (1f - aS) * ab
            cr = k1 * sr + k2 * b0 + k3 * bsR
            cg = k1 * sg + k2 * b1 + k3 * bsG
            cbOut = k1 * sb + k2 * b2 + k3 * bsB
        }

        // cr/cg/cb are already premultiplied. Clamp + pack.
        val aOut = (ao * 255f + 0.5f).toInt().coerceIn(0, 255)
        val rOut = (cr * 255f + 0.5f).toInt().coerceIn(0, aOut)
        val gOut = (cg * 255f + 0.5f).toInt().coerceIn(0, aOut)
        val bOut = (cbOut * 255f + 0.5f).toInt().coerceIn(0, aOut)
        return (aOut shl 24) or (rOut shl 16) or (gOut shl 8) or bOut
    }

    /** Source-over of a premultiplied [src] pixel (already scaled by coverage/opacity is NOT applied). */
    fun over(dst: Int, src: Int): Int {
        val sa = alphaOf(src)
        if (sa == 255) return src
        if (sa == 0) return dst
        val inv = 255 - sa
        val da = alphaOf(dst)
        val a = sa + (da * inv + 127) / 255
        val r = redOf(src) + (redOf(dst) * inv + 127) / 255
        val g = greenOf(src) + (greenOf(dst) * inv + 127) / 255
        val b = blueOf(src) + (blueOf(dst) * inv + 127) / 255
        return (a shl 24) or (r.coerceAtMost(a) shl 16) or (g.coerceAtMost(a) shl 8) or b.coerceAtMost(a)
    }

    /** Composite the [src] pixel over [dst] with a constant coverage multiplier and Normal mode. */
    fun overWithCoverage(dst: Int, src: Int, coverage: Float): Int {
        if (coverage >= 0.999f) return over(dst, src)
        val sa = alphaOf(src)
        if (sa == 0) return dst
        val scaled = scaleAlpha(src, coverage)
        return over(dst, scaled)
    }

    /** Multiplies the alpha of a premultiplied pixel by [f] (0..1). */
    fun scaleAlpha(premul: Int, f: Float): Int {
        if (f >= 0.999f) return premul
        if (f <= 0f) return 0
        val a = (alphaOf(premul) * f + 0.5f).toInt().coerceIn(0, 255)
        if (a == 0) return 0
        val r = (redOf(premul) * f + 0.5f).toInt().coerceAtMost(a)
        val g = (greenOf(premul) * f + 0.5f).toInt().coerceAtMost(a)
        val b = (blueOf(premul) * f + 0.5f).toInt().coerceAtMost(a)
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun unit(v: Int): Float = UNIT[v and 0xFF]
}
