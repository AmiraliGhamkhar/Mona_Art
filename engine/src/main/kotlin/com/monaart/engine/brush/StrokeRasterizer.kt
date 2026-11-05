package com.monaart.engine.brush

import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf
import com.monaart.engine.paint.straightToPremul

/**
 * Rasterizes a brush stroke into a straight-ARGB scratch buffer covering the
 * stroke's bounding box (plus margin). The buffer is composited into the layer
 * once per flush, which gives correct, uniform stroke opacity independent of
 * how many dabs the stroke contains.
 */
class StrokeRasterizer(private val spec: BrushSpec, pad: Int) {

    private var bx = 0
    private var by = 0
    private var bw = 0
    private var bh = 0
    /** Straight-alpha premultiplied bytes stored as ints 0xAARRGGBB with premultiplied channels. */
    var buf: IntArray = IntArray(0)
        private set

    private val padPx = pad.coerceAtLeast(1) + 2

    val originX: Int get() = bx
    val originY: Int get() = by
    val width: Int get() = bw
    val height: Int get() = bh

    /** Ensures the scratch buffer covers the requested rect, translating existing content. */
    fun ensure(x0: Int, y0: Int, x1: Int, y1: Int) {
        if (bw == 0) {
            bx = x0; by = y0; bw = (x1 - x0).coerceAtLeast(1); bh = (y1 - y0).coerceAtLeast(1)
            buf = IntArray(bw * bh)
            return
        }
        if (x0 >= bx && y0 >= by && x1 <= bx + bw && y1 <= by + bh) return
        val nbx = minOf(bx, x0)
        val nby = minOf(by, y0)
        val nbx1 = maxOf(bx + bw, x1)
        val nby1 = maxOf(by + bh, y1)
        val nw = nbx1 - nbx
        val nh = nby1 - nby
        val nb = IntArray(nw * nh)
        for (y in 0 until bh) {
            System.arraycopy(buf, y * bw, nb, (y + by - nby) * nw + (bx - nbx), bw)
        }
        buf = nb
        bx = nbx; by = nby; bw = nw; bh = nh
    }

    /** Pre-allocates the buffer for a stroke bounding box. */
    fun reserve(x0: Float, y0: Float, x1: Float, y1: Float) {
        ensure(
            kotlin.math.floor(x0).toInt() - padPx,
            kotlin.math.floor(y0).toInt() - padPx,
            kotlin.math.ceil(x1).toInt() + padPx,
            kotlin.math.ceil(y1).toInt() + padPx,
        )
    }

    /**
     * Draws one dab. [color] is straight ARGB; [flow] is the effective per-dab
     * alpha multiplier; [seed] makes scatter/jitter deterministic per dab.
     */
    fun dab(d: StrokeProcessor.Dab, color: Int, flow: Float, seed: Int, pickup: ColorPickup?) {
        val r = d.radius + padPx
        if (r <= 0f) return
        val x0 = kotlin.math.floor(d.x - r).toInt()
        val y0 = kotlin.math.floor(d.y - r).toInt()
        val x1 = kotlin.math.ceil(d.x + r).toInt()
        val y1 = kotlin.math.ceil(d.y + r).toInt()
        ensure(x0, y0, x1, y1)
        when (spec.kind) {
            BrushKind.PARTICLE -> drawParticles(d, color, flow, seed)
            else -> drawSingleDab(d, color, flow, pickup)
        }
    }

    private fun drawParticles(d: StrokeProcessor.Dab, color: Int, flow: Float, seed: Int) {
        val rnd = kotlin.random.Random(seed)
        val n = spec.particleCount.coerceIn(1, 64)
        val spread = spec.size * 0.5f
        for (i in 0 until n) {
            val a = rnd.nextFloat() * (Math.PI * 2).toFloat()
            val dist = kotlin.math.sqrt(rnd.nextFloat()) * spread
            val px = d.x + kotlin.math.cos(a) * dist
            val py = d.y + kotlin.math.sin(a) * dist
            val pr = d.radius * (0.08f + rnd.nextFloat() * 0.22f)
            val sub = StrokeProcessor.Dab(px, py, pr, d.pressure, d.direction, d.index, d.speed)
            drawSingleDab(sub, color, flow * 0.85f, null)
        }
    }

