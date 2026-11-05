package com.monaart.engine.document

import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.PixelOps
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.alphaOf

/**
 * Software compositor for the layer tree.
 *
 * Rendering is region-based: only the requested rectangle is touched, and only
 * tiles that actually contain data are walked, so a large document with a few
 * painted areas composites in proportion to painted pixels rather than canvas
 * size. The same code path drives on-screen updates and export, so what the
 * artist sees is what is exported.
 */
class Compositor {

    fun render(doc: ArtDocument, out: Pixmap, region: RectI = doc.canvasRect, includeBackground: Boolean = true) {
        val r = region.intersect(RectI.wh(out.width, out.height)).intersect(doc.canvasRect)
        if (r.isEmpty) return
        if (includeBackground) {
            val bg = if (doc.transparentBackground) 0 else doc.backgroundColor
            if (bg != 0) {
                for (y in r.top until r.bottom) {
                    java.util.Arrays.fill(out.pixels, y * out.width + r.left, y * out.width + r.right, bg)
                }
            }
        }
        renderStack(doc.layers, out, r)
    }

    /** Composites a list of layers (bottom to top) into [out] over [region]. */
    fun renderStack(list: List<Layer>, out: Pixmap, region: RectI) {
        var i = 0
        while (i < list.size) {
            val layer = list[i]
            if (layer.clipping) { i++; continue } // handled with its base below
            // Gather the following clipping layers.
            var j = i + 1
            while (j < list.size && list[j].clipping) j++
            val clipped = if (j > i + 1) list.subList(i + 1, j) else emptyList()

            if (clipped.isEmpty()) {
                compositeSingle(layer, out, region)
            } else {
                // A clipping layer is visible only where its base is opaque, so the
                // group is first built with normal blending and then multiplied by
                // the base's own alpha. Composite the clipped layers onto a copy of
                // the base, then mask by the base alpha on the way out.
                val baseBuf = Pixmap(region.width, region.height)
                renderLayerIsolated(layer, baseBuf, region)
                val clippedBuf = Pixmap(region.width, region.height)
                for (c in clipped) compositeSingle(c, clippedBuf, region)
                applyClipMask(clippedBuf, baseBuf, region)
                compositeBuffer(baseBuf, out, region, layer.blend, layer.opacity * layer.visibleFactor())
                // The clipped pixels themselves replace nothing: they only tint the
                // base region, which is the definition of a clipping mask.
                compositeClippedOverBase(clippedBuf, baseBuf, region)
            }
            i = j
        }
    }

    private fun Layer.visibleFactor(): Float = if (visible) 1f else 0f

    /** Multiplies the clipped pixels by the base layer's alpha. */
    private fun applyClipMask(clipped: Pixmap, base: Pixmap, region: RectI) {
        for (y in 0 until region.height) {
            for (x in 0 until region.width) {
                val i = y * region.width + x
                val src = clipped.pixels[i]
                if (src == 0) continue
                val baseAlpha = com.monaart.engine.paint.alphaOf(base.pixels[i])
                if (baseAlpha == 0) {
                    clipped.pixels[i] = 0
                    continue
                }
                if (baseAlpha == 255) continue
                clipped.pixels[i] = com.monaart.engine.paint.PixelOps.scaleAlpha(src, baseAlpha / 255f)
            }
        }
    }

    /**
     * Draws clipped pixels above their base. The result is what the user sees:
     * base colour where the base is opaque, clipped colour replacing it where the
     * clipping layer paints.
     */
    private fun compositeClippedOverBase(clipped: Pixmap, base: Pixmap, region: RectI) {
        for (y in 0 until region.height) {
            val row = y * region.width
            for (x in 0 until region.width) {
                val src = clipped.pixels[row + x]
                if (src == 0) continue
                base.pixels[row + x] = com.monaart.engine.paint.PixelOps.composite(
                    BlendMode.Normal, base.pixels[row + x], src, 1f, 1f,
                )
            }
        }
    }

    private fun compositeSingle(layer: Layer, out: Pixmap, region: RectI) {
        if (!layer.visible || layer.opacity <= 0f) return
        if (layer.isGroup) {
            val buf = Pixmap(region.width, region.height)
            renderStack(layer.children, buf, region)
            compositeBuffer(buf, out, region, layer.blend, layer.opacity)
        } else {
            val s = layer.surface ?: return
            compositeSurface(s, out, region, layer.blend, layer.opacity)
        }
    }

    /** Renders a layer (and its children) onto an empty buffer, ignoring its own blend mode. */
    fun renderLayerIsolated(layer: Layer, out: Pixmap, region: RectI) {
        if (!layer.visible) return
        if (layer.isGroup) {
            val buf = Pixmap(region.width, region.height)
            renderStack(layer.children, buf, region)
            compositeBuffer(buf, out, region, BlendMode.Normal, layer.opacity)
        } else {
            layer.surface?.let { compositeSurface(it, out, region, BlendMode.Normal, layer.opacity) }
        }
    }

