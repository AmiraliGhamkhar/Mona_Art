package com.monaart.studio.core.input

import android.view.MotionEvent
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

/** One stylus/touch sample in *view* (device) pixels. */
data class RawSample(
    val x: Float,
    val y: Float,
    val pressure: Float,
    /** Tilt away from the surface normal, 0 = perpendicular, ~π/2 = flat on glass. */
    val tilt: Float,
    /** Direction of the stylus lean in radians (screen space). */
    val orientation: Float,
    val timestampMillis: Long,
    val stylus: Boolean,
    /** True when the tool is flipped to the eraser end or the side button is held. */
    val eraser: Boolean = false,
    val buttonsPressed: Boolean = false,
)

/** Callbacks the editor implements. Narrow so input logic stays independent of UI. */
interface CanvasInputTarget {
    fun onStrokeStart(sample: RawSample)
    fun onStrokeMove(sample: RawSample)
    fun onStrokeEnd(sample: RawSample)
    fun onStrokeCancel()
    fun onTransformGesture(panX: Float, panY: Float, zoom: Float, rotationRadians: Float)
    fun onGestureAction(action: GestureAction)
    /** Called when a single tap happens without drawing (fill/eyedropper tools). */
    fun onTap(x: Float, y: Float)
    /** Hover in progress (stylus above the glass); used for a brush cursor. */
    fun onHover(x: Float, y: Float, active: Boolean) {}
}

/**
 * Canvas input pipeline driven directly by [MotionEvent].
 *
 * Compose's pointer API normalises events and drops several axes a paint app
 * needs (tilt, distance/hover, tool type beyond "stylus", historical batches with
 * their own pressure). [MonaSurfaceView] therefore forwards raw motion events,
 * and this class owns the gesture logic:
 *
 *  - A stylus always paints (subject to [InputMode]).
 *  - Palm rejection: fingers are ignored for [PALM_WINDOW_MS] after the stylus
 *    touched the glass, and while the stylus is hovering closer than
 *    [HOVER_PALM_DISTANCE_MM] (a palm rests *after* the tip lands).
 *  - Two-finger gestures pan/zoom/rotate; two- and three-finger taps can be
 *    mapped to undo/redo/panel toggles.
 *  - Batched historical samples are forwarded so fast strokes keep every
 *    pressure and tilt reading the digitizer produced.
 *
 * The class is deliberately free of Android UI types other than MotionEvent so
 * it can be unit-tested with synthetic event streams (see `StylusInputTest`).
 */