    private fun drawSingleDab(d: StrokeProcessor.Dab, color: Int, flow: Float, pickup: ColorPickup?) {
        val radius = d.radius
        if (radius < 0.12f) return
        val angle = (spec.angle + if (spec.rotationJitter > 0f && spec.kind == BrushKind.CALLIGRAPHY) 0f else 0f) *
            (Math.PI.toFloat() / 180f) + if (spec.grainFollowStroke) d.direction else 0f
        val cosA = kotlin.math.cos(angle)
        val sinA = kotlin.math.sin(angle)
        val roundness = spec.roundness.coerceIn(0.03f, 1f)
        val hard = spec.hardness.coerceIn(0f, 1f)
        // Falloff shape: hard core with soft shoulder.
        val core = hard * 0.92f
        val invSpan = if (1f - core < 1e-4f) 1e4f else 1f / (1f - core)
        val grain = spec.texture?.let { Patterns.grain(it) }
        val grainStrength = if (grain != null) spec.grainStrength.coerceIn(0f, 1f) else 0f
        val grainScale = spec.grainScale.coerceAtLeast(0.02f)
        val wet = spec.wetness.coerceIn(0f, 1f)
        val smudge = if (spec.kind == BrushKind.SMUDGE) spec.smudgeRate.coerceIn(0f, 1f) else 0f

        val ca = alphaOf(color)
        if (ca == 0 && smudge <= 0f) return
        val straightR = redOf(color) / 255f
        val straightG = greenOf(color) / 255f
        val straightB = blueOf(color) / 255f
        val colPremulA = ca / 255f

        // Iterate the axis-aligned box of the (possibly squashed) ellipse.
        val rx = radius
        val ry = radius * roundness
        val extent = maxOf(rx, ry)
        val x0 = kotlin.math.floor(d.x - extent).toInt()
        val y0 = kotlin.math.floor(d.y - extent).toInt()
        val x1 = kotlin.math.ceil(d.x + extent).toInt()
        val y1 = kotlin.math.ceil(d.y + extent).toInt()

        for (py in y0 until y1) {
            val ly = py + 0.5f - d.y
            for (px in x0 until x1) {
                val lx = px + 0.5f - d.x
                // Rotate into the nib's local frame.
                val ux = lx * cosA + ly * sinA
                val uy = -lx * sinA + ly * cosA
                val nx = ux / rx
                val ny = uy / ry
                val d2 = nx * nx + ny * ny
                if (d2 > 1f) continue
                val dist = kotlin.math.sqrt(d2)
                var cov = if (dist <= core) 1f else 1f - (dist - core) * invSpan
                cov = cov.coerceIn(0f, 1f)
                // Soften the very rim to avoid polygon stair-stepping.
                if (dist > 0.92f) cov *= (1f - dist) / 0.08f * 0.999f + 0.001f
                if (cov <= 0.002f) continue
                if (grain != null && grainStrength > 0f) {
                    val gx = ((px - bx) * grainScale).toInt()
                    val gy = ((py - by) * grainScale).toInt()
                    val g = Patterns.sample(grain, gx, gy) / 255f
                    cov *= (1f - grainStrength) + grainStrength * g
                    if (cov <= 0.002f) continue
                }
                val lx0 = px - bx
                val ly0 = py - by
                if (lx0 < 0 || lx0 >= bw || ly0 < 0 || ly0 >= bh) continue
                val idx = ly0 * bw + lx0
                if (idx < 0 || idx >= buf.size) continue

                var sr = 0f; var sg = 0f; var sb = 0f; var sa = 0f
                if (smudge > 0f && pickup != null) {
                    val p = pickup.sample(px, py)
                    val pa = alphaOf(p) / 255f
                    if (pa > 0f) {
                        sr = redOf(p) / 255f
                        sg = greenOf(p) / 255f
                        sb = blueOf(p) / 255f
                        sa = pa
                    }
                }
                if (sa <= 0f) {
                    sr = straightR; sg = straightG; sb = straightB; sa = colPremulA
                } else if (wet > 0f && colPremulA > 0f) {
                    // Wet media: mix the paint's own color with picked-up color.
                    val mix = wet * (0.35f + 0.65f * cov)
                    sr = sr * mix + straightR * (1f - mix)
                    sg = sg * mix + straightG * (1f - mix)
                    sb = sb * mix + straightB * (1f - mix)
                    sa = sa * mix + colPremulA * (1f - mix)
                }
                val a = cov * flow
                if (a <= 0f) continue
                val dst = buf[idx]
                val da = alphaOf(dst) / 255f
                val outA = a * sa + da * (1f - a * sa)
                if (outA <= 0f) continue
                // Premultiplied source-over in straight-source space.
                val pr = sr * sa * a + (redOf(dst) / 255f) * (1f - a * sa)
                val pg = sg * sa * a + (greenOf(dst) / 255f) * (1f - a * sa)
                val pb = sb * sa * a + (blueOf(dst) / 255f) * (1f - a * sa)
                val oa = (outA * 255f + 0.5f).toInt().coerceIn(0, 255)
                val orb = (pr * 255f + 0.5f).toInt().coerceIn(0, oa)
                val og = (pg * 255f + 0.5f).toInt().coerceIn(0, oa)
                val ob = (pb * 255f + 0.5f).toInt().coerceIn(0, oa)
                buf[idx] = (oa shl 24) or (orb shl 16) or (og shl 8) or ob
                if (smudge > 0f && pickup != null) {
                    pickup.feedback(px, py, (oa shl 24) or (orb shl 16) or (og shl 8) or ob, smudge * cov)
                }
            }
        }
    }

