package com.monaart.engine.filter

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.PixelEdit
import com.monaart.engine.document.TileSurface
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.premulToStraight
import com.monaart.engine.paint.straightToPremul
import com.monaart.engine.selection.Selection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for the filter preview contract: previews never destroy the
 * undo/redo history, repeated parameter changes never compound destructive
 * edits, cancel restores the exact original pixels, and apply is exactly one
 * undoable step — exactly the lifecycle the editor's FilterPreview flow uses.
 */
class FiltersTest {
    private fun doc(w: Int = 96, h: Int = 96) = ArtDocument.create("t", w, h, transparentBackground = true)

    private fun paintBlock(d: ArtDocument, surfaceRect: RectI, argb: Int) {
        val layer = requireNotNull(d.activeLayer())
        val s = layer.requireSurface()
        assertTrue(
            PixelEdit.run(d, "Paint stroke", surfaceRect) {
                for (y in surfaceRect.top until surfaceRect.bottom) {
                    for (x in surfaceRect.left until surfaceRect.right) {
                        s.setPixel(x, y, straightToPremul(argb))
                    }
                }
            },
        )
    }

    private fun snapshot(surface: TileSurface, r: RectI): Pair<RectI, IntArray> {
        val out = IntArray(r.width * r.height)
        surface.copyRect(r, out)
        return r to out
    }

    private fun restore(surface: TileSurface, backup: Pair<RectI, IntArray>) {
        surface.writeRect(backup.first, backup.second.copyOf())
    }

    @Test
    fun `a preview pass leaves the undo history untouched`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        paintBlock(d, RectI(10, 10, 40, 40), 0xFF00FF00.toInt())
        val historyDepth = d.history.undoCount
        assertEquals(1, historyDepth)

        Filters.apply(d, layer, FilterKind.BrightnessContrast, 0.5f, recordHistory = false)
        assertEquals("a preview must not create a history entry", historyDepth, d.history.undoCount)

        // Repeated slider changes do not accumulate into the history either.
        (0..4).forEach { i ->
            Filters.apply(d, layer, FilterKind.BrightnessContrast, 0.25f * (i + 1), recordHistory = false)
        }
        assertEquals("no amount of preview passes changes the history", historyDepth, d.history.undoCount)
        assertFalse(d.history.canRedo)
    }

    @Test
    fun `restoring the preview backup restores the exact original pixels`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        val surface = layer.requireSurface()
        paintBlock(d, RectI(10, 10, 40, 40), 0xFF00FF00.toInt())
        val original = surface.getPixel(20, 20)

        val backup = snapshot(surface, RectI.wh(d.width, d.height))
        Filters.apply(d, layer, FilterKind.Invert, 0f, recordHistory = false)
        assertEquals("invert changed the opaque pixel", 0xFFFF00FF.toInt(), premulToStraight(surface.getPixel(20, 20)))

        restore(surface, backup)
        assertEquals("cancel must restore the exact pre-preview pixels", original, surface.getPixel(20, 20))
    }

    @Test
    fun `apply after a preview is exactly one undo step back to the original`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        val surface = layer.requireSurface()
        paintBlock(d, RectI(10, 10, 40, 40), 0xFF00FF00.toInt())
        val original = surface.getPixel(20, 20)
        val strokeUndoCount = d.history.undoCount

        val backup = snapshot(surface, RectI.wh(d.width, d.height))
        // Live preview passes at several amounts, as the slider moves.
        for (a in listOf(0.2f, 0.6f, 1.0f)) {
            restore(surface, backup)
            Filters.apply(d, layer, FilterKind.BrightnessContrast, a, recordHistory = false)
        }
        // Committed apply: same restore first, then record.
        restore(surface, backup)
        assertTrue(Filters.apply(d, layer, FilterKind.BrightnessContrast, 1.0f, recordHistory = true))

        assertEquals("apply adds exactly one history step", strokeUndoCount + 1, d.history.undoCount)
        assertTrue("the earlier stroke survives the preview lifecycle", d.history.canUndo)
        d.history.undo()
        assertEquals("one undo returns the layer to the pre-filter pixels", original, surface.getPixel(20, 20))
        assertEquals("one undo returns to the stroke-only history", strokeUndoCount, d.history.undoCount)
    }

    @Test
    fun `a committed filter respects the selection`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        val surface = layer.requireSurface()
        paintBlock(d, RectI(0, 0, d.width, d.height), 0xFF112233.toInt())
        d.selection = Selection.rectangle(d.width, d.height, RectI(0, 0, 48, d.height))
        assertTrue(Filters.apply(d, layer, FilterKind.Invert, 0f, recordHistory = true))
        assertEquals(
            "pixels inside the selection invert", 0xFFEEDDCC.toInt(),
            premulToStraight(
                surface.getPixel(10, 10),
            ),
        )
        assertEquals(
            "pixels outside the selection are unchanged", 0xFF112233.toInt(),
            premulToStraight(
                surface.getPixel(60, 10),
            ),
        )
    }
}