class CanvasInputHandler(
    private val target: CanvasInputTarget,
    private val config: () -> InputConfig,
) {
    data class InputConfig(
        val mode: InputMode = InputMode.StylusOnly,
        val palmRejection: Boolean = true,
        val rotationEnabled: Boolean = true,
        val doubleTapZoom: Boolean = true,
        val twoFingerTap: GestureAction = GestureAction.Undo,
        val threeFingerTap: GestureAction = GestureAction.Redo,
        val stylusButtonAction: GestureAction = GestureAction.Undo,
    )

    private var stylusDown = false
    private var painting = false
    private var lastStylusUpAt = Long.MIN_VALUE
    private var lastHoverAt = 0L
    private var hoverDistanceMm = 0f
    private var fingerCanPaint = false
    private var gesturePointers = 0
    private var gestureStart = 0L
    private var gestureMaxMove = 0f
    private var gestureStartCentroid = 0f to 0f
    private var lastGestureActionAt = Long.MIN_VALUE
    private var lastTapAt = Long.MIN_VALUE
    private var lastTapX = 0f
    private var lastTapY = 0f
    private var eraserActive = false

    private companion object {
        const val PALM_WINDOW_MS = 600L
        const val HOVER_PALM_WINDOW_MS = 250L
        const val HOVER_PALM_DISTANCE_MM = 30f
        const val GESTURE_TAP_MAX_MS = 320L
        const val GESTURE_TAP_MAX_MOVE = 24f
        const val DOUBLE_TAP_MS = 320L
        const val DOUBLE_TAP_SLOP = 48f
        const val GESTURE_DEBOUNCE_MS = 250L
    }

    /** Feeds one event. Returns true when the event was consumed by the canvas. */
    fun handle(event: MotionEvent): Boolean {
        val cfg = config()
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return onDown(event, cfg)
            MotionEvent.ACTION_POINTER_DOWN -> return onPointerDown(event, cfg)
            MotionEvent.ACTION_MOVE -> return onMove(event, cfg)
            MotionEvent.ACTION_POINTER_UP -> return onPointerUp(event, cfg)
            MotionEvent.ACTION_UP -> return onUp(event, cfg)
            MotionEvent.ACTION_CANCEL -> return onCancel()
            MotionEvent.ACTION_HOVER_MOVE, MotionEvent.ACTION_HOVER_ENTER ->
                return onHover(event)
            MotionEvent.ACTION_HOVER_EXIT -> {
                target.onHover(0f, 0f, false)
                return false
            }
        }
        return false
    }

    // -- press ------------------------------------------------------------

    private fun onDown(event: MotionEvent, cfg: InputConfig): Boolean {
        val isStylus = event.isStylusEvent()
        val now = event.eventTime
        eraserActive = isStylus && event.isEraserEvent()
        if (isStylus) {
            stylusDown = true
            if (event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY != 0 &&
                !eraserActive && now - lastGestureActionAt > GESTURE_DEBOUNCE_MS
            ) {
                lastGestureActionAt = now
                target.onGestureAction(cfg.stylusButtonAction)
                return true
            }
            painting = true
            gesturePointers = 0
            target.onStrokeStart(sample(event, 0))
            return true
        }
        // Finger
        if (isPalmBlocked(now)) return true
        fingerCanPaint = cfg.mode == InputMode.AnyInput
        if (fingerCanPaint) {
            painting = true
            eraserActive = false
            target.onStrokeStart(sample(event, 0))
            return true
        }
        // Gesture candidate
        gesturePointers = event.pointerCount
        gestureStart = now
        gestureMaxMove = 0f
        gestureStartCentroid = centroid(event)
        return false
    }

    private fun onPointerDown(event: MotionEvent, cfg: InputConfig): Boolean {
        val now = event.eventTime
        if (isPalmBlocked(now) && !stylusDown) return true
        if (painting && fingerCanPaint) {
            // A second finger during a finger stroke converts it into a gesture:
            // cancel the stroke rather than leaving a stray mark.
            target.onStrokeCancel()
            painting = false
        }
        gesturePointers = event.pointerCount
        gestureStart = now
        gestureMaxMove = 0f
        gestureStartCentroid = centroid(event)
        return true
    }

    // -- move -------------------------------------------------------------

    private fun onMove(event: MotionEvent, cfg: InputConfig): Boolean {
        val isStylus = event.isStylusEvent()
        if (isStylus && painting) {
            stylusDown = true
            validateStroke(event)
            // Historical samples keep full pressure/tilt fidelity on fast strokes.
            for (h in 0 until event.historySize) {
                val s = sample(event, 0, useHistoryIndex = h)
                target.onStrokeMove(s)
            }
            target.onStrokeMove(sample(event, 0))
            return true
        }
        if (painting && fingerCanPaint) {
            if (isPalmBlocked(event.eventTime)) return true
            validateStroke(event)
            for (h in 0 until event.historySize) target.onStrokeMove(sample(event, 0, h))
            target.onStrokeMove(sample(event, 0))
            return true
        }
        // Multi-finger transform
        if (event.pointerCount >= 2) {
            val centroid = centroid(event)
            val dx = centroid.first - gestureStartCentroid.first
            val dy = centroid.second - gestureStartCentroid.second
            gestureStartCentroid = centroid
            gestureMaxMove = maxOf(gestureMaxMove, hypot(dx, dy))
            val (prevSpan, prevAngle) = spanAndAngle(event, useHistorical = true)
            val (span, angle) = spanAndAngle(event, useHistorical = false)
            if (prevSpan > 0.5f && span > 0.5f) {
                val zoom = (span / prevSpan).coerceIn(0.5f, 2f)
                val rotation = if (cfg.rotationEnabled) angle - prevAngle else 0f
                target.onTransformGesture(dx, dy, zoom, rotation)
            } else {
                target.onTransformGesture(dx, dy, 1f, 0f)
            }
            return true
        }
        // Stylus hover with button held can still pan; ignore other moves.
        return false
    }

    /** Drops a stroke whose samples are implausible (palm, hardware glitch). */
    private fun validateStroke(event: MotionEvent) {
        if (event.pointerCount == 0) {
            target.onStrokeCancel()
            painting = false
            return
        }
        if (event.isPalmSize()) {
            target.onStrokeCancel()
            painting = false
        }
    }

    private fun onPointerUp(event: MotionEvent, cfg: InputConfig): Boolean {
        gesturePointers = event.pointerCount - 1
        return true
    }

    // -- release ----------------------------------------------------------

    private fun onUp(event: MotionEvent, cfg: InputConfig): Boolean {
        val now = event.eventTime
        val wasStylus = event.isStylusEvent() || stylusDown
        if (painting) {
            target.onStrokeEnd(sample(event, 0))
            painting = false
        }
        if (wasStylus) {
            stylusDown = false
            lastStylusUpAt = now
            hoverDistanceMm = 0f
        }
        // Tap-only interactions (undo gestures, double-tap zoom, fill/eyedropper)
        val duration = now - gestureStart
        val isTap = duration <= GESTURE_TAP_MAX_MS && gestureMaxMove <= GESTURE_TAP_MAX_MOVE
        if (wasStylus && isTap && duration <= 40L) {
            target.onTap(event.x, event.y)
        }
        if (!wasStylus && isTap && gesturePointers >= 1) {
            when (gesturePointers) {
                2 -> if (now - lastGestureActionAt > GESTURE_DEBOUNCE_MS) {
                    lastGestureActionAt = now
                    target.onGestureAction(cfg.twoFingerTap)
                }
                3 -> if (now - lastGestureActionAt > GESTURE_DEBOUNCE_MS) {
                    lastGestureActionAt = now
                    target.onGestureAction(cfg.threeFingerTap)
                }
                else -> {
                    if (cfg.doubleTapZoom && now - lastTapAt <= DOUBLE_TAP_MS &&
                        hypot(event.x - lastTapX, event.y - lastTapY) <= DOUBLE_TAP_SLOP &&
                        now - lastGestureActionAt > GESTURE_DEBOUNCE_MS
                    ) {
                        lastGestureActionAt = now
                        target.onGestureAction(GestureAction.ToggleZoom)
                    }
                    lastTapAt = now
                    lastTapX = event.x
                    lastTapY = event.y
                }
            }
        } else if (!wasStylus && duration > GESTURE_TAP_MAX_MS) {
            lastTapAt = Long.MIN_VALUE
        }
        gesturePointers = 0
        fingerCanPaint = false
        return true
    }

    private fun onCancel(): Boolean {
        if (painting) target.onStrokeCancel()
        painting = false
        stylusDown = false
        gesturePointers = 0
        fingerCanPaint = false
        return true
    }

    private fun onHover(event: MotionEvent): Boolean {
        lastHoverAt = event.eventTime
        hoverDistanceMm = event.getAxisValue(MotionEvent.AXIS_DISTANCE)
        target.onHover(event.x, event.y, true)
        return false
    }

    // -- helpers ----------------------------------------------------------

    /** True when finger input must be ignored because a hand/palm is resting. */
    private fun isPalmBlocked(now: Long): Boolean {
        if (!config().palmRejection) return false
        if (stylusDown) return true
        val sinceLift = now - lastStylusUpAt
        if (sinceLift in 0..PALM_WINDOW_MS) return true
        // A hovering stylus means the palm may be resting right now.
        return (now - lastHoverAt) in 0..HOVER_PALM_WINDOW_MS && hoverDistanceMm in 0.01f..HOVER_PALM_DISTANCE_MM
    }

    private fun sample(event: MotionEvent, index: Int, useHistoryIndex: Int? = null): RawSample {
        val pointerIndex = event.findPointerIndex(event.getPointerId(index))
        val idx = if (pointerIndex >= 0) pointerIndex else index
        val history = useHistoryIndex
        val x = if (history != null) event.getHistoricalX(idx, history) else event.getX(idx)
        val y = if (history != null) event.getHistoricalY(idx, history) else event.getY(idx)
        val pressure = if (history != null) event.getHistoricalPressure(idx, history) else event.getPressure(idx)
        // AXIS_TILT is only reported by tilt-capable digitizers; when the range
        // is empty the value is meaningless, so treat it as perpendicular.
        val tiltRange = event.device?.getMotionRange(MotionEvent.AXIS_TILT)
        val tilt = if (tiltRange != null && tiltRange.max > 0f) {
            event.getAxisValue(MotionEvent.AXIS_TILT, idx).coerceIn(0f, tiltRange.max)
        } else {
            0f
        }
        val orientation = if (event.device?.getMotionRange(MotionEvent.AXIS_ORIENTATION) != null) {
            event.getAxisValue(MotionEvent.AXIS_ORIENTATION, idx)
        } else {
            0f
        }
        val time = if (history != null) {
            event.getHistoricalEventTime(history)
        } else {
            event.eventTime
        }
        return RawSample(
            x = x,
            y = y,
            pressure = if (event.isStylusEvent()) pressure.coerceIn(0.01f, 1f) else 1f,
            tilt = tilt,
            orientation = orientation,
            timestampMillis = time,
            stylus = event.isStylusEvent(),
            eraser = eraserActive || event.isEraserEvent(),
            buttonsPressed = event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY != 0,
        )
    }

    private fun centroid(event: MotionEvent): Pair<Float, Float> {
        var sx = 0f
        var sy = 0f
        var n = 0
        for (i in 0 until event.pointerCount) {
            if (event.getToolType(i) == MotionEvent.TOOL_TYPE_STYLUS) continue
            sx += event.getX(i)
            sy += event.getY(i)
            n++
        }
        if (n == 0) return 0f to 0f
        return sx / n to sy / n
    }

    private fun spanAndAngle(event: MotionEvent, useHistorical: Boolean): Pair<Float, Float> {
        val n = minOf(event.pointerCount, 5)
        if (n < 2) return 1f to 0f
        var sx = 0f
        var sy = 0f
        for (i in 0 until n) {
                        sx += if (useHistorical && event.historySize > 0) event.getHistoricalX(i,
                event.historySize - 1) else event.getX(i)
                        sy += if (useHistorical && event.historySize > 0) event.getHistoricalY(i,
                event.historySize - 1) else event.getY(i)
        }
        sx /= n
        sy /= n
        var span = 0f
        var angleX = 0f
        var angleY = 0f
        for (i in 0 until n) {
                        val px = if (useHistorical && event.historySize > 0) event.getHistoricalX(i,
                event.historySize - 1) else event.getX(i)
                        val py = if (useHistorical && event.historySize > 0) event.getHistoricalY(i,
                event.historySize - 1) else event.getY(i)
            val dx = px - sx
            val dy = py - sy
            span += sqrt(dx * dx + dy * dy)
            angleX += dx
            angleY += dy
        }
        return span / n to atan2(angleY, angleX)
    }
}

