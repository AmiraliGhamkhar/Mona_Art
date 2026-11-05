package com.monaart.engine.filter

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Layer
import com.monaart.engine.document.PixelEdit
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf
import com.monaart.engine.selection.Selection
import com.monaart.engine.brush.Hsv

/** Every adjustment/filter offered by the app, with the parameter it consumes. */
enum class FilterKind(val label: String, val paramLabel: String, val min: Float, val max: Float, val default: Float) {
    BrightnessContrast("Brightness / Contrast", "Amount", -1f, 1f, 0.25f),
    HueSaturation("Hue / Saturation", "Hue shift", -180f, 180f, 30f),
    Saturation("Saturation", "Saturation", -1f, 1f, 0.4f),
    Level("Levels", "Gamma", 0.1f, 3f, 1.4f),
    ColorBalance("Color Balance", "Warmth", -1f, 1f, 0.3f),
    Blur("Blur", "Radius px", 1f, 60f, 6f),
    Sharpen("Sharpen", "Amount", 0f, 2f, 0.8f),
    Noise("Noise", "Amount", 0f, 1f, 0.25f),
    Mosaic("Mosaic", "Cell px", 2f, 128f, 16f),
    GradientMap("Gradient Map", "Blend", 0f, 1f, 0.6f),
    Bloom("Bloom", "Intensity", 0f, 1f, 0.45f),
    Glitch("Glitch", "Intensity", 0f, 1f, 0.4f),
    Halftone("Halftone", "Cell px", 2f, 40f, 8f),
    ChromaticAberration("Chromatic Aberration", "Offset px", 0f, 40f, 6f),
    Invert("Invert", "", 0f, 0f, 0f),
    Grayscale("Grayscale", "", 0f, 0f, 0f),
}

/**
 * Deterministic image adjustments. All filters respect the active selection,
 * preserve alpha silhouettes, and record a single undo step.
 */
object Filters {

    fun apply(
        doc: ArtDocument,
        layer: Layer,
        kind: FilterKind,
        amount: Float,
        selection: Selection? = doc.selection,
        secondaryColor: Int = 0xFF000000.toInt(),
    ): Boolean {
        val surface = layer.surface ?: return false
        val region = selection?.bounds?.intersect(doc.canvasRect) ?: doc.canvasRect
        if (region.isEmpty) return false
        val w = region.width
        val h = region.height
        // Work copy in straight ARGB.
        val src = IntArray(w * h)
        for (y in 0 until h) for (x in 0 until w) {
            val p = surface.getPixel(region.left + x, region.top + y)
            src[y * w + x] = com.monaart.engine.paint.premulToStraight(p)
        }
        val out = src.copyOf()
        when (kind) {
            FilterKind.BrightnessContrast -> brightnessContrast(src, out, amount)
            FilterKind.HueSaturation -> hueSaturation(src, out, amount, 0f)
            FilterKind.Saturation -> hueSaturation(src, out, 0f, amount)
            FilterKind.Level -> levels(src, out, amount)
            FilterKind.ColorBalance -> colorBalance(src, out, amount)
            FilterKind.Blur -> boxBlur(src, out, w, h, amount.toInt().coerceAtLeast(1))
            FilterKind.Sharpen -> sharpen(src, src, out, w, h, amount)
            FilterKind.Noise -> noise(src, out, amount)
            FilterKind.Mosaic -> mosaic(src, out, w, h, amount.toInt().coerceAtLeast(2))
            FilterKind.GradientMap -> gradientMap(src, out, amount, secondaryColor)
            FilterKind.Bloom -> bloom(src, out, w, h, amount)
            FilterKind.Glitch -> glitch(src, out, w, h, amount)
            FilterKind.Halftone -> halftone(src, out, w, h, amount.toInt().coerceAtLeast(2))
            FilterKind.ChromaticAberration -> chromatic(src, out, w, h, amount.toInt().coerceAtLeast(0))
            FilterKind.Invert -> invert(src, out)
            FilterKind.Grayscale -> grayscale(src, out)
        }
        val edit = PixelEdit(doc, kind.label, layer)
        edit.touchRect(region)
        for (y in 0 until h) for (x in 0 until w) {
            val cov = selection?.coverage(region.left + x, region.top + y) ?: 1f
            if (cov <= 0f) continue
            val a = src[y * w + x]
            val b = out[y * w + x]
            val blended = if (cov >= 0.999f) b else blendStraight(a, b, cov)
            surface.setPixel(region.left + x, region.top + y, com.monaart.engine.paint.straightToPremul(blended))
        }
        surface.touch()
        doc.bumpRender()
        return edit.commit()
    }

