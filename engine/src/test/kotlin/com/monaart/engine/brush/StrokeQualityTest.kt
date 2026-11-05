package com.monaart.engine.brush

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Layer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Quality and performance contract of the stroke engine.
 *
 * These tests exist because the two defects they guard against were invisible
 * to feature tests: a stroke that flushed mid-draw used to break into dashes
 * (quality), and `flush()` used to re-composite the whole stroke box on every
 * pointer sample (cost). Both are cheap to regress and expensive to notice.
 */
class StrokeQualityTest {
    private fun document(width: Int = 512, height: Int = 512) =
        ArtDocument.create("quality", width, height, transparentBackground = true)

    @Test
    fun `horizontal and vertical strokes are equally continuous`() {
        fun gaps(vertical: Boolean): Int {
            val doc = document()
            val layer = requireNotNull(doc.activeLayer())
            val engine = BrushEngine()
            engine.begin(
                doc, layer, BrushPresets.default.copy(size = 8f, spacing = 0.12f),
                0xFF000000.toInt(), seed = 3,
            )
            for (i in 0..40) {
                val point = if (vertical) {
                    StrokePoint(256f, 40f + i * 10f, 1f, timestampMillis = i * 6L)
                } else {
                    StrokePoint(40f + i * 10f, 256f, 1f, timestampMillis = i * 6L)
                }
                engine.move(point)
                if (i % 4 == 0) engine.flush()
            }
            engine.end()
            var holes = 0
            for (i in 0 until 380) {
                val alpha = if (vertical) layer.alphaAt(256, 45 + i) else layer.alphaAt(45 + i, 256)
                if (alpha < 32) holes++
            }
            return holes
        }
        assertEquals("horizontal stroke must be unbroken", 0, gaps(false))
        assertEquals("vertical stroke must be unbroken", 0, gaps(true))
    }

    @Test
    fun `fast strokes over long distances stay connected`() {
        val doc = document(1024, 256)
        val layer = requireNotNull(doc.activeLayer())
        val engine = BrushEngine()
        engine.begin(doc, layer, BrushPresets.default.copy(size = 6f, spacing = 0.15f), 0xFF000000.toInt(), seed = 1)
        // Only four samples across the whole canvas: interpolation has to do the work.
        listOf(20f, 340f, 660f, 1000f).forEachIndexed { i, x ->
            engine.move(StrokePoint(x, 128f, 1f, timestampMillis = i * 120L))
            engine.flush()
        }
        engine.end()
        val holes = (25 until 995).count { x -> layer.alphaAt(x, 128) < 32 }
        assertEquals("a 4-sample stroke must still be a continuous line", 0, holes)
    }

    @Test
    fun `soft brush opacity is independent of flush cadence`() {
        fun profile(flushEvery: Int): List<Int> {
            val doc = document(400, 200)
            val layer = requireNotNull(doc.activeLayer())
            val engine = BrushEngine()
            val spec = BrushPresets.default.copy(
                size = 24f, spacing = 0.08f, hardness = 0.1f,
                opacity =
                0.4f,
                flow = 0.6f,
            )
            engine.begin(doc, layer, spec, 0xFF202020.toInt(), seed = 9)
            for (i in 0..90) {
                engine.move(StrokePoint(30f + i * 4f, 100f, 0.8f, timestampMillis = i * 5L))
                if (flushEvery > 0 && i % flushEvery == 0) engine.flush()
            }
            engine.end()
            return (40..360 step 20).map { x -> layer.alphaAt(x, 100) }
        }
        val continuous = profile(0)
        assertEquals("per-sample flushing must match batch flushing", continuous, profile(1))
        assertEquals("periodic flushing must match batch flushing", continuous, profile(7))
        assertTrue("a 40% brush must stay translucent", continuous.max() < 250)
    }

    @Test
    fun `eraser only removes what the stroke covers`() {
        val doc = document()
        val layer = requireNotNull(doc.activeLayer())
        val surface = layer.requireSurface()
        com.monaart.engine.document.LayerOps.fillLayer(
            doc, layer, 0xFF3366FF.toInt(), useSelection = false,
        )
        val engine = BrushEngine()
        engine.begin(
            doc, layer,
            BrushPresets.default.copy(
                size = 30f, kind = BrushKind.ERASER, opacity = 1f,
                flow = 1f, spacing = 0.1f,
            ),
            0x00000000, seed = 2,
        )
        for (i in 0..10) engine.move(StrokePoint(60f + i * 8f, 128f, 1f, timestampMillis = i * 10L))
        engine.end()
        assertEquals("the erased band is transparent", 0, layer.alphaAt(100, 128))
        assertEquals("pixels outside the band keep their alpha", 255, layer.alphaAt(100, 200))
        assertTrue("the layer keeps its colour", surface.getPixel(100, 200) != 0)
    }

    @Test
    fun `flush cost per pointer sample stays inside the frame budget on a large canvas`() {
        val doc = document(2048, 2048)
        val layer = requireNotNull(doc.activeLayer())
        val engine = BrushEngine()
        val spec = BrushPresets.default.copy(size = 24f, spacing = 0.1f)
        engine.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 4)
        // Warm up so JIT and tile allocation are not measured.
        for (i in 0..60) engine.move(StrokePoint(100f + i * 4f, 200f, 1f, timestampMillis = i * 4L))
        engine.flush()

