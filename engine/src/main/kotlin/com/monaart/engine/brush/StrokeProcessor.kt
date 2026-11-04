package com.monaart.engine.brush

/** A raw pointer sample in document coordinates. */
data class StrokePoint(
    val x: Float,
    val y: Float,
    /** 0..1 (1 for devices without pressure, i.e. finger or mouse). */
    val pressure: Float = 1f,
    /** Stylus tilt in radians from the screen normal (0 = perpendicular). */
    val tilt: Float = 0f,
    /** Stylus orientation in radians. */
    val orientation: Float = 0f,
    val timestampMillis: Long = 0L,
    /** True when this sample came from an active stylus. */
    val isStylus: Boolean = false,
)

/**
 * Turns raw pointer samples into evenly spaced, smoothed dab positions.
 *
 * Responsibilities:
 *  - exponential smoothing / stabilisation (configurable strength and lag),
 *  - quadratic interpolation between samples so fast strokes stay continuous,
 *  - constant-spacing dab emission controlled by the brush's spacing ratio,
 *  - per-dab pressure derived from the interpolated samples,
 *  - velocity estimation for velocity-sensitive brushes,
 *  - taper calculation over the stroke.
 *
 * This class is pure: it does not know about bitmaps, so it is fully unit-tested
 * on the JVM.
 */
class StrokeProcessor(private val spec: BrushSpec) {

    private var lastRaw: StrokePoint? = null
    private var smoothX = 0f
    private var smoothY = 0f
    private var strokeLength = 0f
    private var carried = 0f
    private val velocitySamples = ArrayDeque<Float>()
    private var prevX = 0f
    private var prevY = 0f
    var finished: Boolean = false
        private set

    /** Total path length so far (used for taper). */
    val length: Float get() = strokeLength

    /** One emitted dab. */
    data class Dab(
        val x: Float,
        val y: Float,
        /** Effective radius in pixels after pressure/tilt/taper. */
        val radius: Float,
        /** Raw pressure 0..1 after curve shaping. */
        val pressure: Float,
        /** Direction of travel in radians (used by calligraphy and grain rotation). */
        val direction: Float,
        /** Index of the dab within the stroke. */
        val index: Int,
        /** Estimated speed in canvas px per ms. */
        val speed: Float,
    )

    private class Quad(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val x2: Float, val y2: Float,
                       val p0: Float, val p1: Float, val p2: Float, val tilt0: Float, val tilt1: Float, val tilt2: Float)

    /**
     * Feeds a raw sample and returns the dabs to render for the path covered
     * since the previous sample.
     */
    fun accept(pt: StrokePoint): List<Dab> {
        val out = ArrayList<Dab>(8)
        val prev = lastRaw
        lastRaw = pt
        if (prev == null) {
            smoothX = pt.x
            smoothY = pt.y
            prevX = pt.x
            prevY = pt.y
            // First dab is emitted immediately for responsiveness.
            emitDab(pt.x, pt.y, pt.pressure, 0f, 0f, out)
            return out
        }

        // Stabilisation: exponentially approach the raw point. Smoothing factor is
        // derived from distance so short moves stay tight and long moves catch up.
        val strength = spec.stabilization.coerceIn(0f, 0.95f)
        val dist = hypot(pt.x - prev.x, pt.y - prev.y)
        val base = if (dist > 0.5f) 1f - strength * 0.85f else 1f - strength * 0.35f
        val alpha = base.coerceIn(0.05f, 1f)
        smoothX += (pt.x - smoothX) * alpha
        smoothY += (pt.y - smoothY) * alpha

        val dt = (pt.timestampMillis - prev.timestampMillis).coerceAtLeast(1L).toFloat()
        val speed = dist / dt
        velocitySamples.addLast(speed)
        while (velocitySamples.size > 8) velocitySamples.removeFirst()
        val avgSpeed = velocitySamples.average().toFloat()

        // Interpolate from the previous smoothed position to the current one using
        // a midpoint quadratic so corners are rounded instead of polygonal.
        val fromX = prevX
        val fromY = prevY
        val toX = smoothX
        val toY = smoothY
        val cx = (fromX + toX) * 0.5f
        val cy = (fromY + toY) * 0.5f
        val quad = Quad(
            fromX, fromY, cx, cy, toX, toY,
            prev.pressure, (prev.pressure + pt.pressure) * 0.5f, pt.pressure,
            prev.tilt, (prev.tilt + pt.tilt) * 0.5f, pt.tilt,
        )
        emitAlong(quad, pt, avgSpeed, out)
        prevX = toX
        prevY = toY
        return out
    }

