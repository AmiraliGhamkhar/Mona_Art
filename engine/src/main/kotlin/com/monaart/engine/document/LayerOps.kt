package com.monaart.engine.document

import com.monaart.engine.history.History
import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.PixelOps
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.RectI

/**
 * All layer-tree mutations live here so the presentation layer stays free of
 * document logic and every operation is undoable.
 */
object LayerOps {

    fun addRasterLayer(doc: ArtDocument, above: Layer? = doc.activeLayer(), name: String? = null): Layer {
        val layer = Layer(doc.newLayerId(), name ?: nextName(doc), TileSurface(doc.width, doc.height))
        doc.addLayer(layer, above)
        doc.activeLayerId = layer.id
        doc.history.push(simpleAdd(doc, layer))
        return layer
    }

    fun addGroup(doc: ArtDocument, above: Layer? = doc.activeLayer()): Layer {
        val layer = Layer(doc.newLayerId(), nextName(doc, "Group"), null, LayerKind.GROUP)
        doc.addLayer(layer, above)
        doc.activeLayerId = layer.id
        doc.history.push(simpleAdd(doc, layer))
        return layer
    }

    private fun nextName(doc: ArtDocument, prefix: String = "Layer"): String {
        var n = doc.layers.size + 1
        val used = collectNames(doc.layers)
        while (used.contains("$prefix $n")) n++
        return "$prefix $n"
    }

    private fun collectNames(list: List<Layer>, acc: MutableSet<String> = HashSet()): Set<String> {
        for (l in list) { acc.add(l.name); collectNames(l.children, acc) }
        return acc
    }

    private fun simpleAdd(doc: ArtDocument, layer: Layer): com.monaart.engine.history.DocCommand = History.Lambda(
        label = "Add ${layer.name}",
        onUndo = { doc.removeLayer(layer); doc.bumpStructure() },
        onRedo = { doc.addLayer(layer); doc.activeLayerId = layer.id; doc.bumpStructure() },
    )

    fun deleteLayer(doc: ArtDocument, layer: Layer): Boolean {
        val parent = doc.parentListOf(layer) ?: return false
        val idx = parent.indexOfFirst { it === layer }
        if (idx < 0) return false
        if (doc.layers.size == 1 && doc.layers[0] === layer) return false // never delete the last layer
        parent.removeAt(idx)
        val prevActive = doc.activeLayerId
        doc.activeLayerId = parent.getOrNull(idx)?.id ?: parent.getOrNull(idx - 1)?.id
            ?: doc.layers.firstOrNull()?.id ?: -1
        doc.bumpStructure()
        doc.history.push(History.Lambda(
            label = "Delete ${layer.name}",
            onUndo = { parent.add(idx.coerceIn(0, parent.size), layer); doc.activeLayerId = prevActive; doc.bumpStructure() },
            onRedo = { parent.remove(layer); doc.bumpStructure() },
            bytes = { layer.pixelBytes() },
        ))
        return true
    }

    fun duplicateLayer(doc: ArtDocument, layer: Layer): Layer {
        val copy = layer.deepCopy(doc.newLayerId()) { doc.newLayerId() }
        copy.name = "${layer.name} copy"
        doc.addLayer(copy, layer)
        doc.activeLayerId = copy.id
        doc.history.push(simpleAdd(doc, copy))
        return copy
    }

    fun renameLayer(doc: ArtDocument, layer: Layer, newName: String): Boolean {
        val old = layer.name
        if (old == newName) return false
        layer.name = newName
        doc.bumpStructure()
        doc.history.push(History.Lambda("Rename layer",
            onUndo = { layer.name = old; doc.bumpStructure() },
            onRedo = { layer.name = newName; doc.bumpStructure() }))
        return true
    }

    fun setOpacity(doc: ArtDocument, layer: Layer, opacity: Float): Boolean {
        val old = layer.opacity
        val v = opacity.coerceIn(0f, 1f)
        if (old == v) return false
        layer.opacity = v
        doc.bumpStructure()
        doc.history.push(History.Lambda("Layer opacity",
            onUndo = { layer.opacity = old; doc.bumpStructure() },
            onRedo = { layer.opacity = v; doc.bumpStructure() }))
        return true
    }

