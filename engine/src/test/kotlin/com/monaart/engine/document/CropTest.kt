package com.monaart.engine.document

import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.straightToPremul
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Crop semantics: the crop tool gives a rectangle; the document, every layer,
 * the selection and undo state must stay consistent afterwards.
 */
class CropTest {
    private fun doc() = ArtDocument.create("t", 128, 96, transparentBackground = true)

    private fun paintBlock(layer: Layer, rect: RectI, argb: Int) {
        val s = layer.requireSurface()
        for (y in rect.top until rect.bottom) {
            for (x in rect.left until rect.right) {
                s.setPixel(x, y, straightToPremul(argb))
            }
        }
    }

    @Test
    fun `cropping resizes the document and every layer, preserving content`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        paintBlock(layer, RectI(20, 10, 40, 30), 0xFF00FF00.toInt())
        val original = layer.requireSurface().getPixel(25, 15)

        assertTrue(Transforms.crop(d, RectI(16, 8, 64, 40)))
        assertEquals(48, d.width)
        assertEquals(32, d.height)
        // Crop replaces layers, so re-resolve the (cropped) active layer.
        val cropped = requireNotNull(d.activeLayer()).requireSurface()
        assertEquals(48, cropped.width)
        assertEquals(32, cropped.height)
        // Content shifts by the crop's origin.
        assertEquals("crop preserves painted content at its new position", original, cropped.getPixel(25 - 16, 15 - 8))
        // Pixels that lived outside the crop window are gone.
        assertEquals("nothing outside the crop survives", 0, cropped.getPixel(0, 0))
    }

    @Test
    fun `crop rejecting impossible rectangles keeps the document intact`() {
        val d = doc()
        assertFalse("outside the canvas crops nothing", Transforms.crop(d, RectI(200, 200, 400, 400)))
        assertFalse("zero-area crop is refused", Transforms.crop(d, RectI(10, 10, 10, 10)))
        assertEquals(128, d.width)
        assertEquals(96, d.height)
        assertTrue("no history entry for a refused crop", d.history.undoCount == 0)
    }

    @Test
    fun `full-size crop is a no-op`() {
        val d = doc()
        assertFalse(Transforms.crop(d, d.canvasRect))
    }

    @Test
    fun `cropping undoes in one step`() {
        val d = doc()
        paintBlock(requireNotNull(d.activeLayer()), RectI(0, 0, 64, 64), 0xFFFF0000.toInt())
        val undoCountBefore = d.history.undoCount
        assertTrue(Transforms.crop(d, RectI(8, 8, 72, 72)))
        assertEquals(64, d.width)
        assertEquals(64, d.height)
        assertEquals(undoCountBefore + 1, d.history.undoCount)

        d.history.undo()
        assertEquals("undo restores the original canvas size", 128, d.width)
        assertEquals(96, d.height)
        val s = requireNotNull(d.activeLayer()).requireSurface()
        assertEquals(
            "undo restores the painted pixels at the original coordinates", 0xFFFF0000.toInt(),
            straightToPremul(s.getPixel(10, 10)),
        )
        assertTrue(d.history.canRedo)
        d.history.redo()
        assertEquals(64, d.width)
        assertEquals("redo crops again", 0xFFFF0000.toInt(), straightToPremul(requireNotNull(d.activeLayer()).requireSurface().getPixel(2, 2)))
    }

    @Test
    fun `groups and children crop consistently`() {
        val d = doc()
        val base = requireNotNull(d.activeLayer())
        val group = LayerOps.addGroup(d, base)
        val child = LayerOps.addRasterLayer(d, base)
        paintBlock(child, RectI(60, 60, 80, 80), 0xFF0000FF.toInt())
        assertTrue(LayerOps.moveIntoGroup(d, child, group))

        assertTrue(Transforms.crop(d, RectI(48, 48, 128, 96)))
        assertEquals(80, d.width)
        assertEquals(48, d.height)
        val groupLayer = d.layers[1]
        assertTrue(groupLayer.isGroup)
        val childLayer = groupLayer.children[0]
        val cs = requireNotNull(childLayer.surface)
        assertEquals(80, cs.width)
        assertEquals(48, cs.height)
        assertEquals(0xFF0000FF.toInt(), straightToPremul(cs.getPixel(60 - 48, 60 - 48)))
    }
}
