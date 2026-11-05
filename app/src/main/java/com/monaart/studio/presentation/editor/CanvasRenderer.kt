package com.monaart.studio.presentation.editor

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Compositor
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.premulToStraight
import com.monaart.engine.view.Viewport

/**
 * Bridges the engine's software compositor to a Compose [ImageBitmap].
 *
 * Only the visible document region is composited, at a resolution capped to
 * 1 device pixel per document pixel (never upsampled). Stroke updates recompose
 * just the dirty rectangle, which keeps input-to-visible latency low on large
 * canvases; a full recompose only happens on zoom/pan/document-structure change.
 */
class CanvasRenderer {
    private var bitmap: Bitmap? = null
    private var image: ImageBitmap? = null
    private var pixmap: Pixmap? = null
    private val compositor = Compositor()

    /** Document-space rectangle currently represented by [image]. */
    var renderedRect: RectI = RectI.EMPTY
        private set
    var renderScale: Float = 1f
        private set
    private var lastViewportHash = 0
    private var lastStructure = -1L

    val currentImage: ImageBitmap? get() = image
    val width: Int get() = bitmap?.width ?: 0
    val height: Int get() = bitmap?.height ?: 0

    fun needsFullRender(doc: ArtDocument, viewport: Viewport): Boolean {
        val hash = viewport.hashCode()
        if (image == null) return true
        if (hash != lastViewportHash) return true
        if (doc.structureRevision != lastStructure) return true
        return false
    }

    /**
     * Rebuilds the visible-region snapshot. [maxDimension] caps the bitmap size so
     * a zoomed-out 4K canvas does not allocate a 4K texture on a phone.
     */
    fun renderAll(doc: ArtDocument, viewport: Viewport, maxDimension: Int = 2560) {
        val visible = viewport.visibleDocRect(padding = 8).intersect(doc.canvasRect)
        if (visible.isEmpty) {
            renderedRect = RectI.EMPTY
            return
        }
        val scale = minOf(viewport.scale.coerceAtLeast(0.02f), 1f)
        var w = (visible.width * scale).toInt().coerceAtLeast(1)
        var h = (visible.height * scale).toInt().coerceAtLeast(1)
        val cap = if (w > h) maxDimension.toFloat() / w else maxDimension.toFloat() / h
        if (cap < 1f) {
            w = (w * cap).toInt().coerceAtLeast(1)
            h = (h * cap).toInt().coerceAtLeast(1)
        }
        ensureCapacity(w, h)
        val pm = Pixmap(w, h)
        // Composite the document region into the pixmap at render resolution.
        compositeRegion(doc, visible, pm)
        pixmap = pm
        val bmp = bitmap ?: Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bitmap = it }
        bmp.setPixels(pm.straightPixels(), 0, w, 0, 0, w, h)
        image = bmp.asImageBitmap()
        renderedRect = visible
        renderScale = w.toFloat() / visible.width
        lastViewportHash = viewport.hashCode()
        lastStructure = doc.structureRevision
    }

    /** Recomposites only [dirtyDoc] (document space) into the existing snapshot. */
    fun updateRegion(doc: ArtDocument, dirtyDoc: RectI) {
        val bmp = bitmap ?: return
        val pm = pixmap ?: return
        if (renderedRect.isEmpty) return
        val visible = renderedRect.intersect(dirtyDoc)
        if (visible.isEmpty) return
        val scale = pm.width.toFloat() / renderedRect.width
        val l = ((visible.left - renderedRect.left) * scale).toInt().coerceIn(0, pm.width)
        val t = ((visible.top - renderedRect.top) * scale).toInt().coerceIn(0, pm.height)
        val r = (((visible.right - renderedRect.left) * scale).toInt() + 1).coerceIn(0, pm.width)
        val b = (((visible.bottom - renderedRect.top) * scale).toInt() + 1).coerceIn(0, pm.height)
        if (r <= l || b <= t) return
        // Clear then composite the sub-region.
        val sub = Pixmap(r - l, b - t)
        compositor.render(doc, sub, visible.copy(), includeBackground = true)
        val pixels = sub.straightPixels()
        bmp.setPixels(pixels, 0, sub.width, l, t, sub.width, sub.height)
        // Mirror into the cached pixmap so a later partial update is consistent.
        for (y in 0 until sub.height) {
            for (x in 0 until sub.width) {
                pm.pixels[(t + y) * pm.width + (l + x)] = sub.pixels[y * sub.width + x]
            }
        }
        image = bmp.asImageBitmap()
    }

    private fun compositeRegion(doc: ArtDocument, region: RectI, out: Pixmap) {
        compositor.render(doc, out, region.copy(), includeBackground = true)
    }

    private fun ensureCapacity(w: Int, h: Int) {
        val b = bitmap
        if (b != null && b.width == w && b.height == h && !b.isRecycled) return
        if (b != null && !b.isRecycled) b.recycle()
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        image = bitmap!!.asImageBitmap()
    }

    fun release() {
        bitmap?.takeIf { !it.isRecycled }?.recycle()
        bitmap = null
        image = null
        pixmap = null
    }
}

/** Converts a document-space rect at render scale into bitmap-local coordinates. */
internal fun RectI.toBitmapRect(origin: RectI, scale: Float): RectI = RectI(
    ((left - origin.left) * scale).toInt(),
    ((top - origin.top) * scale).toInt(),
    (((right - origin.left) * scale).toInt()) + 1,
    (((bottom - origin.top) * scale).toInt()) + 1,
)

internal fun Pixmap.asStraightInts(): IntArray = IntArray(width * height) { premulToStraight(pixels[it]) }