/** True when the primary tool of this event is a stylus. */
internal fun MotionEvent.isStylusEvent(): Boolean =
    getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS || getToolType(0) == MotionEvent.TOOL_TYPE_ERASER

/** Honest eraser detection: tool type or the inverted-eraser axis. */
internal fun MotionEvent.isEraserEvent(): Boolean {
    if (getToolType(0) == MotionEvent.TOOL_TYPE_ERASER) return true
    // Some EMR pens report the eraser by setting negative pressure on the button
    // axis; MotionEvent models that as a large negative axis value.
    val range = device?.getMotionRange(MotionEvent.AXIS_DISTANCE) ?: return false
    return range.min < 0f && getAxisValue(MotionEvent.AXIS_DISTANCE) < 0f
}

/** Coarse palm heuristic from contact size; stylus tips are small. */
internal fun MotionEvent.isPalmSize(): Boolean {
    if (getToolType(0) != MotionEvent.TOOL_TYPE_FINGER) return false
    val major = getTouchMajor(0)
    val minor = getTouchMinor(0)
    if (major <= 0f || minor <= 0f) return false
    // Palm contacts are wide; compare against the device's own reported density.
    return major > 0.9f * resources_density_ref.get() || minor > 0.9f * resources_density_ref.get()
}

/** Density holder filled in by the view so the heuristic can stay unit-testable. */
internal object resources_density_ref {
    var get: () -> Float = { 1f }
}

/** Small helper: sanitises a zoom factor from a gesture. */
fun sanitizeZoom(zoom: Float): Float = if (zoom.isNaN() || zoom <= 0f) 1f else zoom.coerceIn(0.5f, 2f)

/** Tilt in radians converted to a 0..1 "flatness" factor for brushes. */
fun tiltFlatness(tiltRadians: Float): Float {
    val max = (Math.PI / 2).toFloat()
    return (abs(tiltRadians) / max).coerceIn(0f, 1f)
}