    private fun blendStraight(a: Int, b: Int, t: Float): Int {
        val aa = alphaOf(a)
        val ba = alphaOf(b)
        fun ch(x: Int, y: Int) = (x + (y - x) * t).toInt().coerceIn(0, 255)
        return (ch(aa, ba) shl 24) or (ch(redOf(a), redOf(b)) shl 16) or
            (ch(greenOf(a), greenOf(b)) shl 8) or ch(blueOf(a), blueOf(b))
    }

    private fun brightnessContrast(src: IntArray, out: IntArray, amt: Float) {
        val bright = amt * 255f * 0.5f
        val contrast = 1f + amt
        for (i in src.indices) {
            val c = src[i]
            fun f(v: Int): Int = (((v - 128f) * contrast + 128f) + bright).toInt().coerceIn(0, 255)
            out[i] = (alphaOf(c) shl 24) or (f(redOf(c)) shl 16) or (f(greenOf(c)) shl 8) or f(blueOf(c))
        }
    }

    private fun hueSaturation(src: IntArray, out: IntArray, hueShift: Float, satDelta: Float) {
        for (i in src.indices) {
            val c = src[i]
            if (alphaOf(c) == 0) { out[i] = c; continue }
            out[i] = Hsv.shift(c, hueShift, 1f + satDelta, 1f)
        }
    }

    private fun levels(src: IntArray, out: IntArray, gamma: Float) {
        val lut = IntArray(256)
        for (v in 0..255) {
            val n = Math.pow(v / 255.0, 1.0 / gamma.toDouble()).toFloat()
            lut[v] = (n * 255f).toInt().coerceIn(0, 255)
        }
        for (i in src.indices) {
            val c = src[i]
            out[i] = (alphaOf(c) shl 24) or (lut[redOf(c)] shl 16) or (lut[greenOf(c)] shl 8) or lut[blueOf(c)]
        }
    }

    private fun colorBalance(src: IntArray, out: IntArray, warmth: Float) {
        val rGain = 1f + warmth * 0.35f
        val bGain = 1f - warmth * 0.35f
        for (i in src.indices) {
            val c = src[i]
            val r = (redOf(c) * rGain).toInt().coerceIn(0, 255)
            val b = (blueOf(c) * bGain).toInt().coerceIn(0, 255)
            out[i] = (alphaOf(c) shl 24) or (r shl 16) or (greenOf(c) shl 8) or b
        }
    }

    private fun invert(src: IntArray, out: IntArray) {
        for (i in src.indices) {
            val c = src[i]
            out[i] = (alphaOf(c) shl 24) or ((255 - redOf(c)) shl 16) or ((255 - greenOf(c)) shl 8) or (255 - blueOf(c))
        }
    }

    private fun grayscale(src: IntArray, out: IntArray) {
        for (i in src.indices) {
            val c = src[i]
            val y = (0.3f * redOf(c) + 0.59f * greenOf(c) + 0.11f * blueOf(c)).toInt().coerceIn(0, 255)
            out[i] = (alphaOf(c) shl 24) or (y shl 16) or (y shl 8) or y
        }
    }

    private fun boxBlur(src: IntArray, out: IntArray, w: Int, h: Int, radius: Int) {
        val tmp = IntArray(w * h)
        blurPass(src, tmp, w, h, radius, horizontal = true)
        blurPass(tmp, out, w, h, radius, horizontal = false)
    }

