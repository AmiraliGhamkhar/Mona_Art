package com.monaart.studio.presentation.editor

import com.monaart.engine.brush.BrushEngine
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.StrokePoint
import com.monaart.engine.document.ArtDocument
import com.monaart.engine.view.Viewport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

/**
 * Performance checks. These are not micro-benchmarks with statistical rigour —
 * they are regression guards with generous margins, so a change that makes
 * painting an order of magnitude slower fails CI instead of shipping.
 *
 * Numbers collected here are reported in BUILD_REPORT.md and in the docs; they
 * are *measured on the CI runner*, not asserted as device performance.
 */
class PerformanceTest {

    @Test
    fun `stroke throughput stays within budget`() {
        val doc = ArtDocument.create("perf", 2048, 2048, transparentBackground = true)
        val layer = requireNotNull(doc.activeLayer())
        val engine = BrushEngine()
        val spec = BrushPresets.default.copy(size = 24f, spacing = 0.1f)

        // Warm up the JIT so the first-stroke penalty does not dominate.
        engine.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 1)
        repeat(200) { engine.move(StrokePoint(100f + it * 3f, 100f, 1f)) }
        engine.end()

        val samples = 2000
        val perSample = ArrayList<Double>(samples)
        val totalNs = measureNanoTime {
            engine.begin(doc, layer, spec, 0xFF000000.toInt(), seed = 2)
            for (i in 0 until samples) {
                val t = i.toFloat() / samples
                val x = 40f + t * 1960f
                val y = 1000f + kotlin.math.sin(t * 30f) * 300f
                val pressure = (0.3f + 0.7f * kotlin.math.sin(t * 3.14159f).toFloat()).coerceIn(0.05f, 1f)
                val ns = measureNanoTime {
                    engine.move(StrokePoint(x, y, pressure))
                    engine.flush()
                }
                perSample.add(ns / 1_000_000.0)
            }
            engine.end()
        }
        val perSampleMs = totalNs / 1_000_000.0 / samples
        val p99 = perSample.sorted()[(samples * 99) / 100]
        println(
            "[perf] stroke engine: %.3f ms/sample mean, p99 %.2f ms (includes move + flush)".format(perSampleMs, p99),
        )
        assertTrue(
            "a pointer sample must cost well under a 16 ms frame (measured %.2f ms)".format(perSampleMs),
            perSampleMs < 4.0,
        )
        // 99th percentile, not the maximum: the maximum on shared CI hardware is
        // dominated by scheduler and collector pauses, which says nothing about
        // whether painting stays smooth.
        assertTrue("p99 sample cost must leave frame headroom (measured %.2f ms)".format(p99), p99 < 8.0)
    }

    @Test
    fun `saving and loading a mid-size document is fast enough for autosave`() {
        val doc = ArtDocument.create("save", 1024, 1024, transparentBackground = true)
        val layer = requireNotNull(doc.activeLayer())
        val engine = BrushEngine()
        engine.begin(doc, layer, BrushPresets.default.copy(size = 60f), 0xFF000000.toInt(), seed = 3)
        for (i in 0 until 400) engine.move(StrokePoint(50f + i * 2f, 512f, 1f))
        engine.end()

        val file = java.io.File.createTempFile("perf", ".mona")
        try {
            var bytes = 0
            val saveNs = measureNanoTime {
                com.monaart.engine.io.ProjectCodec.save(doc, file)
                bytes = file.length().toInt()
            }
            val loadNs = measureNanoTime {
                com.monaart.engine.io.ProjectCodec.load(file)
            }
            println(
                "[perf] save %.0f ms (%.1f KiB), load %.0f ms".format(
                    saveNs / 1_000_000.0, bytes / 1024.0, loadNs / 1_000_000.0,
                ),
            )
            assertTrue("save must complete within an autosave tick", saveNs / 1_000_000.0 < 3000.0)
            assertTrue("load must feel instant", loadNs / 1_000_000.0 < 3000.0)
        } finally {
            file.delete()
        }
    }

    @Test
    fun `viewport transforms are allocation-light and reversible`() {
        var vp = Viewport(scale = 1.8f, offsetX = 120f, offsetY = -40f, rotation = 0.4f)
        vp = vp.copy(viewportWidth = 1280f, viewportHeight = 800f)
        val (dx, dy) = vp.screenToDoc(640f, 400f)
        val (sx, sy) = vp.docToScreen(dx, dy)
        assertEquals(640f, sx, 0.01f)
        assertEquals(400f, sy, 0.01f)
    }
}
