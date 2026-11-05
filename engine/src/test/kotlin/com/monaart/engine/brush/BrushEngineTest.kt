package com.monaart.engine.brush

import com.monaart.engine.document.ArtDocument
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavioural contract of the brush engine. These tests assert observable
 * pixel output, not internal state, so they keep holding if the rasteriser is
 * optimised or replaced.
 */
class BrushEngineTest {

    private fun document(width: Int = 256, height: Int = 256) =
        ArtDocument.create("test", width, height, transparentBackground = true)

    private fun stroke(
        doc: ArtDocument,
        spec: BrushSpec,
        points: List<StrokePoint>,
        color: Int = 0xFF000000.toInt(),
    ) {
        val layer = requireNotNull(doc.activeLayer())
        val engine = BrushEngine()
        engine.begin(doc, layer, spec, color, seed = 7)
        points.forEachIndexed { index, p ->
            engine.move(p)
            if (index % 8 == 0) engine.flush()
        }
        engine.end()
    }

    @Test
    fun `a stroke deposits opaque pixels along its path`() {
        val doc = document()
        stroke(
            doc,
            BrushPresets.default.copy(size = 12f, opacity = 1f, flow = 1f, spacing = 0.1f),
            (0..20).map { StrokePoint(20f + it * 10f, 128f, 1f) },
        )
        val layer = requireNotNull(doc.activeLayer())
        var opaque = 0
        for (x in 0 until doc.width) {
            if (layer.alphaAt(x, 128) > 200) opaque++
        }
        assertTrue("expected a continuous horizontal stroke, found $opaque px", opaque > 150)
    }

    @Test
    fun `pressure changes stroke width`() {
        fun widthAtPressure(pressure: Float): Int {
            val doc = document()
            stroke(
                doc,
                BrushPresets.default.copy(size = 40f, pressureSize = 1f, hardness = 1f),
                (0..30).map { StrokePoint(128f, 20f + it * 4f, pressure) },
            )
            val layer = requireNotNull(doc.activeLayer())
            var covered = 0
            for (y in 0 until doc.height) if (layer.alphaAt(128, y) > 32) covered++
            return covered
        }
        val light = widthAtPressure(0.15f)
        val heavy = widthAtPressure(1f)
        assertTrue("light=$light heavy=$heavy: heavy pressure must paint more pixels", heavy > light)
    }

    @Test
    fun `sparse samples are interpolated without gaps`() {
        val doc = document()
        // 10 px jumps with a 6 px brush: without interpolation the stroke would break up.
        stroke(
            doc,
            BrushPresets.default.copy(size = 6f, spacing = 0.15f),
            listOf(StrokePoint(20f, 128f, 1f), StrokePoint(120f, 128f, 1f)),
        )
        val layer = requireNotNull(doc.activeLayer())
        val gaps = (25 until 115).count { x -> layer.alphaAt(x, 128) < 32 }
        assertEquals("stroke must not contain gaps", 0, gaps)
    }

    @Test
    fun `eraser removes previously painted pixels`() {
        val doc = document()
        val layer = requireNotNull(doc.activeLayer())
        stroke(doc, BrushPresets.eraserSized(20f), listOf(StrokePoint(20f, 128f, 1f), StrokePoint(230f, 128f, 1f)))
        val before = layer.alphaAt(128, 128)
        stroke(
            doc,
            BrushSpec(kind = BrushKind.ERASER, size = 30f, hardness = 1f),
            listOf(StrokePoint(128f, 100f, 1f), StrokePoint(128f, 160f, 1f)),
        )
        val after = layer.alphaAt(128, 128)
        assertTrue("before=$before after=$after: eraser must reduce alpha", after < before)
    }

    @Test
    fun `textured brush modulates coverage`() {
        val plain = document()
        stroke(plain, BrushPresets.default.copy(size = 30f, texture = null), (0..40).map { StrokePoint(20f + it * 5f, 128f, 1f) })
        val grained = document()
        stroke(
            grained,
            BrushPresets.default.copy(size = 30f, texture = Patterns.GRAIN_NAMES.first(), grainStrength = 1f),
            (0..40).map { StrokePoint(20f + it * 5f, 128f, 1f) },
        )
        fun variance(doc: ArtDocument): Long {
            val layer = requireNotNull(doc.activeLayer())
            var sum = 0L
            var count = 0L
            for (x in 20 until 230) {
                val a = layer.alphaAt(x, 128)
                if (a > 0) {
                    sum += a.toLong()
                    count++
                }
            }
            return if (count == 0L) 0 else sum / count
        }
        assertTrue("grain must not be equivalent to a solid stroke", variance(plain) != variance(grained))
    }

