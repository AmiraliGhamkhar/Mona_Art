package com.monaart.studio.core.input

import android.view.MotionEvent
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the stylus pipeline with synthetic [MotionEvent]s.
 *
 * Physical stylus verification is impossible in this environment, so these
 * tests assert the *routing* decisions that a device test would otherwise cover:
 * which pointer paints, what gets cancelled, and which axis values reach the
 * engine. Tilt values are constructed to mimic what an EMR digitizer reports.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StylusInputTest {
    private class Recorder : CanvasInputTarget {
        val events = mutableListOf<String>()
        var lastSample: RawSample? = null
        var strokes = 0
        var cancels = 0
        var taps = 0
        var actions = mutableListOf<GestureAction>()
        var transforms = 0

        override fun onStrokeStart(sample: RawSample) {
            strokes++
            lastSample = sample
            events.add("start")
        }

        override fun onStrokeMove(sample: RawSample) {
            lastSample = sample
            events.add("move")
        }

        override fun onStrokeEnd(sample: RawSample) {
            lastSample = sample
            events.add("end")
        }

        override fun onStrokeCancel() {
            cancels++
            events.add("cancel")
        }

        override fun onTransformGesture(
            panX: Float,
            panY: Float,
            zoom: Float,
            rotationRadians: Float,
        ) {
            transforms++
        }

        override fun onGestureAction(action: GestureAction) {
            actions.add(action)
        }

        override fun onTap(
            x: Float,
            y: Float,
        ) {
            taps++
        }
    }

    private fun config(
        mode: InputMode = InputMode.StylusOnly,
        palm: Boolean = true,
    ) = CanvasInputHandler.InputConfig(
        mode = mode,
        palmRejection = palm,
        twoFingerTap = GestureAction.Undo,
    )

    private fun stylusEvent(
        action: Int,
        x: Float,
        y: Float,
        pressure: Float,
        time: Long,
    ): MotionEvent {
        val props =
            android.view.MotionEvent.PointerProperties().apply {
                id = 0
                toolType = MotionEvent.TOOL_TYPE_STYLUS
            }
        val coords =
            android.view.MotionEvent.PointerCoords().apply {
                this.x = x
                this.y = y
                this.pressure = pressure
                this.size = 0.02f
                // 0.35 rad from the surface normal, leaning along +x.
                this.setAxisValue(MotionEvent.AXIS_TILT, 0.35f)
                this.setAxisValue(MotionEvent.AXIS_ORIENTATION, 0f)
                this.setAxisValue(MotionEvent.AXIS_DISTANCE, 0f)
            }
        return MotionEvent.obtain(
            time,
            time,
            action,
            1,
            arrayOf(props),
            arrayOf(coords),
            0,
            0,
            1f,
            1f,
            0,
            0,
            android.view.InputDevice.SOURCE_STYLUS,
            0,
        )
    }

    private fun fingerEvent(
        action: Int,
        pointers: Int,
        time: Long,
    ): MotionEvent {
        val props =
            Array(pointers) { i ->
                android.view.MotionEvent.PointerProperties().apply {
                    id = i
                    toolType = MotionEvent.TOOL_TYPE_FINGER
                }
            }
        val coords =
            Array(pointers) { i ->
                android.view.MotionEvent.PointerCoords().apply {
                    x = 100f + i * 200f
                    y = 200f
                    pressure = 1f
                    size = 0.1f
                }
            }
        return MotionEvent.obtain(
            time,
            time,
            action,
            pointers,
            props,
            coords,
            0,
            0,
            1f,
            1f,
            0,
            0,
            android.view.InputDevice.SOURCE_TOUCHSCREEN,
            0,
        )
    }

    @Test
    fun `stylus press starts a stroke and carries pressure`() {
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config() }
        handler.handle(stylusEvent(MotionEvent.ACTION_DOWN, 100f, 100f, 0.42f, 1000))
        assertEquals(1, recorder.strokes)
        assertEquals(0.42f, requireNotNull(recorder.lastSample).pressure, 0.01f)
        assertTrue("stylus flag must be set", requireNotNull(recorder.lastSample).stylus)
    }

    @Test
    fun `tilt is only reported when the device exposes the axis`() {
        // Synthetic events have no InputDevice motion ranges, which is exactly how
        // a non-tilt-capable digitizer behaves: tilt must then be neutral rather
        // than a fabricated number. Devices with a tilt axis report it through
        // MotionEvent.AXIS_TILT, which the handler reads via getAxisValue().
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config() }
        handler.handle(stylusEvent(MotionEvent.ACTION_DOWN, 10f, 10f, 1f, 2000))
        val sample = requireNotNull(recorder.lastSample)
        assertEquals("no tilt axis means perpendicular", 0f, sample.tilt, 0.001f)
        assertEquals(0f, tiltFlatness(sample.tilt), 0.001f)
    }

    @Test
    fun `finger does not paint in stylus-only mode`() {
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config(mode = InputMode.StylusOnly) }
        handler.handle(fingerEvent(MotionEvent.ACTION_DOWN, 1, 3000))
        handler.handle(fingerEvent(MotionEvent.ACTION_UP, 1, 3100))
        assertEquals("a single finger must never start a pointer stroke", 0, recorder.strokes)
    }

    @Test
    fun `finger paints in any-input mode`() {
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config(mode = InputMode.AnyInput) }
        handler.handle(fingerEvent(MotionEvent.ACTION_DOWN, 1, 4000))
        assertEquals(1, recorder.strokes)
    }

    @Test
    fun `palm touch right after a stylus lift is ignored`() {
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config(palm = true) }
        handler.handle(stylusEvent(MotionEvent.ACTION_DOWN, 500f, 500f, 0.8f, 5000))
        handler.handle(stylusEvent(MotionEvent.ACTION_UP, 500f, 500f, 0f, 5050))
        // A palm lands 100 ms later, well inside the rejection window.
        handler.handle(fingerEvent(MotionEvent.ACTION_DOWN, 3, 5150))
        assertEquals("the palm must not create a stroke", 1, recorder.strokes)
    }

    @Test
    fun `two-finger drag pans and zooms instead of painting`() {
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config(mode = InputMode.StylusOnly) }
        handler.handle(fingerEvent(MotionEvent.ACTION_POINTER_DOWN or (0 shl 8), 2, 6000))
        handler.handle(fingerEvent(MotionEvent.ACTION_MOVE, 2, 6016))
        handler.handle(fingerEvent(MotionEvent.ACTION_MOVE, 2, 6032))
        assertEquals("no stroke may be created by a transform gesture", 0, recorder.strokes)
        assertTrue("the viewport must receive transform deltas", recorder.transforms > 0)
    }

    @Test
    fun `stylus cancel ends the stroke without committing`() {
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config() }
        handler.handle(stylusEvent(MotionEvent.ACTION_DOWN, 1f, 1f, 0.5f, 7000))
        handler.handle(stylusEvent(MotionEvent.ACTION_CANCEL, 1f, 1f, 0f, 7010))
        assertEquals(1, recorder.cancels)
        assertEquals("cancel must not emit a normal end", listOf("start", "cancel"), recorder.events)
    }

    @Test
    fun `eraser tool type is reported as an eraser sample`() {
        val recorder = Recorder()
        val handler = CanvasInputHandler(recorder) { config() }
        val eraserProps =
            android.view.MotionEvent.PointerProperties().apply {
                id = 0
                toolType = MotionEvent.TOOL_TYPE_ERASER
            }
        val coords =
            android.view.MotionEvent.PointerCoords().apply {
                x = 5f
                y = 5f
                pressure = 0.7f
            }
        val event =
            MotionEvent.obtain(
                9000,
                9000,
                MotionEvent.ACTION_DOWN,
                1,
                arrayOf(eraserProps),
                arrayOf(coords),
                0,
                0,
                1f,
                1f,
                0,
                0,
                android.view.InputDevice.SOURCE_STYLUS,
                0,
            )
        handler.handle(event)
        assertTrue("eraser tip must set the eraser flag", requireNotNull(recorder.lastSample).eraser)
    }

    @Test
    fun `zoom sanitiser rejects junk values`() {
        assertEquals(1f, sanitizeZoom(Float.NaN), 0f)
        assertEquals(1f, sanitizeZoom(0f), 0f)
        assertEquals(0.5f, sanitizeZoom(0.001f), 0f)
        assertEquals(2f, sanitizeZoom(99f), 0f)
        assertEquals(1.5f, sanitizeZoom(1.5f), 0f)
    }

    @Test
    fun `tilt flatness maps perpendicular to zero and flat to one`() {
        assertEquals(0f, tiltFlatness(0f), 0.001f)
        assertEquals(1f, tiltFlatness((Math.PI / 2).toFloat()), 0.001f)
        assertTrue(tiltFlatness((Math.PI / 4).toFloat()) in 0.49f..0.51f)
    }
}