    fun setBlend(doc: ArtDocument, layer: Layer, mode: BlendMode): Boolean {
        val old = layer.blend
        if (old == mode) return false
        layer.blend = mode
        doc.bumpStructure()
        doc.history.push(History.Lambda("Blend mode",
            onUndo = { layer.blend = old; doc.bumpStructure() },
            onRedo = { layer.blend = mode; doc.bumpStructure() }))
        return true
    }

    fun setVisible(doc: ArtDocument, layer: Layer, visible: Boolean): Boolean {
        val old = layer.visible
        if (old == visible) return false
        layer.visible = visible
        doc.bumpStructure()
        doc.history.push(History.Lambda(if (visible) "Show layer" else "Hide layer",
            onUndo = { layer.visible = old; doc.bumpStructure() },
            onRedo = { layer.visible = visible; doc.bumpStructure() }))
        return true
    }

    fun setLocked(doc: ArtDocument, layer: Layer, locked: Boolean) {
        layer.locked = locked
        doc.bumpStructure()
        doc.history.push(History.Lambda(if (locked) "Lock layer" else "Unlock layer",
            onUndo = { layer.locked = !locked; doc.bumpStructure() },
            onRedo = { layer.locked = locked; doc.bumpStructure() }))
    }

    fun setAlphaLocked(doc: ArtDocument, layer: Layer, locked: Boolean) {
        layer.alphaLocked = locked
        doc.bumpStructure()
        doc.history.push(History.Lambda(if (locked) "Alpha lock" else "Unlock alpha",
            onUndo = { layer.alphaLocked = !locked; doc.bumpStructure() },
            onRedo = { layer.alphaLocked = locked; doc.bumpStructure() }))
    }

    fun setClipping(doc: ArtDocument, layer: Layer, clipping: Boolean) {
        layer.clipping = clipping
        doc.bumpStructure()
        doc.history.push(History.Lambda(if (clipping) "Clip layer" else "Unclip layer",
            onUndo = { layer.clipping = !clipping; doc.bumpStructure() },
            onRedo = { layer.clipping = clipping; doc.bumpStructure() }))
    }

    /** Moves [layer] by [delta] positions within its parent list. */
    fun moveLayer(doc: ArtDocument, layer: Layer, delta: Int): Boolean {
        val parent = doc.parentListOf(layer) ?: return false
        val from = parent.indexOfFirst { it === layer }
        val to = (from + delta).coerceIn(0, parent.size - 1)
        if (from == to) return false
        parent.removeAt(from)
        parent.add(to, layer)
        doc.bumpStructure()
        doc.history.push(History.Lambda("Reorder layer",
            onUndo = { parent.remove(layer); parent.add(from, layer); doc.bumpStructure() },
            onRedo = { parent.remove(layer); parent.add(to, layer); doc.bumpStructure() }))
        return true
    }

    /** Moves a layer into [group] (placeAtEnd=true appends on top). */
    fun moveIntoGroup(doc: ArtDocument, layer: Layer, group: Layer, placeAtEnd: Boolean = true): Boolean {
        if (!group.isGroup || layer === group) return false
        val from = doc.parentListOf(layer) ?: return false
        val fromIdx = from.indexOfFirst { it === layer }
        if (from === group.children && placeAtEnd) return false
        from.removeAt(fromIdx)
        val toIdx = if (placeAtEnd) group.children.size else 0
        group.children.add(toIdx, layer)
        doc.bumpStructure()
        doc.history.push(History.Lambda("Move into group",
            onUndo = { group.children.remove(layer); from.add(fromIdx, layer); doc.bumpStructure() },
            onRedo = { from.remove(layer); group.children.add(toIdx.coerceIn(0, group.children.size), layer); doc.bumpStructure() }))
        return true
    }

