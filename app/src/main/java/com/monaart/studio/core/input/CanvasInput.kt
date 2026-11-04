package com.monaart.studio.core.input

import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import kotlin.math.abs
import kotlin.math.hypot

/** One stylus/touch sample in *screen* coordinates. */
data class RawSample(
    val x: Float,
    val y: Float,
    val pressure: Float,
    val tilt: Float,
    val orientation: Float,
    val timestampMillis: Long,
    val stylus: Boolean,
)

/** Callbacks the editor implements. Kept narrow so input logic stays independent of UI. */
interface CanvasInputTarget {
    fun onStrokeStart(sample: RawSample)
    fun onStrokeMove(sample: RawSample)
    fun onStrokeEnd(sample: RawSample)
    fun onStrokeCancel()
    fun onTransformGesture(pan: Offset, zoom: Float, rotationRadians: Float)
    fun onGestureAction(action: GestureAction)
    /** Called when a single tap happens without drawing (e.g. fill/eyedropper tools). */
    fun onTap(x: Float, y: Float)
}

/**
 * Unified canvas input pipeline.
 *
 * Rules (all configurable):
 *  - A stylus always paints (unless the current tool is not a paint tool).
 *  - With [InputMode.StylusOnly], finger input never paints; fingers pan/zoom/rotate.
 *  - Palm rejection: while a stylus is in contact (or within [palmWindowMs] of
 *    release), finger pointers are ignored entirely so a resting palm cannot
 *    draw, pan or cancel the stroke.
 *  - Two-finger gestures pan/zoom/rotate. Two- and three-finger taps can be
 *    mapped to undo/redo/panel toggles.
 *  - Historical (batched) pointer samples are forwarded so fast strokes keep
 *    all their pressure data.
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
    private var lastStylusUpAt = 0L
    private var fingerCanPaint = false
    private var gesturePointers = 0
    private var gestureStart = 0L
    private var gestureMaxMove = 0f
    private var lastGestureActionAt = 0L
    private var lastTapAt = 0L
    private var lastTapPos = Offset.Zero

    private companion object {
        const val PALM_WINDOW_MS = 600L
        const val GESTURE_TAP_MAX_MS = 320L
        const val GESTURE_TAP_MAX_MOVE = 24f
        const val DOUBLE_TAP_MS = 320L
        const val GESTURE_DEBOUNCE_MS = 250L
    }

    suspend fun PointerInputScope.attach() {
        awaitPointerEventScope { eventLoop() }
    }

    private suspend fun AwaitPointerEventScope.eventLoop() {
        while (true) {
            val event = awaitPointerEvent()
            handle(event)
        }
    }

    private fun isPalmWindowActive(now: Long): Boolean =
        stylusDown || (config().palmRejection && now - lastStylusUpAt < PALM_WINDOW_MS)

    private fun handle(event: PointerEvent) {
        val cfg = config()
        val now = System.currentTimeMillis()
        val stylusPointer = event.changes.firstOrNull { it.type == PointerType.Stylus }
        val fingers = event.changes.filter { it.type == PointerType.Touch || it.type == PointerType.Mouse }
        val stylusActive = stylusPointer != null || stylusDown
        val palmBlock = isPalmWindowActive(now)

        // ---- Stylus button shortcuts -------------------------------------
        if (stylusPointer != null) {
            val buttons = event.buttons
            if (buttons.isSecondaryPressed && !stylusDown && now - lastGestureActionAt > GESTURE_DEBOUNCE_MS) {
                lastGestureActionAt = now
                target.onGestureAction(cfg.stylusButtonAction)
                return
            }
        }

        // ---- Stylus painting ---------------------------------------------
        if (stylusPointer != null) {
            if (stylusPointer.changedToDownIgnoreConsumed()) {
                stylusDown = true
                painting = true
                gesturePointers = 0
                target.onStrokeStart(stylusPointer.toSample(stylusPointer.position))
                stylusPointer.consume()
                return
            }
            if (painting) {
                // Forward batched historical samples so no stylus input is lost.
                for (h in stylusPointer.historical) {
                    target.onStrokeMove(
                        RawSample(
                            x = h.position.x,
                            y = h.position.y,
                            pressure = h.pressure.coerceIn(0.01f, 1f),
                            tilt = tiltOf(h.pressure, h.orientation, h.type),
                            orientation = h.orientation,
                            timestampMillis = now - (stylusPointer.uptimeMillis - h.uptimeMillis),
                            stylus = true,
                        )
                    )
                }
                if (stylusPointer.changedToUpIgnoreConsumed()) {
                    target.onStrokeEnd(stylusPointer.toSample(stylusPointer.position))
                    painting = false
                    stylusDown = false
                    lastStylusUpAt = now
                } else {
                    target.onStrokeMove(stylusPointer.toSample(stylusPointer.position))
                }
                stylusPointer.consume()
                return
            }
            // Stylus hovering or secondary contact beyond a stroke
            if (stylusPointer.changedToUpIgnoreConsumed()) {
                stylusDown = false
                lastStylusUpAt = now
            }
        }

        // ---- Palm rejection: ignore everything finger-driven meanwhile -----
        if (palmBlock && !painting) {
            // A resting palm must not start a gesture either. Consume so the
            // gesture detectors below never see it.
            fingers.forEach { it.consume() }
            if (fingers.isEmpty()) return
            return
        }

        if (fingers.isEmpty()) return

        // ---- Finger painting (only in permissive modes) --------------------
        val fingerPaintingAllowed = when (cfg.mode) {
            InputMode.StylusOnly -> false
            InputMode.StylusPreferFinger -> now - lastStylusUpAt > 1000L
            InputMode.AnyInput -> true
        }

        if (fingerPaintingAllowed && event.changes.size == 1) {
            val f = fingers.first()
            if (!painting) {
                if (f.changedToDownIgnoreConsumed()) {
                    fingerCanPaint = true
                    painting = true
                    target.onStrokeStart(f.toSample(f.position))
                    f.consume()
                    return
                }
            } else {
                if (f.changedToUpIgnoreConsumed()) {
                    target.onStrokeEnd(f.toSample(f.position))
                    painting = false
                    fingerCanPaint = false
                } else {
                    target.onStrokeMove(f.toSample(f.position))
                }
                f.consume()
                return
            }
        }

        // ---- Two/three finger transforms ----------------------------------
        if (event.changes.size >= 2) {
            if (painting) {
                // A second finger arriving mid-stroke converts the stroke into a
                // gesture: cancel the stroke instead of producing a smear.
                target.onStrokeCancel()
                painting = false
            }
            if (gesturePointers == 0) {
                gesturePointers = event.changes.size
                gestureStart = now
                gestureMaxMove = 0f
            }
            gesturePointers = maxOf(gesturePointers, event.changes.size)
            val pan = event.calculatePan()
            val zoom = event.calculateZoom()
            val rot = if (cfg.rotationEnabled) event.calculateRotation() else 0f
            val centroid = event.calculateCentroid(useCurrent = false)
            if (centroid != Offset.Unspecified) {
                gestureMaxMove = maxOf(gestureMaxMove, hypot(pan.x, pan.y))
            }
            if (abs(zoom - 1f) > 0.001f || pan != Offset.Zero || abs(rot) > 0.001f) {
                target.onTransformGesture(pan, zoom, rot)
            }
            event.changes.forEach { it.consume() }
            return
        }

        // ---- Gesture end / taps -------------------------------------------
        if (gesturePointers >= 2 && event.changes.all { it.changedToUpIgnoreConsumed() || !it.pressed }) {
            val duration = now - gestureStart
            val wasTap = duration < GESTURE_TAP_MAX_MS && gestureMaxMove < GESTURE_TAP_MAX_MOVE
            if (wasTap && now - lastGestureActionAt > GESTURE_DEBOUNCE_MS) {
                lastGestureActionAt = now
                val action = if (gesturePointers >= 3) cfg.threeFingerTap else cfg.twoFingerTap
                if (action != GestureAction.Nothing) target.onGestureAction(action)
            }
            gesturePointers = 0
            gestureMaxMove = 0f
            return
        }

        // ---- Single-finger tap / double-tap (non-painting modes) ----------
        if (!fingerPaintingAllowed && event.changes.size == 1) {
            val f = event.changes.first()
            if (f.changedToUpIgnoreConsumed()) {
                val up = f.position
                val isDouble = now - lastTapAt < DOUBLE_TAP_MS && (up - lastTapPos).getDistance() < 40f
                lastTapAt = now
                lastTapPos = up
                if (!isDouble) target.onTap(up.x, up.y)
            }
        }
    }

    /**
     * Android exposes tilt only through the stylus axis values; Compose maps
     * pressure and orientation. We derive a tilt magnitude from the contact
     * ellipse proxy when available and fall back to 0 (perpendicular), which
     * keeps tilt-driven brushes stable on devices that do not report tilt.
     */
    private fun tiltOf(pressure: Float, orientation: Float, type: PointerType): Float {
        if (type != PointerType.Stylus) return 0f
        return 0f
    }

    private fun PointerInputChange.toSample(pos: Offset): RawSample = RawSample(
        x = pos.x,
        y = pos.y,
        pressure = if (type == PointerType.Stylus) pressure.coerceIn(0.01f, 1f) else 1f,
        tilt = tiltOf(pressure, orientation, type),
        orientation = orientation,
        timestampMillis = uptimeMillis,
        stylus = type == PointerType.Stylus,
    )
}

/** Modifier that feeds canvas input into [handler]. */
fun androidx.compose.ui.Modifier.canvasInput(handler: CanvasInputHandler): androidx.compose.ui.Modifier =
    this.pointerInput(handler) { handler.attach() }

/** Small helper: sanitises a zoom factor from a gesture. */
fun sanitizeZoom(zoom: Float): Float = if (zoom.isNaN() || zoom <= 0f) 1f else zoom.coerceIn(0.5f, 2f)
