package com.monaart.engine.document

import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.Pixmap

/**
 * A layer node. Raster layers own a sparse [TileSurface]; group layers own
 * children and no pixels. Ordering inside [ArtDocument.layers] and in
 * [children] is bottom-to-top, matching compositing order.
 */
class Layer(
    val id: Long,
    var name: String,
    val surface: TileSurface?,
    var kind: LayerKind = if (surface == null) LayerKind.GROUP else LayerKind.RASTER,
) {
    var visible: Boolean = true
    var locked: Boolean = false
    var alphaLocked: Boolean = false
    var clipping: Boolean = false
    var opacity: Float = 1f
    var blend: BlendMode = BlendMode.Normal
    var children: MutableList<Layer> = mutableListOf()
    /** Optional cached thumbnail; invalidated by bumping [revision]. */
    var thumbnail: Pixmap? = null
    internal var thumbRevision: Long = -1

    val isGroup: Boolean get() = kind == LayerKind.GROUP
    val width: Int get() = surface?.width ?: 0
    val height: Int get() = surface?.height ?: 0

    fun requireSurface(): TileSurface =
        surface ?: throw IllegalStateException("Layer '$name' is a group layer and has no pixel data")

    /** Number of bytes of pixel data held (approx). */
        fun pixelBytes(): Long = surface?.usedTileCount()?.toLong()?.times(
        TileSurface.TILE_SIZE * TileSurface.TILE_SIZE * 4L) ?: 0L

    /** Flattens groups recursively into [dst] using Normal blending — used by merge down / export helpers. */
    fun flattenTo(dst: Pixmap, dx: Int, dy: Int) {
        if (!visible) return
        val s = surface
        if (s != null) {
            for (y in 0 until s.height) for (x in 0 until s.width) {
                val p = s.getPixel(x, y)
                if (p != 0) {
                    val px = x + dx
                    val py = y + dy
                    if (px in 0 until dst.width && py in 0 until dst.height) {
                        dst.pixels[py * dst.width + px] =
                            com.monaart.engine.paint.PixelOps.over(dst.pixels[py * dst.width + px], p)
                    }
                }
            }
        }
        for (c in children) c.flattenTo(dst, dx, dy)
    }

    fun deepCopy(newId: Long, idGen: () -> Long): Layer {
        val copy = Layer(newId, name, surface?.let { TileSurface(it.width, it.height, it.tileSize) }, kind)
        if (surface != null) {
            val t = copy.surface!!
            for (k in surface.tileKeys()) {
                val coords = surface.tileCoords(k)
                surface.peekTile(coords[0], coords[1])?.let { src ->
                    System.arraycopy(src, 0, t.obtainTile(coords[0], coords[1]), 0, src.size)
                }
            }
            t.touch()
        }
        copy.visible = visible
        copy.locked = locked
        copy.alphaLocked = alphaLocked
        copy.clipping = clipping
        copy.opacity = opacity
        copy.blend = blend
        copy.children = children.map { it.deepCopy(idGen(), idGen) }.toMutableList()
        return copy
    }
}

enum class LayerKind { RASTER, GROUP }
