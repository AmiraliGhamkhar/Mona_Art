package com.monaart.engine.document

import com.monaart.engine.history.History
import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.straightToPremul
import com.monaart.engine.selection.Selection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Layer semantics, compositing, selections and undo.
 *
 * Pixel reads go through [TileSurface.getPixel], which returns premultiplied
 * ARGB. Fully opaque pixels are therefore directly comparable to `0xAARRGGBB`
 * literals, and transparent ones read as 0.
 */
class DocumentTest {

    private fun doc(w: Int = 128, h: Int = 128) = ArtDocument.create("t", w, h, transparentBackground = true)

    private fun opaque(argb: Int): Int = straightToPremul(argb)

    private fun fill(layer: Layer, argb: Int, bounds: RectI) {
        val surface = layer.requireSurface()
        for (y in bounds.top until bounds.bottom) {
            for (x in bounds.left until bounds.right) {
                surface.setPixel(x, y, opaque(argb))
            }
        }
    }

    private fun newLayer(doc: ArtDocument, name: String): Layer =
        LayerOps.addRasterLayer(doc, doc.activeLayer(), name)

    @Test
    fun `compositor stacks visible layers in order`() {
        val d = doc()
        fill(requireNotNull(d.activeLayer()), 0xFFFF0000.toInt(), d.canvasRect)
        val top = newLayer(d, "top")
        fill(top, 0xFF0000FF.toInt(), d.canvasRect)

        val out = Pixmap(d.width, d.height)
        Compositor().render(d, out, d.canvasRect, includeBackground = false)
        assertEquals("the top layer wins where it is opaque", 0xFF0000FF.toInt(), out.get(64, 64))
    }

    @Test
    fun `hidden layers are excluded from the composite`() {
        val d = doc()
        fill(requireNotNull(d.activeLayer()), 0xFFFF0000.toInt(), d.canvasRect)
        val top = newLayer(d, "top")
        fill(top, 0xFF00FF00.toInt(), d.canvasRect)
        top.visible = false

        val out = Pixmap(d.width, d.height)
        Compositor().render(d, out, d.canvasRect, includeBackground = false)
        assertEquals(0xFFFF0000.toInt(), out.get(10, 10))
    }

    @Test
    fun `multiply blend darkens`() {
        val d = doc()
        fill(requireNotNull(d.activeLayer()), 0xFF808080.toInt(), d.canvasRect)
        val top = newLayer(d, "top")
        top.blend = BlendMode.Multiply
        fill(top, 0xFF808080.toInt(), d.canvasRect)

        val out = Pixmap(d.width, d.height)
        Compositor().render(d, out, d.canvasRect, includeBackground = false)
        val red = (out.get(32, 32) shr 16) and 0xFF
        // 0x80 * 0x80 / 0xFF ≈ 0x40
        assertTrue("multiply of 0x80 over 0x80 must darken, got $red", red in 0x38..0x48)
    }

    @Test
    fun `screen blend lightens`() {
        val d = doc()
        fill(requireNotNull(d.activeLayer()), 0xFF404040.toInt(), d.canvasRect)
        val top = newLayer(d, "top")
        top.blend = BlendMode.Screen
        fill(top, 0xFF404040.toInt(), d.canvasRect)

        val out = Pixmap(d.width, d.height)
        Compositor().render(d, out, d.canvasRect, includeBackground = false)
        val red = (out.get(32, 32) shr 16) and 0xFF
        assertTrue("screen must lighten 0x40, got $red", red > 0x60)
    }

    @Test
    fun `clipping mask limits a layer to the base alpha`() {
        val d = doc()
        val base = requireNotNull(d.activeLayer())
        fill(base, 0xFF000000.toInt(), RectI(0, 0, 64, 128)) // left half only
        val clipped = newLayer(d, "clipped")
        clipped.clipping = true
        fill(clipped, 0xFFFF0000.toInt(), d.canvasRect)

        val out = Pixmap(d.width, d.height)
        Compositor().render(d, out, d.canvasRect, includeBackground = false)
        val leftRed = (out.get(20, 64) shr 16) and 0xFF
        assertTrue("clipped red must show over the base, got $leftRed", leftRed > 100)
        assertEquals("clipped layer must be invisible where the base is transparent", 0, out.get(100, 64))
    }

