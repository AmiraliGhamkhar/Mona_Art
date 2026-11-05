package com.monaart.engine.document

import com.monaart.engine.history.History
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.RectI
import com.monaart.engine.selection.Selection

/**
 * The editable artwork document: canvas properties, layer tree, selection and
 * undo history. All drawing operations mutate this class; UI never touches
 * pixels directly.
 */
class ArtDocument(
    var id: Long,
    var name: String,
    var width: Int,
    var height: Int,
    var dpi: Int = 72,
) {
    val layers: MutableList<Layer> = mutableListOf()
    var activeLayerId: Long = -1
    var selection: Selection? = null
    /** Canvas background as straight ARGB; alpha 0 means transparent canvas. */
    var backgroundColor: Int = 0xFFFFFFFF.toInt()
    var transparentBackground: Boolean = false
    val history: History = History()

    /** Incremented on any change that affects rendered output. */
    var revision: Long = 0
        private set

    /** Incremented on structural changes (layer add/remove/reorder/properties). */
    var structureRevision: Long = 0
        private set

    private var nextLayerId: Long = 1
    private var nextId: Long = System.nanoTime()

    val canvasRect: RectI get() = RectI.wh(width, height)

    fun bumpRender() { revision++ }
    fun bumpStructure() { revision++; structureRevision++ }

    fun newLayerId(): Long = nextLayerId++

    fun allocateDocumentId(): Long = nextId++

    fun findLayer(id: Long): Layer? {
        fun search(list: List<Layer>): Layer? {
            for (l in list) {
                if (l.id == id) return l
                search(l.children)?.let { return it }
            }
            return null
        }
        return search(layers)
    }

    fun activeLayer(): Layer? = findLayer(activeLayerId)

    /** Parent list containing [layer], or null if not found. */
    fun parentListOf(layer: Layer): MutableList<Layer>? {
        fun search(list: MutableList<Layer>): MutableList<Layer>? {
            if (list.any { it === layer }) return list
            for (l in list) search(l.children)?.let { return it }
            return null
        }
        return search(layers)
    }

    fun indexOfLayer(layer: Layer): Int = parentListOf(layer)?.indexOfFirst { it === layer } ?: -1

    fun addLayer(layer: Layer, above: Layer? = null): Layer {
        val parent = above?.let { parentListOf(it) } ?: layers
        val idx = if (above != null) parent.indexOfFirst { it === above } + 1 else parent.size
        parent.add(idx.coerceIn(0, parent.size), layer)
        if (activeLayerId == -1L) activeLayerId = layer.id
        bumpStructure()
        return layer
    }

    fun removeLayer(layer: Layer): Boolean {
        val parent = parentListOf(layer) ?: return false
        val idx = parent.indexOfFirst { it === layer }
        if (idx < 0) return false
        parent.removeAt(idx)
        if (activeLayerId == layer.id) {
            activeLayerId = parent.getOrNull(idx)?.id
                ?: parent.getOrNull(idx - 1)?.id
                ?: layers.firstOrNull()?.id ?: -1
        }
        bumpStructure()
        return true
    }

    /** Flattened composite at full canvas resolution. */
    fun flatten(): Pixmap {
        val out = Pixmap(width, height)
        Compositor().render(this, out, canvasRect, includeBackground = true)
        return out
    }

    /** Small preview of the flattened document. */
    fun preview(maxDim: Int): Pixmap = flatten().apply { }.thumbnail(maxDim)

    companion object {
        fun create(
            name: String,
            width: Int,
            height: Int,
            dpi: Int = 72,
            background: Int = 0xFFFFFFFF.toInt(),
            transparentBackground: Boolean = false,
        ): ArtDocument {
            val doc = ArtDocument(0, name, width, height, dpi)
            doc.backgroundColor = background
            doc.transparentBackground = transparentBackground
            val base = Layer(doc.newLayerId(), "Layer 1", TileSurface(width, height))
            doc.layers.add(base)
            doc.activeLayerId = base.id
            doc.bumpStructure()
            return doc
        }
    }
}

/** Utility to build a filled layer (e.g. background fill / bucket fill). */
fun Layer.fillWith(color: Int) {
    requireSurface().fill(color)
}
