package com.monaart.engine.view

import kotlin.math.cos
import kotlin.math.sin

/**
 * Canvas viewport transform: document pixels <-> screen pixels.
 *
 * `screen = center + R(rotation) * scale * (doc - pivot)`
 * where the pivot is the rotation centre (canvas centre by default).
 * Kept in the engine (not the UI) so gesture handling, hit-testing and
 * rendering all share one mathematically tested implementation.
 */
data class Viewport(
    val scale: Float = 1f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    /** Rotation in radians. */
    val rotation: Float = 0f,
    val viewportWidth: Float = 0f,
    val viewportHeight: Float = 0f,
    val docWidth: Int = 1,
    val docHeight: Int = 1,
) {
    val cosR: Float get() = cos(rotation)
    val sinR: Float get() = sin(rotation)

    private val pivotX: Float get() = viewportWidth * 0.5f + offsetX
    private val pivotY: Float get() = viewportHeight * 0.5f + offsetY

    fun docToScreen(dx: Float, dy: Float): Pair<Float, Float> {
        val cx = docWidth * 0.5f
        val cy = docHeight * 0.5f
        val x = (dx - cx) * scale
        val y = (dy - cy) * scale
        val rx = x * cosR - y * sinR
        val ry = x * sinR + y * cosR
        val sx = viewportWidth * 0.5f + offsetX + rx
        val sy = viewportHeight * 0.5f + offsetY + ry
        return sx to sy
    }

    fun screenToDoc(sx: Float, sy: Float): Pair<Float, Float> {
        val rx = sx - (viewportWidth * 0.5f + offsetX)
        val ry = sy - (viewportHeight * 0.5f + offsetY)
        val x = (rx * cosR + ry * sinR) / scale
        val y = (-rx * sinR + ry * cosR) / scale
        return (x + docWidth * 0.5f) to (y + docHeight * 0.5f)
    }

    /** Visible document rectangle (axis-aligned bounding box of the rotated viewport). */
    fun visibleDocRect(padding: Int = 0): com.monaart.engine.paint.RectI {
        val corners = listOf(
            screenToDoc(0f, 0f),
            screenToDoc(viewportWidth, 0f),
            screenToDoc(0f, viewportHeight),
            screenToDoc(viewportWidth, viewportHeight),
        )
        val minX = corners.minOf { it.first } - padding
        val maxX = corners.maxOf { it.first } + padding
        val minY = corners.minOf { it.second } - padding
        val maxY = corners.maxOf { it.second } + padding
        val l = kotlin.math.floor(minX).toInt().coerceIn(-1, docWidth)
        val t = kotlin.math.floor(minY).toInt().coerceIn(-1, docHeight)
        val r = kotlin.math.ceil(maxX).toInt().coerceIn(0, docWidth + 1)
        val b = kotlin.math.ceil(maxY).toInt().coerceIn(0, docHeight + 1)
        return com.monaart.engine.paint.RectI(l, t, maxOf(r, l + 1), maxOf(b, t + 1))
    }

    fun zoomed(factor: Float, focusScreenX: Float, focusScreenY: Float, minScale: Float = 0.02f, maxScale: Float = 64f): Viewport {
        val (fx, fy) = screenToDoc(focusScreenX, focusScreenY)
        val newScale = (scale * factor).coerceIn(minScale, maxScale)
        val applied = newScale / scale
        val zoomedVp = copy(scale = newScale)
        val (nx, ny) = zoomedVp.docToScreen(fx, fy)
        return zoomedVp.copy(
            offsetX = zoomedVp.offsetX + (focusScreenX - nx),
            offsetY = zoomedVp.offsetY + (focusScreenY - ny),
        ).also { require(applied > 0f) }
    }

    fun panned(dxScreen: Float, dyScreen: Float): Viewport = copy(offsetX = offsetX + dxScreen, offsetY = offsetY + dyScreen)

    fun rotated(deltaRadians: Float): Viewport = copy(rotation = rotation + deltaRadians)

    /** Fits the whole document in the viewport with [margin] screen px. */
    fun fit(margin: Float = 24f): Viewport {
        val sx = (viewportWidth - margin * 2) / docWidth
        val sy = (viewportHeight - margin * 2) / docHeight
        return copy(scale = minOf(sx, sy).coerceAtLeast(0.02f), offsetX = 0f, offsetY = 0f, rotation = 0f)
    }

    /** Zoom level as a percentage relative to 1 device px per document px. */
    val zoomPercent: Int get() = (scale * 100).toInt()

    companion object {
        fun identity(viewW: Float, viewH: Float, docW: Int, docH: Int) =
            Viewport(1f, 0f, 0f, 0f, viewW, viewH, docW, docH).fit()
    }
}