    private fun blurPass(src: IntArray, dst: IntArray, w: Int, h: Int, r: Int, horizontal: Boolean) {
        val len = if (horizontal) w else h
        val lines = if (horizontal) h else w
        val window = 2 * r + 1
        for (line in 0 until lines) {
            var sa = 0; var sr = 0; var sg = 0; var sb = 0
            fun idxOf(k: Int): Int {
                val c = k.coerceIn(0, len - 1)
                return if (horizontal) line * w + c else c * w + line
            }
            for (k in -r..r) {
                val p = src[idxOf(k)]
                sa += alphaOf(p); sr += redOf(p); sg += greenOf(p); sb += blueOf(p)
            }
            for (i in 0 until len) {
                val o = idxOf(i)
                dst[o] = ((sa / window) shl 24) or ((sr / window) shl 16) or ((sg / window) shl 8) or (sb / window)
                val add = src[idxOf(i + r + 1)]
                val rem = src[idxOf(i - r)]
                sa += alphaOf(add) - alphaOf(rem)
                sr += redOf(add) - redOf(rem)
                sg += greenOf(add) - greenOf(rem)
                sb += blueOf(add) - blueOf(rem)
            }
        }
    }

    private fun sharpen(src: IntArray, blurredSrc: IntArray, out: IntArray, w: Int, h: Int, amount: Float) {
        val blur = IntArray(w * h)
        boxBlur(src, blur, w, h, 1)
        for (i in src.indices) {
            val c = src[i]
            val b = blur[i]
            fun f(v: Int, bv: Int) = (v + (v - bv) * amount).toInt().coerceIn(0, 255)
            out[i] = (alphaOf(c) shl 24) or
                (f(redOf(c), redOf(b)) shl 16) or
                (f(greenOf(c), greenOf(b)) shl 8) or
                f(blueOf(c), blueOf(b))
        }
    }

    private fun noise(src: IntArray, out: IntArray, amount: Float) {
        val rnd = kotlin.random.Random(1234)
        val amp = (amount * 128f).toInt()
        for (i in src.indices) {
            val c = src[i]
            fun jitter(v: Int) = (v + rnd.nextInt(-amp, amp + 1)).coerceIn(0, 255)
            out[i] = (alphaOf(c) shl 24) or (jitter(redOf(c)) shl 16) or (jitter(greenOf(c)) shl 8) or jitter(blueOf(c))
        }
    }

    private fun mosaic(src: IntArray, out: IntArray, w: Int, h: Int, cell: Int) {
        java.util.Arrays.fill(out, 0)
        for (by in 0 until h step cell) {
            for (bx in 0 until w step cell) {
                var sa = 0L; var sr = 0L; var sg = 0L; var sb = 0L; var n = 0L
                for (y in by until minOf(by + cell, h)) for (x in bx until minOf(bx + cell, w)) {
                    val p = src[y * w + x]
                    sa += alphaOf(p); sr += redOf(p); sg += greenOf(p); sb += blueOf(p); n++
                }
                if (n == 0L) continue
                val c = ((sa / n).toInt() shl 24) or ((sr / n).toInt() shl 16) or ((sg / n).toInt() shl 8) or (sb / n).toInt()
                for (y in by until minOf(by + cell, h)) for (x in bx until minOf(bx + cell, w)) out[y * w + x] = c
            }
        }
    }

    private fun gradientMap(src: IntArray, out: IntArray, amount: Float, darkColor: Int) {
        for (i in src.indices) {
            val c = src[i]
            val l = (0.3f * redOf(c) + 0.59f * greenOf(c) + 0.11f * blueOf(c)) / 255f
            val r = (redOf(darkColor) + (255 - redOf(darkColor)) * l).toInt().coerceIn(0, 255)
            val g = (greenOf(darkColor) + (255 - greenOf(darkColor)) * l).toInt().coerceIn(0, 255)
            val b = (blueOf(darkColor) + (255 - blueOf(darkColor)) * l).toInt().coerceIn(0, 255)
            fun mix(a: Int, d: Int) = (a + (d - a) * amount).toInt().coerceIn(0, 255)
            out[i] = (alphaOf(c) shl 24) or (mix(redOf(c), r) shl 16) or (mix(greenOf(c), g) shl 8) or mix(blueOf(c), b)
        }
    }