    fun mergeDown(doc: ArtDocument, layer: Layer): Boolean {
        val parent = doc.parentListOf(layer) ?: return false
        val idx = parent.indexOfFirst { it === layer }
        if (idx <= 0) return false
        val below = parent[idx - 1]
        if (below.isGroup) return false
        val before = below.surface!!.snapshot(doc.canvasRect)
        val merged = Pixmap(doc.width, doc.height)
        val comp = Compositor()
        comp.renderLayerIsolated(below, merged, doc.canvasRect)
        comp.renderLayerIsolated(layer, merged, doc.canvasRect)
        val target = below.surface
        target.clear()
        for (y in 0 until doc.height) for (x in 0 until doc.width) {
            val p = merged.pixels[y * doc.width + x]
            if (p != 0) target.setPixel(x, y, p)
        }
        below.opacity = 1f
        below.blend = BlendMode.Normal
        target.touch()
        parent.removeAt(idx)
        doc.activeLayerId = below.id
        doc.bumpStructure()
        val after = target.snapshot(doc.canvasRect)
        doc.history.push(History.Lambda("Merge down",
            onUndo = {
                target.restore(before)
                parent.add(idx, layer)
                doc.activeLayerId = layer.id
                doc.bumpStructure()
            },
            onRedo = {
                target.restore(after)
                parent.remove(layer)
                doc.activeLayerId = below.id
                doc.bumpStructure()
            },
            bytes = { before.byteSize() + after.byteSize() },
        ))
        return true
    }

    /** Flattens every layer into a single raster layer. */
    fun flattenAll(doc: ArtDocument): Boolean {
        if (doc.layers.size == 1 && !doc.layers[0].isGroup) return false
        val flat = doc.flatten()
        val oldLayers = doc.layers.toList()
        val oldActive = doc.activeLayerId
        doc.layers.clear()
        val layer = Layer(doc.newLayerId(), "Flattened", TileSurface(doc.width, doc.height))
        for (y in 0 until doc.height) for (x in 0 until doc.width) {
            val p = flat.pixels[y * doc.width + x]
            if (p != 0) layer.surface!!.setPixel(x, y, p)
        }
        doc.layers.add(layer)
        doc.activeLayerId = layer.id
        doc.bumpStructure()
        doc.history.push(History.Lambda("Flatten image",
            onUndo = { doc.layers.clear(); doc.layers.addAll(oldLayers); doc.activeLayerId = oldActive; doc.bumpStructure() },
            onRedo = { doc.layers.clear(); doc.layers.add(layer); doc.activeLayerId = layer.id; doc.bumpStructure() },
            bytes = { layer.pixelBytes() },
        ))
        return true
    }

    /** Fills a layer (or the current selection) with a straight ARGB color. */
    fun fillLayer(doc: ArtDocument, layer: Layer, color: Int, useSelection: Boolean = true): Boolean {
        val s = layer.surface ?: return false
        val sel = if (useSelection) doc.selection else null
        val region = sel?.bounds?.intersect(doc.canvasRect) ?: doc.canvasRect
        if (region.isEmpty) return false
        val edit = PixelEdit(doc, "Fill", layer)
        edit.touchRect(region)
        val premul = com.monaart.engine.paint.straightToPremul(color)
        if (sel == null) {
            for (k in s.tilesFor(region)) {
                val c = s.tileCoords(k)
                val t = s.obtainTile(c[0], c[1])
                val tx0 = c[0] * s.tileSize; val ty0 = c[1] * s.tileSize
                for (y in 0 until s.tileSize) {
                    val gy = ty0 + y
                    if (gy < region.top || gy >= region.bottom) continue
                    for (x in 0 until s.tileSize) {
                        val gx = tx0 + x
                        if (gx < region.left || gx >= region.right) continue
                        t[y * s.tileSize + x] = if (layer.alphaLocked) PixelOps.over(t[y * s.tileSize + x], com.monaart.engine.paint.PixelOps.scaleAlpha(premul, com.monaart.engine.paint.alphaOf(t[y*s.tileSize+x])/255f)) else premul
                    }
                }
            }
        } else {
            for (y in region.top until region.bottom) for (x in region.left until region.right) {
                val cov = sel.coverage(x, y)
                if (cov <= 0f) continue
                val cur = s.getPixel(x, y)
                if (layer.alphaLocked && com.monaart.engine.paint.alphaOf(cur) == 0) continue
                s.setPixel(x, y, PixelOps.over(cur, com.monaart.engine.paint.PixelOps.scaleAlpha(premul, cov)))
            }
        }
        s.touch()
        doc.bumpRender()
        return edit.commit()
    }

    fun clearLayerRegion(doc: ArtDocument, layer: Layer, region: RectI = doc.canvasRect, label: String = "Clear"): Boolean {
        val s = layer.surface ?: return false
        val edit = PixelEdit(doc, label, layer)
        edit.touchRect(region)
        s.forEachPixelIn(region) { x, y -> s.setPixel(x, y, 0) }
        s.touch()
        return edit.commit()
    }
}