    @Test
    fun `alpha lock confines a fill to existing coverage`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        fill(layer, 0xFF112233.toInt(), RectI(0, 0, 32, 32))
        layer.alphaLocked = true
        LayerOps.fillLayer(d, layer, 0xFFFF0000.toInt(), useSelection = false)

        val surface = layer.requireSurface()
        assertEquals("inside coverage the fill applies", 0xFFFF0000.toInt(), surface.getPixel(10, 10))
        assertEquals("outside coverage nothing is written", 0, surface.getPixel(100, 100))
    }

    @Test
    fun `undo restores the previous pixels and redo reapplies them`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        val surface = layer.requireSurface()

        PixelEdit.run(d, "Paint", RectI(0, 0, 40, 20)) {
            for (x in 0 until 40) surface.setPixel(x, 10, opaque(0xFF000000.toInt()))
        }
        val painted = surface.getPixel(20, 10)
        assertNotEquals(0, painted)
        assertTrue("the edit must be recorded", d.history.canUndo)

        d.history.undo()
        assertEquals("undo must clear the painted pixels", 0, surface.getPixel(20, 10))

        assertTrue(d.history.canRedo)
        d.history.redo()
        assertEquals(painted, surface.getPixel(20, 10))
    }

    @Test
    fun `history depth is bounded`() {
        val d = doc()
        val history = History(maxSteps = 3)
        repeat(8) { i ->
            history.push(
                History.Lambda("step $i", onUndo = {}, onRedo = {}, bytes = { 1L }),
            )
        }
        assertEquals(3, history.undoCount)
    }

    @Test
    fun `merge down keeps the composite`() {
        val d = doc()
        fill(requireNotNull(d.activeLayer()), 0xFFFF0000.toInt(), d.canvasRect)
        val top = newLayer(d, "top")
        fill(top, 0xFF0000FF.toInt(), RectI(0, 0, 64, 64))

        assertTrue(LayerOps.mergeDown(d, top))
        val out = Pixmap(d.width, d.height)
        Compositor().render(d, out, d.canvasRect, includeBackground = false)
        assertEquals("the blue region survives the merge", 0xFF0000FF.toInt(), out.get(10, 10))
        assertEquals("the red region survives the merge", 0xFFFF0000.toInt(), out.get(100, 100))
        assertEquals(1, d.layers.size)
    }

    @Test
    fun `flatten produces a single raster layer`() {
        val d = doc()
        fill(requireNotNull(d.activeLayer()), 0xFFFF0000.toInt(), d.canvasRect)
        newLayer(d, "second")
        assertTrue(LayerOps.flattenAll(d))
        assertEquals(1, d.layers.size)
        val out = Pixmap(d.width, d.height)
        Compositor().render(d, out, d.canvasRect, includeBackground = false)
        assertEquals(0xFFFF0000.toInt(), out.get(5, 5))
    }

    @Test
    fun `selection restricts a fill to its mask`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        d.selection = Selection.rectangle(d.width, d.height, RectI(0, 0, 64, 128))
        LayerOps.fillLayer(d, layer, 0xFF00FF00.toInt())

        val surface = layer.requireSurface()
        assertEquals(0xFF00FF00.toInt(), surface.getPixel(10, 10))
        assertEquals("outside the selection must stay transparent", 0, surface.getPixel(100, 10))
    }

    @Test
    fun `deleting the last layer is refused`() {
        val d = doc()
        val only = requireNotNull(d.activeLayer())
        assertFalse(LayerOps.deleteLayer(d, only))
        assertTrue("the document must always keep one layer", d.layers.isNotEmpty())
    }

    @Test
    fun `group visibility hides its children`() {
        val d = doc()
        val group = LayerOps.addGroup(d, d.activeLayer())
        fill(requireNotNull(d.activeLayer()), 0xFFFF0000.toInt(), d.canvasRect)
        val child = newLayer(d, "child")
        fill(child, 0xFF0000FF.toInt(), d.canvasRect)
        LayerOps.moveIntoGroup(d, child, group)

        val visible = Pixmap(d.width, d.height)
        Compositor().render(d, visible, d.canvasRect, includeBackground = false)
        assertEquals(0xFF0000FF.toInt(), visible.get(32, 32))

        group.visible = false
        val hidden = Pixmap(d.width, d.height)
        Compositor().render(d, hidden, d.canvasRect, includeBackground = false)
        assertEquals("hiding the group must hide its children", 0xFFFF0000.toInt(), hidden.get(32, 32))
    }
}