    /** Composites the scratch buffer into [payload] (premultiplied) with Normal blending. */
    fun compositeInto(payload: IntArray, payloadWidth: Int, payloadOriginX: Int, payloadOriginY: Int, opacity: Float, region: com.monaart.engine.paint.RectI? = null) {
        if (bw == 0 || bh == 0) return
        val alphaMul = opacity.coerceIn(0f, 1f)
        val ry0 = if (region == null) 0 else (region.top - by).coerceIn(0, bh)
        val ry1 = if (region == null) bh else (region.bottom - by).coerceIn(0, bh)
        for (y in ry0 until ry1) {
            val py = by + y - payloadOriginY
            for (x in 0 until bw) {
                if (region != null && (bx + x) < region.left || region != null && (bx + x) >= region.right) continue
                val src = buf[y * bw + x]
                val a = alphaOf(src)
                if (a == 0) continue
                val pxx = bx + x - payloadOriginX
                if (pxx < 0 || pxx >= payloadWidth) continue
                val idx = py * payloadWidth + pxx
                if (idx < 0 || idx >= payload.size) continue
                val dst = payload[idx]
                val da = alphaOf(dst) / 255f
                val sa = (a / 255f) * alphaMul
                val outA = sa + da * (1f - sa)
                if (outA <= 0f) continue
                val inv = 1f - sa
                val r = (redOf(src) / 255f) * alphaMul + (redOf(dst) / 255f) * inv
                val g = (greenOf(src) / 255f) * alphaMul + (greenOf(dst) / 255f) * inv
                val b = (blueOf(src) / 255f) * alphaMul + (blueOf(dst) / 255f) * inv
                val oa = (outA * 255f + 0.5f).toInt().coerceIn(0, 255)
                payload[idx] = (oa shl 24) or
                    ((r * 255f + 0.5f).toInt().coerceIn(0, oa) shl 16) or
                    ((g * 255f + 0.5f).toInt().coerceIn(0, oa) shl 8) or
                    ((b * 255f + 0.5f).toInt().coerceIn(0, oa))
            }
        }
    }

    /** Erases the buffer covered by the stroke from a premultiplied pixel buffer. */
    fun eraseFrom(payload: IntArray, payloadWidth: Int, payloadOriginX: Int, payloadOriginY: Int, opacity: Float, region: com.monaart.engine.paint.RectI? = null) {
        if (bw == 0) return
        val mul = opacity.coerceIn(0f, 1f)
        val ry0 = if (region == null) 0 else (region.top - by).coerceIn(0, bh)
        val ry1 = if (region == null) bh else (region.bottom - by).coerceIn(0, bh)
        for (y in ry0 until ry1) {
            val py = by + y - payloadOriginY
            if (py < 0) continue
            for (x in 0 until bw) {
                if (region != null && (bx + x) < region.left || region != null && (bx + x) >= region.right) continue
                val a = alphaOf(buf[y * bw + x])
                if (a == 0) continue
                val pxx = bx + x - payloadOriginX
                if (pxx < 0 || pxx >= payloadWidth) continue
                val idx = py * payloadWidth + pxx
                if (idx < 0 || idx >= payload.size) continue
                val keep = 1f - (a / 255f) * mul
                payload[idx] = com.monaart.engine.paint.PixelOps.scaleAlpha(payload[idx], keep)
            }
        }
    }

    /** Adds colours into a premultiplied payload with the given blend behavior. */
    fun blendInto(payload: IntArray, payloadWidth: Int, payloadOriginX: Int, payloadOriginY: Int, opacity: Float, mode: BlendBehavior) {
        if (mode == BlendBehavior.NORMAL) { compositeInto(payload, payloadWidth, payloadOriginX, payloadOriginY, opacity); return }
        val blendMode = when (mode) {
            BlendBehavior.MULTIPLY -> com.monaart.engine.paint.BlendMode.Multiply
            BlendBehavior.SCREEN -> com.monaart.engine.paint.BlendMode.Screen
            BlendBehavior.ADD -> com.monaart.engine.paint.BlendMode.Add
            BlendBehavior.NORMAL -> com.monaart.engine.paint.BlendMode.Normal
        }
        val tmp = IntArray(payloadWidth)
        for (y in 0 until bh) {
            val py = by + y - payloadOriginY
            if (py < 0) continue
            for (x in 0 until bw) {
                val src = buf[y * bw + x]
                if (alphaOf(src) == 0) continue
                val pxx = bx + x - payloadOriginX
                if (pxx < 0 || pxx >= payloadWidth) continue
                val idx = py * payloadWidth + pxx
                if (idx < 0 || idx >= payload.size) continue
                tmp[0] = com.monaart.engine.paint.premulToStraight(src)
                payload[idx] = com.monaart.engine.paint.PixelOps.composite(
                    blendMode, payload[idx], com.monaart.engine.paint.premulToStraight(src), 1f, opacity
                )
            }
        }
    }

    /** Reads from the target layer for smudge/wet brushes. */
    interface ColorPickup {
        fun sample(x: Int, y: Int): Int
        fun feedback(x: Int, y: Int, color: Int, amount: Float)
    }

    companion object {
        fun premulOf(straight: Int): Int = straightToPremul(straight)
    }
}