    private fun emitAlong(q: Quad, end: StrokePoint, speed: Float, out: MutableList<Dab>) {
        // Sample the quadratic densely enough that spacing is honoured.
        val approxLen = hypot(q.x2 - q.x0, q.y2 - q.y0) + hypot(q.x1 - q.x0, q.y1 - q.y0) * 0.5f
        val steps = kotlin.math.ceil(approxLen / 1.5f).toInt().coerceIn(1, 2048)
        var px = q.x0
        var py = q.y0
        for (i in 1..steps) {
            val t = i.toFloat() / steps
            val x = quadratic(q.x0, q.x1, q.x2, t)
            val y = quadratic(q.y0, q.y1, q.y2, t)
            val segLen = hypot(x - px, y - py)
            if (segLen <= 0.0001f) continue
            val dir = kotlin.math.atan2((y - py).toDouble(), (x - px).toDouble()).toFloat()
            var travelled = 0f
            val pr = lerp(lerp(q.p0, q.p1, t), q.p2, t)
            val tilt = lerp(lerp(q.tilt0, q.tilt1, t), q.tilt2, t)
            // Never let the gap between dabs exceed a third of the nib: a fast
            // stylus sample (or a segmented path) must not produce visible gaps.
            var spacingPx = (spec.size * spec.spacing).coerceAtMost(spec.size * 0.34f).coerceAtLeast(0.5f)
            while (carried + (segLen - travelled) >= spacingPx) {
                val need = spacingPx - carried
                travelled += need
                val u = travelled / segLen
                val dx = px + (x - px) * u
                val dy = py + (y - py) * u
                emitDab(dx, dy, pr, tilt, dir, out, speed)
                strokeLength += spacingPx
                carried = 0f
                spacingPx = (spec.size * spec.spacing).coerceAtMost(spec.size * 0.34f).coerceAtLeast(0.5f)
            }
            carried += segLen - travelled
            px = x
            py = y
        }
    }

    private var dabIndex = 0

    private fun emitDab(x: Float, y: Float, pressure: Float, tilt: Float, dir: Float, out: MutableList<Dab>, speed: Float = 0f) {
        val shaped = spec.curve.apply(pressure)
        var sizeFactor = 1f
        when (spec.pressureTarget) {
            PressureTarget.SIZE -> sizeFactor = 1f + (shaped - 1f) * spec.pressureSize
            PressureTarget.OPACITY, PressureTarget.FLOW -> sizeFactor = 1f
            PressureTarget.SIZE_AND_OPACITY -> sizeFactor = 1f + (shaped - 1f) * spec.pressureSize
            PressureTarget.NONE -> sizeFactor = 1f
        }
        if (spec.tiltSize > 0f) {
            // Tilt from vertical (0) to flat (PI/2): widen the dab up to 1.6x.
            val t = (tilt / (Math.PI.toFloat() / 2f)).coerceIn(0f, 1f)
            sizeFactor *= 1f + t * spec.tiltSize * 0.6f
        }
        if (spec.velocitySize > 0f) {
            // Faster strokes thin out (classic ink behaviour).
            val v = (speed / 1.2f).coerceIn(0f, 1f)
            sizeFactor *= 1f - v * spec.velocitySize * 0.5f
        }
        if (spec.taperMode != TaperMode.NONE) {
            val len = strokeLength.coerceAtLeast(1f)
            val expected = (len).coerceAtLeast(spec.size * 6f)
            val taperPx = (spec.taperLength * expected).coerceAtLeast(1f)
            val atStart = strokeLength < taperPx
            val atEnd = false // end taper is applied on finish() for the trailing dabs we cannot know
            val t = when {
                atStart && (spec.taperMode == TaperMode.START || spec.taperMode == TaperMode.BOTH) ->
                    (strokeLength / taperPx).coerceIn(0f, 1f)
                atEnd -> 1f
                else -> 1f
            }
            sizeFactor *= (spec.taperTipSize + (1f - spec.taperTipSize) * t)
        }
        val radius = (spec.dabRadius * sizeFactor).coerceAtLeast(0.35f)
        out.add(Dab(x, y, radius, shaped, dir, dabIndex++, speed))
    }

    /** Emits the trailing taper dabs when a stroke ends. */
    fun finish(): List<Dab> {
        if (finished) return emptyList()
        finished = true
        val out = ArrayList<Dab>()
        if (spec.taperMode == TaperMode.END || spec.taperMode == TaperMode.BOTH) {
            val last = lastRaw ?: return out
            val n = 6
            for (i in 1..n) {
                val t = 1f - i.toFloat() / n
                val r = (spec.dabRadius * (spec.taperTipSize + (1f - spec.taperTipSize) * t)).coerceAtLeast(0.35f)
                out.add(Dab(last.x, last.y, r, spec.curve.apply(last.pressure), 0f, dabIndex++, 0f))
            }
        }
        return out
    }

    private fun quadratic(a: Float, b: Float, c: Float, t: Float): Float {
        val it = 1f - t
        return it * it * a + 2f * it * t * b + t * t * c
    }

    private fun hypot(dx: Float, dy: Float): Float = kotlin.math.sqrt(dx * dx + dy * dy)

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
}