    private fun bloom(src: IntArray, out: IntArray, w: Int, h: Int, amount: Float) {
        val blur = IntArray(w * h)
        boxBlur(src, blur, w, h, maxOf(2, (minOf(w, h) / 40)))
        for (i in src.indices) {
            val c = src[i]
            val b = blur[i]
            // Screen the blurred highlights back on top.
            fun screen(a: Int, bb: Int): Int = (a + bb - a * bb / 255).coerceIn(0, 255)
            val k = amount
            val r = (redOf(c) + (screen(redOf(c), redOf(b)) - redOf(c)) * k).toInt().coerceIn(0, 255)
            val g = (greenOf(c) + (screen(greenOf(c), greenOf(b)) - greenOf(c)) * k).toInt().coerceIn(0, 255)
            val bl = (blueOf(c) + (screen(blueOf(c), blueOf(b)) - blueOf(c)) * k).toInt().coerceIn(0, 255)
            out[i] = (alphaOf(c) shl 24) or (r shl 16) or (g shl 8) or bl
        }
    }

    private fun glitch(src: IntArray, out: IntArray, w: Int, h: Int, amount: Float) {
        val rnd = kotlin.random.Random(99)
        System.arraycopy(src, 0, out, 0, src.size)
        val bands = (h / 12).coerceAtLeast(2)
        repeat(bands) {
            val y0 = rnd.nextInt(0, h)
            val bh = rnd.nextInt(2, maxOf(3, h / 8))
            val shift = ((rnd.nextFloat() * 2f - 1f) * amount * w * 0.08f).toInt()
            for (y in y0 until minOf(y0 + bh, h)) {
                for (x in 0 until w) {
                    val sx = (x - shift).coerceIn(0, w - 1)
                    out[y * w + x] = src[y * w + sx]
                }
            }
        }
        chromatic(out, out.copyOf(), w, h, (amount * 12).toInt())
    }

    private fun chromatic(src: IntArray, out: IntArray, w: Int, h: Int, offset: Int) {
        if (offset <= 0) { System.arraycopy(src, 0, out, 0, src.size); return }
        System.arraycopy(src, 0, out, 0, src.size)
        for (y in 0 until h) for (x in 0 until w) {
            val c = src[y * w + x]
            val ri = (x - offset).coerceIn(0, w - 1)
            val bi = (x + offset).coerceIn(0, w - 1)
            val r = redOf(src[y * w + ri])
            val b = blueOf(src[y * w + bi])
            out[y * w + x] = (alphaOf(c) shl 24) or (r shl 16) or (greenOf(c) shl 8) or b
        }
    }

    private fun halftone(src: IntArray, out: IntArray, w: Int, h: Int, cell: Int) {
        java.util.Arrays.fill(out, 0)
        val r = cell * 0.5f
        for (by in 0 until h step cell) for (bx in 0 until w step cell) {
            val cx = bx + r
            val cy = by + r
            val p = src[(by.coerceIn(0, h - 1)) * w + bx.coerceIn(0, w - 1)]
            val lum = (0.3f * redOf(p) + 0.59f * greenOf(p) + 0.11f * blueOf(p)) / 255f
            val dotR = r * (1f - lum).coerceIn(0.05f, 1f)
            for (y in by until minOf(by + cell, h)) for (x in bx until minOf(bx + cell, w)) {
                val dx = x + 0.5f - cx
                val dy = y + 0.5f - cy
                if (dx * dx + dy * dy <= dotR * dotR) out[y * w + x] = p
            }
        }
    }
}
