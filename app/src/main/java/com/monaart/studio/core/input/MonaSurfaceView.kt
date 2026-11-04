package com.monaart.studio.core.input

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
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
class MonaSurfaceView @JvmOverloads constructor(
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
     * Android 14+ can deliver the stylus' predicted positions; for a paint app we
     * explicitly want raw sampling only, because prediction would double-draw.
     */
    fun configureForStylus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestUnbufferedDispatch(InputDevice.SOURCE_STYLUS)
        }
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
    val view = remember {
        MonaSurfaceView(androidx.compose.ui.platform.LocalContext.current).apply {
            this.handler = handler
            configureForStylus()
        }
    }
    // The handler is created inside a `remember(config)` block in the workspace;
    // keep the view pointed at the current instance across recompositions.
    remember(handler) { view.handler = handler }
    AndroidView(factory = { view }, modifier = modifier)
}