    fun compositeSurface(surface: TileSurface, out: Pixmap, region: RectI, mode: BlendMode, opacity: Float) {
        if (opacity <= 0f) return
        val r = region.intersect(RectI.wh(out.width, out.height)).intersect(RectI.wh(surface.width, surface.height))
        if (r.isEmpty) return
        val ts = surface.tileSize
        val tx0 = r.left / ts; val tx1 = (r.right - 1) / ts
        val ty0 = r.top / ts; val ty1 = (r.bottom - 1) / ts
        for (ty in ty0..ty1) {
            for (tx in tx0..tx1) {
                val tile = surface.peekTile(tx, ty) ?: continue
                val x0 = maxOf(r.left, tx * ts)
                val x1 = minOf(r.right, tx * ts + ts)
                val y0 = maxOf(r.top, ty * ts)
                val y1 = minOf(r.bottom, ty * ts + ts)
                for (y in y0 until y1) {
                    val tileRow = (y - ty * ts) * ts
                    val outRow = y * out.width
                    for (x in x0 until x1) {
                        val src = tile[tileRow + (x - tx * ts)]
                        if (src == 0) continue
                        val idx = outRow + x
                        out.pixels[idx] = PixelOps.composite(mode, out.pixels[idx], src, 1f, opacity)
                    }
                }
            }
        }
    }

    fun compositeBuffer(buf: Pixmap, out: Pixmap, region: RectI, mode: BlendMode, opacity: Float) {
        if (opacity <= 0f) return
        for (y in 0 until region.height) {
            val oy = region.top + y
            if (oy < 0 || oy >= out.height) continue
            for (x in 0 until region.width) {
                val ox = region.left + x
                if (ox < 0 || ox >= out.width) continue
                val src = buf.pixels[y * region.width + x]
                if (src == 0) continue
                val idx = oy * out.width + ox
                out.pixels[idx] = PixelOps.composite(mode, out.pixels[idx], src, 1f, opacity)
            }
        }
    }

    /** Renders one layer alone onto a fresh pixmap, for thumbnails and previews. */
    fun renderLayerThumbnail(doc: ArtDocument, layer: Layer, maxDim: Int): Pixmap {
        val w = maxDim
        val h = (maxDim.toFloat() * doc.height / doc.width).toInt().coerceAtLeast(1)
        val buf = Pixmap(w, h)
        val sx = doc.width.toFloat() / w
        val sy = doc.height.toFloat() / h
        val surface = layer.surface
        if (surface != null) {
            for (y in 0 until h) {
                for (x in 0 until w) {
                    buf.pixels[y * w + x] = surface.getPixel((x * sx).toInt(), (y * sy).toInt())
                }
            }
        }
        return buf
    }

    /** Renders the document at a reduced size (previews, gallery thumbnails). */
    fun renderPreview(doc: ArtDocument, maxDim: Int, includeBackground: Boolean = true): Pixmap {
        val scale = minOf(maxDim.toFloat() / doc.width, maxDim.toFloat() / doc.height, 1f)
        val w = (doc.width * scale).toInt().coerceAtLeast(1)
        val h = (doc.height * scale).toInt().coerceAtLeast(1)
        val buf = Pixmap(w, h)
        val sx = doc.width.toFloat() / w
        val sy = doc.height.toFloat() / h
        for (y in 0 until h) {
            val cy = ((y + 0.5f) * sy).toInt().coerceIn(0, doc.height - 1)
            for (x in 0 until w) {
                val cx = ((x + 0.5f) * sx).toInt().coerceIn(0, doc.width - 1)
                var acc = 0
                if (includeBackground && !doc.transparentBackground) acc = doc.backgroundColor
                for (layer in doc.layers) {
                    val s = layer.surface ?: continue
                    if (!layer.visible) continue
                    val p = s.getPixel(cx, cy)
                    if (p != 0) acc = PixelOps.composite(layer.blend, acc, p, 1f, layer.opacity)
                }
                buf.pixels[y * w + x] = acc
            }
        }
        return buf
    }

    /** Counts non-transparent pixels of a layer in [region] (used for "is this layer blank" checks). */
    fun countOpaque(surface: TileSurface, region: RectI): Long {
        var n = 0L
        for (k in surface.tilesFor(region)) {
            val c = surface.tileCoords(k)
            val t = surface.peekTile(c[0], c[1]) ?: continue
            for (p in t) if (alphaOf(p) != 0) n++
        }
        return n
    }
}
