package com.monaart.studio.core.input

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Invisible view that owns pointer input for the drawing canvas.
 *
 * A paint app cannot use Compose's normalised pointer stream: it loses tilt,
 * hover distance, the raw eraser tool type and — importantly — the batched
 * historical samples that carry most of a fast stroke's pressure data. This view
 * receives raw [MotionEvent]s and forwards them to a [CanvasInputHandler].
 *
 * It requests unbuffered, predispatch-free input handling because a stroke must
 * start on the exact sample the digitizer produced, not after gesture
 * disambiguation.
 */
@SuppressLint("ClickableViewAccessibility")
class MonaSurfaceView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : View(context, attrs) {
        var handler: CanvasInputHandler? = null

        init {
            isClickable = false
            isFocusable = false
            // We render into a Compose canvas above this view; it only captures input.
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            resources_density_ref.get = { resources.displayMetrics.density }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val consumed = handler?.handle(event) ?: false
            return consumed
        }

        /** Hover events are separate from touch events; enable them for stylus cursors. */
        override fun onGenericMotionEvent(event: MotionEvent): Boolean {
            if (event.isFromSource(InputDevice.SOURCE_STYLUS) ||
                event.isFromSource(InputDevice.SOURCE_MOUSE)
            ) {
                if (handler?.handle(event) == true) return true
            }
            return super.onGenericMotionEvent(event)
        }

        override fun onHoverEvent(event: MotionEvent): Boolean {
            handler?.handle(event)
            return super.onHoverEvent(event)
        }

        /**
         * Asks the input pipeline to deliver pointer events without batching, so a
         * stroke starts on the exact sample the digitizer produced.
         *
         * The parameter is a *source class* mask (`SOURCE_CLASS_*`), not a source:
         * passing `SOURCE_STYLUS` was flagged by Lint's WrongConstant check and did
         * not describe a class at all. `requestUnbufferedDispatch` has existed since
         * API 21, so the previous `SDK_INT >= 34` guard meant low-latency input was
         * silently off on Android 8 through 13 — most of the supported range.
         */
        fun configureForStylus() {
            requestUnbufferedDispatch(InputDevice.SOURCE_CLASS_POINTER)
        }
    }

/**
 * Places the [MonaSurfaceView] inside the Compose tree. It sits above the canvas
 * bitmap and below the on-screen overlays (handles, HUD), so overlay buttons keep
 * their own click handling.
 */
@Composable
fun CanvasInputOverlay(
    handler: CanvasInputHandler,
    modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = remember(context) {
        MonaSurfaceView(context).apply { configureForStylus() }
    }
    // The handler is rebuilt whenever input settings change; keep the view pointed
    // at the current instance across recompositions.
    remember(handler) { view.handler = handler }
    AndroidView(
        factory = { view },
        modifier = modifier,
        update = { it.handler = handler },
    )
}