        val samples = 400
        val start = System.nanoTime()
        for (i in 0 until samples) {
            val t = i.toFloat() / samples
            engine.move(StrokePoint(120f + t * 1800f, 1000f + kotlin.math.sin(t * 20f) * 200f, 0.7f))
            engine.flush()
        }
        engine.end()
        val perSampleMs = (System.nanoTime() - start) / 1_000_000.0 / samples
        println("[perf] engine stroke+flush: %.3f ms/sample".format(perSampleMs))
        // A 120 Hz display has 8.3 ms per frame; the stroke must leave room for UI.
        assertTrue("stroke flush must stay under 4 ms/sample, measured %.2f".format(perSampleMs), perSampleMs < 4.0)
    }

    @Test
    fun `recycling the stroke buffer never changes the result`() {
        // A stroke this long outgrows the scratch buffer, so the engine drops the
        // coverage it has already flushed and rebuilds around the live tail. That
        // optimisation is only safe if the painted result is identical to a stroke
        // that was drawn in one pass, including where the two passes cross.
        fun render(flushEvery: Int): IntArray {
            val doc = document(1024, 1024)
            val layer = requireNotNull(doc.activeLayer())
            val engine = BrushEngine()
            val spec = BrushPresets.default.copy(size = 12f, spacing = 0.05f, opacity = 0.35f, flow = 0.5f)
            engine.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 7)
            for (i in 0..500) {
                engine.move(StrokePoint(20f + i * 1.8f, 200f + i * 0.2f, 1f, timestampMillis = i * 8L))
                if (flushEvery > 0 && i % flushEvery == 0) engine.flush()
            }
            for (i in 0..500) {
                engine.move(StrokePoint(920f - i * 1.8f, 700f - i * 0.5f, 1f, timestampMillis = 4000L + i * 8L))
                if (flushEvery > 0 && i % flushEvery == 0) engine.flush()
            }
            engine.end()
            val out = IntArray(1024 * 1024)
            layer.requireSurface().copyRect(com.monaart.engine.paint.RectI(0, 0, 1024, 1024), out)
            return out
        }
        val reference = render(0)
        for (every in listOf(1, 5, 40)) {
            val other = render(every)
            var differing = 0
            for (i in reference.indices) if (reference[i] != other[i]) differing++
            assertEquals("flushing every $every samples must not change a pixel", 0, differing)
        }
        assertTrue("the stroke must actually paint", reference.any { it != 0 })
    }

    @Test
    fun `a long stroke does not keep growing its scratch memory`() {
        val doc = document(2048, 2048)
        val layer = requireNotNull(doc.activeLayer())
        val engine = BrushEngine()
        val spec = BrushPresets.default.copy(size = 24f, spacing = 0.1f)
        engine.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 5)
        val samples = ArrayList<Double>(1600)
        for (i in 0..1500) {
            val t = i.toFloat() / 1500f
            val started = System.nanoTime()
            engine.move(StrokePoint(40f + t * 1960f, 1000f + kotlin.math.sin(t * 40f) * 900f, 1f))
            engine.flush()
            samples.add((System.nanoTime() - started) / 1_000_000.0)
        }
        engine.end()
        val sorted = samples.sorted()
        val p99 = sorted[(sorted.size * 99) / 100]
        val mean = sorted.average()
        println(
            "[perf] long diagonal stroke: mean %.3f ms, p99 %.2f ms, worst %.2f ms".format(
                mean, p99,
                sorted.last(),
            ),
        )
        assertTrue("mean cost per sample must stay in budget, measured %.2f ms".format(mean), mean < 2.0)
        // The regression this guards is a stall that grows with stroke length: the
        // buffer used to expand to the stroke's bounding box, reaching 134 MB with
        // an 85 ms pause. A percentile gate catches that without failing on the
        // occasional scheduler or collector pause on shared CI hardware.
        assertTrue("99th percentile must leave frame headroom, measured %.2f ms".format(p99), p99 < 8.0)
    }

    @Test
    fun `symmetric strokes mark both mirror axes`() {
        val doc = document()
        val layer = requireNotNull(doc.activeLayer())
        // Mirroring runs one engine per axis on the same layer, exactly as the
        // editor's symmetry feature does.
        val spec = BrushPresets.default.copy(size = 10f)
        val left = BrushEngine()
        val right = BrushEngine()
        left.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 6)
        right.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 6)
        for (i in 0..20) {
            val x = 60f + i * 4f
            left.move(StrokePoint(x, 80f, 1f, timestampMillis = i * 8L))
            right.move(StrokePoint(doc.width - x, 80f, 1f, timestampMillis = i * 8L))
            if (i % 6 == 0) {
                left.flush()
                right.flush()
            }
        }
        left.end()
        right.end()
        assertTrue("left branch painted", layer.alphaAt(100, 80) > 0)
        assertTrue("right branch painted", layer.alphaAt(doc.width - 100, 80) > 0)
    }

    /** Alpha of a layer pixel, read through the sparse tile surface. */
    private fun Layer.alphaAt(x: Int, y: Int): Int =
        (requireSurface().getPixel(x, y) ushr 24) and 0xFF
}
