package com.monaart.engine.brush

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.paint.alphaOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pixel-level tests for mirrored (symmetry) painting: the mirrored half must be
 * a genuine reflection, pressure must transfer, and a stroke plus its mirror
 * must never double-paint the alpha of any pixel near the axis.
 */
class SymmetryTest {

    private fun doc(w: Int = 128, h: Int = 128) = ArtDocument.create("t", w, h, transparentBackground = true)

    /**
     * Partial flow so pass accumulation is observable rather than saturated, and
     * every shaping knob neutralised so coverage is a plain round dab.
     */
    private fun spec() = BrushPresets.default.copy(
        size = 8f, flow = 0.35f, spacing = 0.05f, opacity = 1f, hardness = 1f,
        taperMode = TaperMode.NONE, stabilization = 0f, velocitySize = 0f,
        scatter = 0f, sizeJitter = 0f, rotationJitter = 0f,
        texture = null, grainStrength = 0f, wetness = 0f,
        pressureSize = 0f, pressureOpacity = 0f, pressureFlow = 0f,
        roundness = 1f, angle = 0f,
    )

    private fun paint(
        d: ArtDocument,
        points: List<StrokePoint>,
        axes: List<BrushEngine.Axis> = emptyList(),
        spec: BrushSpec = spec(),
    ): com.monaart.engine.document.Layer {
        val layer = requireNotNull(d.activeLayer())
        val engine = BrushEngine()
        engine.begin(d, layer, spec, COLOR, seed = 7, axes = axes)
        points.forEach { engine.move(it) }
        engine.end()
        return layer
    }

    private fun pressureSpec() = spec().copy(
        flow = 1f,
        pressureSize = 1.0f,
        pressureOpacity = 0f,
    )

    @Test
    fun `a vertical axis mirrors strokes across the canvas centre`() {
        val d = doc()
        paint(d, listOf(StrokePoint(30f, 64f), StrokePoint(40f, 64f)), listOf(V_AXIS))
        val s = requireNotNull(d.activeLayer()).requireSurface()

        val left = alphaOf(s.getPixel(29, 64))
        val right = alphaOf(s.getPixel(98, 64)) // 127 - 29
        assertTrue("the primary side must paint", left > 0)
        assertEquals(
            "the mirrored side must be a true reflection with equal coverage",
            left,
            right,
        )
    }

    @Test
    fun `a horizontal axis mirrors strokes down the canvas centre`() {
        val d = doc()
        paint(d, listOf(StrokePoint(64f, 30f), StrokePoint(64f, 42f)), listOf(H_AXIS))
        val s = requireNotNull(d.activeLayer()).requireSurface()
        val top = alphaOf(s.getPixel(64, 29))
        val bottom = alphaOf(s.getPixel(64, 98)) // 127 - 29
        assertTrue("the top half must paint", top > 0)
        assertEquals("the bottom half must be the reflection", top, bottom)
    }

    @Test
    fun `both axes paint the diagonal mirror quadrants`() {
        val d = doc()
        paint(
            d,
            listOf(StrokePoint(30f, 32f), StrokePoint(38f, 36f)),
            listOf(V_AXIS, H_AXIS),
        )
        val s = requireNotNull(d.activeLayer()).requireSurface()
        val quadrants = listOf(
            alphaOf(s.getPixel(29, 32)),   // primary
            alphaOf(s.getPixel(98, 32)),   // vertical mirror
            alphaOf(s.getPixel(29, 95)),   // horizontal mirror
            alphaOf(s.getPixel(98, 95)),   // diagonal mirror
        )
        quadrants.forEachIndexed { i, a ->
            assertTrue("quadrant ${i + 1} must paint, got 0", a > 0)
        }
    }

    @Test
    fun `pressure transfers to the mirrored stroke`() {
        // A light-pressure and a heavy-pressure dab mirror symmetrically: total
        // mirrored coverage must grow with pressure exactly like the original.
        fun mirroredCoverage(pressure: Float): Int {
            val d = doc()
            val layer = requireNotNull(d.activeLayer())
            val engine = BrushEngine()
            engine.begin(d, layer, pressureSpec(), COLOR, seed = 7, axes = listOf(V_AXIS))
            engine.move(StrokePoint(34f, 64f, pressure = pressure))
            engine.end()
            val s = layer.requireSurface()
            return (64 until 128).sumOf { alphaOf(s.getPixel(it, 64)) }
        }
        val light = mirroredCoverage(0.1f)
        val heavy = mirroredCoverage(1f)
        assertTrue(
            "mirrored coverage must respond to pressure ($light vs $heavy)",
            heavy > light,
        )
    }

    /**
     * Mirrored painting must not double-paint pixels: a mirrored run's right
     * half equals an unmirrored run of the same path exactly, and its left half
     * is the reflection of that. Overlapping strokes would accumulate more alpha.
     */
    @Test
    fun `no pixel accumulates mirrored coverage twice`() {
        val mirrored = doc()
        paint(mirrored, listOf(StrokePoint(66f, 64f), StrokePoint(63f, 64f)), listOf(V_AXIS))
        val plain = doc()
        paint(plain, listOf(StrokePoint(66f, 64f), StrokePoint(63f, 64f)))

        val ms = requireNotNull(mirrored.activeLayer()).requireSurface()
        val ps = requireNotNull(plain.activeLayer()).requireSurface()
        // Right half: the mirror must place exactly one stroke's worth of ink at
        // the mirrored location, i.e. pixel x carries what an unmirrored stroke
        // left at its reflection (127 - x).
        for (x in 64 until 128) {
            assertEquals(
                "right-half pixel x=$x must mirror the unmirrored stroke's ink",
                alphaOf(ps.getPixel(127 - x, 64)),
                alphaOf(ms.getPixel(x, 64)),
            )
        }
        // Left half of the mirrored run is the reflection of its right half.
        for (x in 0 until 64) {
            assertEquals(
                "mirror reflection must hold for x=$x",
                alphaOf(ms.getPixel(x, 64)),
                alphaOf(ms.getPixel(127 - x, 64)),
            )
        }
    }

    @Test
    fun `a mirrored gesture is a single undo step`() {
        val d = doc()
        val before = d.history.undoCount
        paint(
            d,
            listOf(StrokePoint(30f, 64f), StrokePoint(50f, 64f), StrokePoint(60f, 64f)),
            listOf(V_AXIS, H_AXIS),
            BrushPresets.default.copy(size = 8f, flow = 1f, spacing = 0.05f, opacity = 1f),
        )
        assertEquals("one gesture, together with its mirrors, is one history entry", before + 1, d.history.undoCount)
    }

    @Test
    fun `cancelling a mirrored stroke rolls everything back`() {
        val d = doc()
        val layer = requireNotNull(d.activeLayer())
        val engine = BrushEngine()
        engine.begin(d, layer, spec(), COLOR, seed = 7, axes = listOf(V_AXIS))
        engine.move(StrokePoint(30f, 64f))
        engine.move(StrokePoint(40f, 64f))
        engine.cancel()
        val s = layer.requireSurface()
        assertEquals("cancel removes the stroke", 0, alphaOf(s.getPixel(29, 64)))
        assertEquals("cancel removes the mirror", 0, alphaOf(s.getPixel(98, 64)))
        assertTrue("cancel leaves no history entry", d.history.undoCount == 0)
    }

    companion object {
        private val COLOR = 0xFFFF0000.toInt()
        private val V_AXIS = BrushEngine.Axis(vertical = true, pivot = 64f)
        private val H_AXIS = BrushEngine.Axis(vertical = false, pivot = 64f)
    }
}