    @Test
    fun `stabilisation keeps the stroke attached to the sampled path`() {
        val doc = document()
        val points = (0..60).map { StrokePoint(30f + it * 3f, 128f, 1f) }
        stroke(doc, BrushPresets.default.copy(size = 8f, stabilization = 0f), points)
        val layer = requireNotNull(doc.activeLayer())
        assertNotNull(layer)
        val endAlpha = (180 until 220).maxOf { x -> layer.alphaAt(x, 128) }
        assertTrue("stroke must reach its end point", endAlpha > 32)
    }

    @Test
    fun `flush cadence does not change the painted result`() {
        // Regression: flushing mid-stroke used to write only the newest strip, so
        // fast strokes broke into dashes and stroke opacity compounded.
        fun paint(flushEvery: Int): IntArray {
            val doc = document(300, 160)
            val layer = requireNotNull(doc.activeLayer())
            val engine = BrushEngine()
            val spec = BrushPresets.default.copy(size = 16f, spacing = 0.1f, hardness = 0.3f, opacity = 0.5f)
            engine.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 11)
            for (i in 0..60) {
                engine.move(StrokePoint(30f + i * 4f, 80f, 1f, timestampMillis = i * 7L))
                if (flushEvery > 0 && i % flushEvery == 0) engine.flush()
            }
            engine.end()
            return IntArray(250) { x -> layer.alphaAt(40 + x, 80) }
        }
        val once = paint(0)
        val everySample = paint(1)
        val everyFive = paint(5)
        for (x in once.indices) {
            assertEquals("flush=1 differs at x=$x", once[x], everySample[x])
            assertEquals("flush=5 differs at x=$x", once[x], everyFive[x])
        }
        assertTrue("a 50% brush must not reach full opacity", once.max() <= 200)
        assertTrue("the stroke must be continuous", once.count { it > 0 } > 200)
    }

    @Test
    fun `undo after a multi-flush stroke restores a blank layer`() {
        val doc = document(240, 240)
        val layer = requireNotNull(doc.activeLayer())
        val engine = BrushEngine()
        engine.begin(doc, layer, BrushPresets.default.copy(size = 12f), 0xFF000000.toInt(), seed = 5)
        for (i in 0..40) {
            engine.move(StrokePoint(20f + i * 4f, 120f, 1f, timestampMillis = i * 6L))
            if (i % 3 == 0) engine.flush()
        }
        engine.end()
        assertTrue("the stroke must be visible", layer.alphaAt(100, 120) > 0)
        doc.history.undo()
        assertEquals("undo must clear the stroke", 0, layer.alphaAt(100, 120))
        assertEquals("undo must also release the tile it created", 0, layer.requireSurface().usedTileCount())
        doc.history.redo()
        assertTrue("redo must bring the stroke back", layer.alphaAt(100, 120) > 0)
    }

    @Test
    fun `stroke outside the canvas is clipped without crashing`() {
        val doc = document(128, 128)
        stroke(
            doc,
            BrushPresets.default.copy(size = 40f),
            listOf(StrokePoint(-200f, -200f, 1f), StrokePoint(400f, 400f, 1f)),
        )
        val layer = requireNotNull(doc.activeLayer())
        // Only the diagonal band inside the canvas may be painted.
        assertTrue(layer.alphaAt(64, 64) > 0)
    }

    private fun BrushPresets.eraserSized(size: Float): BrushSpec =
        BrushPresets.default.copy(size = size, opacity = 1f, flow = 1f, spacing = 0.1f)

    /** Alpha of a layer pixel, read through the sparse tile surface. */
    private fun com.monaart.engine.document.Layer.alphaAt(x: Int, y: Int): Int =
        (requireSurface().getPixel(x, y) ushr 24) and 0xFF
}
