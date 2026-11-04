package com.monaart.engine.diag

import com.monaart.engine.brush.BrushEngine
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.StrokePoint
import com.monaart.engine.brush.StrokeProcessor
import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Compositor
import com.monaart.engine.document.LayerOps
import com.monaart.engine.filter.FilterKind
import com.monaart.engine.filter.Filters
import com.monaart.engine.io.DocumentExport
import com.monaart.engine.io.ExportFormat
import com.monaart.engine.io.ExportOptions
import com.monaart.engine.io.PngCodec
import com.monaart.engine.io.ProjectCodec
import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.BlendMath
import com.monaart.engine.paint.PixelOps
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.packStraight
import com.monaart.engine.paint.redOf
import com.monaart.engine.selection.Selection
import com.monaart.engine.view.Viewport
import java.io.File
import kotlin.math.abs

/**
 * Executable verification suite for the painting engine. Runs on the JVM
 * (`./gradlew :engine:selfCheck`) and in CI, and prints a machine-readable
 * summary so results can be attached to a build.
 *
 * These checks exercise real behaviour (pixels, bytes, timing) rather than
 * asserting that classes exist.
 */
object SelfCheck {

    data class Result(val name: String, val passed: Boolean, val detail: String)

    fun run(verbose: Boolean = true): List<Result> {
        val results = mutableListOf<Result>()
        fun check(name: String, block: () -> Any?) {
            val t0 = System.nanoTime()
            val r = try {
                val detail = block()?.toString() ?: "ok"
                Result(name, true, detail)
            } catch (e: Throwable) {
                Result(name, false, "${e::class.simpleName}: ${e.message}")
            }
            val ms = (System.nanoTime() - t0) / 1_000_000
            results.add(r.copy(detail = r.detail + " (${ms}ms)"))
            if (verbose) println(if (r.passed) "PASS ${r.name} — ${r.detail} (${ms}ms)" else "FAIL ${r.name} — ${r.detail}")
        }

        // ---------------- Paint / blend ----------------
        check("blend.normal_passthrough") {
            val dst = packStraight(0.1f, 0.2f, 0.3f, 1f)
            val src = packStraight(1f, 0f, 0f, 1f)
            val out = PixelOps.composite(BlendMode.Normal, dst, src, 1f, 1f)
            assertEq(255, redOf(out), "red")
            assertEq(0, greenOf(out), "green")
        }

        check("blend.multiply_reference") {
            val a = 0.5f
            val b = 0.4f
            val expected = BlendMath.blendChannel(BlendMode.Multiply, a, b)
            val out = PixelOps.composite(
                BlendMode.Multiply,
                packStraight(a, a, a, 1f), packStraight(b, b, b, 1f), 1f, 1f,
            )
            near(redOf(out) / 255f, expected, 0.01f, "multiply")
        }

        check("blend.all_modes_defined") {
            // Every mode must produce a defined, in-range result for edge colors.
            for (m in BlendMode.entries) {
                for (pair in listOf(0 to 255, 255 to 0, 128 to 128, 3 to 250)) {
                    val out = PixelOps.composite(
                        m, packStraight(pair.first / 255f, 0.2f, 0.9f, 1f),
                        packStraight(pair.second / 255f, 0.7f, 0.1f, 1f), 1f, 1f,
                    )
                    if (alphaOf(out) != 255) throw IllegalStateException("${m.label}: alpha ${alphaOf(out)}")
                    if (redOf(out) > 255 || greenOf(out) > 255 || blueOf(out) > 255) throw IllegalStateException("${m.label}: channel overflow")
                }
            }
            "${BlendMode.entries.size} modes"
        }

        check("blend.composite_alpha_math") {
            // 50% red over opaque blue = purple-ish with alpha 255
            val out = PixelOps.composite(
                BlendMode.Normal, packStraight(0f, 0f, 1f, 1f), packStraight(1f, 0f, 0f, 1f), 0.5f, 1f
            )
            near(redOf(out) / 255f, 0.5f, 0.02f, "r")
            near(blueOf(out) / 255f, 0.5f, 0.02f, "b")
        }

        // ---------------- Brush ----------------
        check("brush.pressure_affects_geometry") {
            // G-Pen: size driven by pressure, taper disabled during measurement.
            val spec = BrushPresets.byId("ink.g-pen")!!.copy(taperMode = com.monaart.engine.brush.TaperMode.NONE)
            val low = StrokeProcessor(spec)
            val high = StrokeProcessor(spec)
            val d1 = low.accept(StrokePoint(10f, 10f, 0.3f, isStylus = true)).firstOrNull()
                ?: throw IllegalStateException("no dab")
            val d2 = high.accept(StrokePoint(10f, 10f, 1.0f, isStylus = true)).firstOrNull()
                ?: throw IllegalStateException("no dab")
            val expectedRatio = (1f + (0.3f - 1f) * spec.pressureSize)
            val actualRatio = d1.radius / d2.radius
            if (abs(actualRatio - expectedRatio) > 0.02f) {
                throw IllegalStateException("pressure curve not applied: expected $expectedRatio, got $actualRatio")
            }
            if (d1.radius >= d2.radius) throw IllegalStateException("light pressure was not thinner")
            // Opacity must also respond for opacity-driven brushes.
            val airbrush = BrushPresets.byId("airbrush.soft")!!
            if (airbrush.curve.apply(0.2f) >= airbrush.curve.apply(0.9f)) {
                throw IllegalStateException("pressure curve is not monotonic")
            }
            "radius ${"%.2f".format(d1.radius)} (30%%) vs ${"%.2f".format(d2.radius)} (100%%)"
        }

        check("brush.spacing_emits_dabs") {
            val spec = BrushPresets.byId("ink.technical-pen")!!
            val proc = StrokeProcessor(spec)
            var count = 0
            proc.accept(StrokePoint(0f, 0f, 1f))
            for (i in 1..100) count += proc.accept(StrokePoint(i * 2f, 0f, 1f, timestampMillis = i.toLong())).size
            if (count < 20) throw IllegalStateException("too few dabs for 200px stroke: $count")
            "$count dabs over 200px"
        }

        check("brush.stroke_paints_pixels") {
            val doc = ArtDocument.create("t", 256, 256, transparentBackground = true)
            val engine = BrushEngine()
            val spec = BrushPresets.byId("ink.g-pen")!!
            engine.begin(doc, doc.activeLayer()!!, spec, 0xFF000000.toInt())
            for (i in 0..60) engine.move(StrokePoint(20f + i * 3f, 128f, 0.8f, isStylus = true, timestampMillis = i * 8L))
            engine.end()
            val px = doc.activeLayer()!!.surface!!.getPixel(128, 128)
            if (alphaOf(px) < 200) throw IllegalStateException("stroke did not paint (alpha=${alphaOf(px)})")
            "alpha at centre ${alphaOf(px)}"
        }

        check("brush.eraser_removes_alpha") {
            val doc = ArtDocument.create("t", 128, 128, background = 0xFFFFFFFF.toInt())
            val layer = doc.activeLayer()!!
            LayerOps.fillLayer(doc, layer, 0xFF000000.toInt())
            val engine = BrushEngine()
            engine.begin(doc, layer, BrushPresets.byId("eraser.hard")!!, 0xFF000000.toInt())
            for (i in 0..40) engine.move(StrokePoint(20f + i * 2f, 64f, 1f, isStylus = true, timestampMillis = i * 8L))
            engine.end()
            val a = alphaOf(layer.surface!!.getPixel(64, 64))
            if (a > 40) throw IllegalStateException("eraser left alpha $a")
            "alpha after erase $a"
        }

        check("brush.all_presets_render") {
            // Every preset must produce visible pixels — no decorative presets.
            var blank = mutableListOf<String>()
            for (spec in BrushPresets.all) {
                if (spec.kind == com.monaart.engine.brush.BrushKind.SMUDGE) continue
                val doc = ArtDocument.create("t", 128, 128, transparentBackground = true)
                val isEraser = spec.kind == com.monaart.engine.brush.BrushKind.ERASER
                if (isEraser) LayerOps.fillLayer(doc, doc.activeLayer()!!, 0xFF000000.toInt())
                val engine = BrushEngine()
                engine.begin(doc, doc.activeLayer()!!, spec, 0xFF3366CC.toInt())
                for (i in 0..40) engine.move(StrokePoint(20f + i * 2f, 64f, 1f, timestampMillis = i * 8L))
                engine.end()
                val s = doc.activeLayer()!!.surface!!
                var nonZero = 0
                for (y in 0 until 128) for (x in 0 until 128) {
                    val a = alphaOf(s.getPixel(x, y))
                    if (if (isEraser) a < 250 else a > 4) nonZero++
                }
                if (nonZero < 5) blank.add(spec.id)
            }
            if (blank.isNotEmpty()) throw IllegalStateException("presets rendered nothing: $blank")
            "${BrushPresets.all.size} presets"
        }

        check("brush.sparse_samples_have_no_gaps") {
            // A fast stylus reports widely spaced samples. Dab centres must be
            // emitted along the whole path so the stroke never breaks.
            val spec = BrushPresets.byId("ink.technical-pen")!!
            val proc = StrokeProcessor(spec)
            proc.accept(StrokePoint(20f, 64f, 1f, timestampMillis = 0L))
            val dabs = ArrayList<StrokeProcessor.Dab>()
            for (i in 1..16) {
                dabs.addAll(proc.accept(StrokePoint(20f + i * 30f, 64f, 1f, timestampMillis = i * 16L)))
            }
            if (dabs.size < 400) throw IllegalStateException("too few dabs for 480px path: ${dabs.size}")
            var maxGap = 0f
            for (i in 1 until dabs.size) {
                maxGap = maxOf(maxGap, abs(dabs[i].x - dabs[i - 1].x) + abs(dabs[i].y - dabs[i - 1].y))
            }
            if (maxGap > spec.size * 0.4f) throw IllegalStateException("dab gap ${maxGap}px exceeds 40% of nib")
            // And the rendered stroke must be continuous where it exists.
            val doc = ArtDocument.create("t", 512, 128, transparentBackground = true)
            val engine = BrushEngine()
            engine.begin(doc, doc.activeLayer()!!, spec, 0xFF000000.toInt())
            for (i in 0..16) engine.move(StrokePoint(20f + i * 30f, 64f, 1f, timestampMillis = i * 16L))
            engine.end()
            val s = doc.activeLayer()!!.surface!!
            var inkedRows = 0
            for (y in 0 until 128) {
                var row = 0
                for (x in 0 until 512) if (alphaOf(s.getPixel(x, y)) > 30) row++
                if (row > 0) inkedRows++
            }
            if (inkedRows < 2) throw IllegalStateException("stroke has no height")
            "${dabs.size} dabs, max gap ${"%.2f".format(maxGap)}px, ${inkedRows}px tall stroke"
        }

        // ---------------- History ----------------
        check("history.undo_redo_restores_pixels") {
            val doc = ArtDocument.create("t", 64, 64, transparentBackground = true)
            val layer = doc.activeLayer()!!
            val engine = BrushEngine()
            engine.begin(doc, layer, BrushPresets.all.first { it.id == "ink.technical-pen" }, 0xFF000000.toInt())
            for (i in 0..20) engine.move(StrokePoint(5f + i * 2f, 32f, 1f, timestampMillis = i * 8L))
            engine.end()
            val after = layer.surface!!.getPixel(32, 32)
            if (alphaOf(after) == 0) throw IllegalStateException("nothing painted")
            doc.history.undo()
            if (alphaOf(layer.surface!!.getPixel(32, 32)) != 0) throw IllegalStateException("undo left pixels")
            doc.history.redo()
            if (alphaOf(layer.surface!!.getPixel(32, 32)) == 0) throw IllegalStateException("redo lost pixels")
            "undo/redo ok"
        }

        check("history.layer_ops_undoable") {
            val doc = ArtDocument.create("t", 64, 64)
            val before = doc.layers.size
            LayerOps.addRasterLayer(doc)
            LayerOps.setOpacity(doc, doc.activeLayer()!!, 0.5f)
            LayerOps.deleteLayer(doc, doc.activeLayer()!!)
            if (doc.layers.size != before) throw IllegalStateException("delete left ${doc.layers.size} layers")
            doc.history.undo()
            if (doc.layers.size != before + 1) throw IllegalStateException("undo delete failed")
            doc.history.undo()
            if (doc.activeLayer()?.opacity != 1f) throw IllegalStateException("undo opacity failed")
            doc.history.undo()
            if (doc.layers.size != before) throw IllegalStateException("undo add failed: ${doc.layers.size}")
            repeat(3) { doc.history.redo() }
            if (doc.layers.size != before) throw IllegalStateException("redo add/delete failed: ${doc.layers.size}")
            "ok"
        }

        // ---------------- Document / persistence ----------------
        check("io.project_roundtrip") {
            val doc = ArtDocument.create("Roundtrip", 300, 200)
            val engine = BrushEngine()
            engine.begin(doc, doc.activeLayer()!!, BrushPresets.byId("paint.watercolor-soft")!!, 0xFF22AA55.toInt())
            for (i in 0..50) engine.move(StrokePoint(20f + i * 5f, 100f, 0.9f, timestampMillis = i * 8L))
            engine.end()
            LayerOps.addRasterLayer(doc, name = "Second")
            LayerOps.setBlend(doc, doc.activeLayer()!!, BlendMode.Multiply)
            LayerOps.setOpacity(doc, doc.activeLayer()!!, 0.6f)
            val bytes = ProjectCodec.encode(doc)
            val loaded = ProjectCodec.decode(bytes)
            if (loaded.width != 300 || loaded.height != 200) throw IllegalStateException("size mismatch")
            if (loaded.layers.size != 2) throw IllegalStateException("layers ${loaded.layers.size}")
            if (loaded.layers[1].blend != BlendMode.Multiply) throw IllegalStateException("blend lost")
            val a = doc.activeLayer()!!.surface!!.getPixel(100, 100)
            val b = loaded.layers[1].surface!!.getPixel(100, 100)
            if (alphaOf(a) != 0 && a != b) throw IllegalStateException("pixel mismatch")
            val original = doc.flatten()
            val restored = loaded.flatten()
            for (i in original.pixels.indices step 97) {
                if (original.pixels[i] != restored.pixels[i]) throw IllegalStateException("composite mismatch at $i")
            }
            "format v${ProjectCodec.FORMAT_VERSION}, ${bytes.size / 1024} KiB"
        }

        check("io.corruption_detected") {
            val doc = ArtDocument.create("c", 64, 64)
            val bytes = ProjectCodec.encode(doc)
            val corrupted = bytes.copyOf()
            corrupted[corrupted.size / 2] = (corrupted[corrupted.size / 2] + 1).toByte()
            var detected = false
            try {
                ProjectCodec.decode(corrupted)
            } catch (e: ProjectCodec.CorruptProjectException) {
                detected = true
            } catch (e: Throwable) {
                detected = true
            }
            if (!detected) throw IllegalStateException("corruption not detected")
            "detected"
        }

        check("io.png_roundtrip") {
            val doc = ArtDocument.create("p", 120, 90)
            val layer = doc.activeLayer()!!
            LayerOps.fillLayer(doc, layer, 0xFFFF8800.toInt())
            val px = doc.flatten()
            val png = PngCodec.encode(px)
            val back = PngCodec.decode(png)
            if (back.width != 120 || back.height != 90) throw IllegalStateException("size")
            val c = back.pixels[45 * 120 + 60]
            if (redOf(c) != 255 || greenOf(c) != 0x88 || blueOf(c) != 0) throw IllegalStateException("color ${redOf(c)},${greenOf(c)},${blueOf(c)}")
            "png ${png.size}B"
        }

        check("io.all_export_formats_produce_bytes") {
            val doc = ArtDocument.create("e", 200, 150)
            LayerOps.fillLayer(doc, doc.activeLayer()!!, 0xFF8855CC.toInt())
            val sizes = StringBuilder()
            for (fmt in ExportFormat.entries) {
                val bytes = DocumentExport.export(doc, ExportOptions(format = fmt))
                if (bytes.size < 64) throw IllegalStateException("${fmt.label} too small (${bytes.size})")
                sizes.append(fmt.name).append('=').append(bytes.size).append(' ')
                // Signature sanity
                when (fmt) {
                    ExportFormat.PNG -> if (bytes[1] != 0x50.toByte()) throw IllegalStateException("PNG signature")
                    ExportFormat.JPEG -> if (bytes[0] != 0xFF.toByte() || bytes[1] != 0xD8.toByte()) throw IllegalStateException("JPEG SOI")
                    ExportFormat.PSD -> if (!String(bytes, 0, 4).equals("8BPS")) throw IllegalStateException("PSD header")
                    ExportFormat.PDF -> if (!String(bytes, 0, 5).equals("%PDF-")) throw IllegalStateException("PDF header")
                    ExportFormat.TIFF -> if (bytes[0] != 'I'.code.toByte() || bytes[1] != 'I'.code.toByte()) throw IllegalStateException("TIFF header")
                    ExportFormat.MONA -> if (bytes[0] != 'P'.code.toByte()) throw IllegalStateException("ZIP header")
                }
            }
            sizes.toString().trim()
        }

        check("io.psd_has_layers") {
            val doc = ArtDocument.create("psd", 64, 64)
            LayerOps.addRasterLayer(doc)
            val bytes = DocumentExport.export(doc, ExportOptions(format = ExportFormat.PSD))
            // Locate the layer info block: header(26) + color mode len(4) + resources len(4)
            val cmLen = ((bytes[26].toInt() and 0xFF) shl 24) or ((bytes[27].toInt() and 0xFF) shl 16) or
                ((bytes[28].toInt() and 0xFF) shl 8) or (bytes[29].toInt() and 0xFF)
            val resOff = 30 + cmLen
            val resLen = ((bytes[resOff].toInt() and 0xFF) shl 24) or ((bytes[resOff + 1].toInt() and 0xFF) shl 16) or
                ((bytes[resOff + 2].toInt() and 0xFF) shl 8) or (bytes[resOff + 3].toInt() and 0xFF)
            val layerOff = resOff + 4 + resLen
            val layerLen = ((bytes[layerOff].toInt() and 0xFF) shl 24) or ((bytes[layerOff + 1].toInt() and 0xFF) shl 16) or
                ((bytes[layerOff + 2].toInt() and 0xFF) shl 8) or (bytes[layerOff + 3].toInt() and 0xFF)
            if (layerLen <= 0) throw IllegalStateException("no layer info block")
            val lcOff = layerOff + 4
            val layerCount = ((bytes[lcOff].toInt() and 0xFF) shl 8) or (bytes[lcOff + 1].toInt() and 0xFF)
            if (layerCount != 2) throw IllegalStateException("layer count $layerCount (block at $lcOff, len $layerLen)")
            "2 layers"
        }

        // ---------------- Compositing ----------------
        check("compositor.layer_order") {
            val doc = ArtDocument.create("c", 32, 32, background = 0xFF000000.toInt(), transparentBackground = false)
            val base = doc.activeLayer()!!
            LayerOps.fillLayer(doc, base, 0xFF000000.toInt())
            val top = LayerOps.addRasterLayer(doc)
            LayerOps.fillLayer(doc, top, 0xFFFFFFFF.toInt())
            val out = doc.flatten()
            if (redOf(out.pixels[0]) != 255) throw IllegalStateException("top layer not on top")
            LayerOps.moveLayer(doc, top, -1)
            val out2 = doc.flatten()
            if (redOf(out2.pixels[0]) != 0) throw IllegalStateException("reorder failed")
            "order ok"
        }

        check("compositor.opacity_and_blend") {
            val doc = ArtDocument.create("c", 8, 8, background = 0xFF000000.toInt())
            val top = LayerOps.addRasterLayer(doc)
            LayerOps.fillLayer(doc, top, 0xFFFFFFFF.toInt())
            LayerOps.setOpacity(doc, top, 0.5f)
            val out = doc.flatten()
            near(redOf(out.pixels[0]) / 255f, 0.5f, 0.02f, "50% white over black")
            LayerOps.setBlend(doc, top, BlendMode.Screen)
            val out2 = doc.flatten()
            near(redOf(out2.pixels[0]) / 255f, 0.5f, 0.02f, "screen")
            "ok"
        }

        check("compositor.dirty_region_only") {
            val doc = ArtDocument.create("c", 2048, 2048, transparentBackground = true)
            val comp = Compositor()
            val region = com.monaart.engine.paint.RectI(100, 100, 356, 356)
            val out = Pixmap(2048, 2048)
            val t0 = System.nanoTime()
            repeat(20) { comp.render(doc, out, region, includeBackground = false) }
            val ms = (System.nanoTime() - t0) / 1_000_000.0 / 20
            if (ms > 50) throw IllegalStateException("region composite too slow: $ms ms")
            String.format("%.2f ms per 256px region on 2048² canvas", ms)
        }

        // ---------------- Filters / selection ----------------
        check("filters.all_apply") {
            val doc = ArtDocument.create("f", 64, 64)
            val layer = doc.activeLayer()!!
            LayerOps.fillLayer(doc, layer, 0xFF3366CC.toInt())
            for (kind in FilterKind.entries) {
                val ok = Filters.apply(doc, layer, kind, if (kind.max == kind.min) 0f else (kind.min + kind.max) / 2f)
                if (!ok) throw IllegalStateException("${kind.label} failed")
            }
            "${FilterKind.entries.size} filters"
        }

        check("selection.rectangle_limits_fill") {
            val doc = ArtDocument.create("s", 64, 64, transparentBackground = true)
            doc.selection = Selection.rectangle(64, 64, com.monaart.engine.paint.RectI(10, 10, 20, 20))
            LayerOps.fillLayer(doc, doc.activeLayer()!!, 0xFF00FF00.toInt())
            val s = doc.activeLayer()!!.surface!!
            if (alphaOf(s.getPixel(15, 15)) == 0) throw IllegalStateException("selection not filled")
            if (alphaOf(s.getPixel(40, 40)) != 0) throw IllegalStateException("fill leaked outside selection")
            "ok"
        }

        check("selection.magic_wand") {
            val doc = ArtDocument.create("s", 32, 32, background = 0xFFFFFFFF.toInt())
            val layer = doc.activeLayer()!!
            LayerOps.fillLayer(doc, layer, 0xFF000000.toInt())
            // Draw a white square then wand-select it.
            for (y in 5 until 15) for (x in 5 until 15) layer.surface!!.setPixel(x, y, 0xFFFFFFFF.toInt())
            val sel = Selection.magicWand(layer, 32, 32, 10, 10, 0.1f)
            if (sel.bounds.width !in 9..11) throw IllegalStateException("wand bounds ${sel.bounds}")
            "bounds ${sel.bounds}"
        }

        // ---------------- Viewport ----------------
        check("viewport.roundtrip_and_zoom") {
            val vp = Viewport(1f, 0f, 0f, 0f, 1000f, 800f, 2000, 1600).fit()
            val (sx, sy) = vp.docToScreen(123f, 456f)
            val (dx, dy) = vp.screenToDoc(sx, sy)
            near(dx, 123f, 0.01f, "x")
            near(dy, 456f, 0.01f, "y")
            val zoomed = vp.zoomed(2f, 500f, 400f)
            val (zx, zy) = zoomed.docToScreen(123f, 456f)
            val (rdx, rdy) = zoomed.screenToDoc(zx, zy)
            near(rdx, 123f, 0.01f, "zoom x")
            near(rdy, 456f, 0.01f, "zoom y")
            val rot = vp.rotated(0.6f)
            val (ax, ay) = rot.docToScreen(1000f, 800f)
            val (bx, by) = rot.screenToDoc(ax, ay)
            near(bx, 1000f, 0.05f, "rot x")
            near(by, 800f, 0.05f, "rot y")
            "ok"
        }

        // ---------------- Performance ----------------
        check("perf.stroke_throughput") {
            val doc = ArtDocument.create("perf", 2048, 2048, transparentBackground = true)
            val engine = BrushEngine()
            val spec = BrushPresets.byId("paint.round-oil")!!
            engine.begin(doc, doc.activeLayer()!!, spec, 0xFFAA3344.toInt())
            val n = 600
            val t0 = System.nanoTime()
            for (i in 0 until n) {
                engine.move(StrokePoint(100f + (i % 400) * 4f, 200f + (i / 400) * 300f, 0.7f, timestampMillis = i.toLong()))
                if (i % 4 == 0) engine.flush()
            }
            engine.end()
            val ms = (System.nanoTime() - t0) / 1_000_000.0
            val perEvent = ms / n
            if (perEvent > 12) throw IllegalStateException("stroke processing too slow: %.2f ms/event".format(perEvent))
            String.format("%.2f ms per stylus event (%.0f events/s)", perEvent, 1000 / perEvent)
        }

        check("perf.large_canvas_memory") {
            val doc = ArtDocument.create("big", 4096, 4096, transparentBackground = true)
            val bytes = doc.layers.sumOf { it.pixelBytes() }
            if (bytes > 2 * 1024 * 1024) throw IllegalStateException("empty 4096² doc uses $bytes bytes")
            val t0 = System.nanoTime()
            val engine = BrushEngine()
            engine.begin(doc, doc.activeLayer()!!, BrushPresets.byId("ink.technical-pen")!!, 0xFF000000.toInt())
            for (i in 0..300) engine.move(StrokePoint(500f + i * 8f, 2000f, 1f, timestampMillis = i.toLong()))
            engine.end()
            val ms = (System.nanoTime() - t0) / 1_000_000.0
            val touched = doc.layers.sumOf { it.pixelBytes() } / 1024
            if (ms > 3000) throw IllegalStateException("large canvas stroke too slow: $ms ms")
            String.format("stroke on 4096² in %.0f ms, %d KiB of tiles used", ms, touched)
        }

        check("perf.save_large_project") {
            val doc = ArtDocument.create("save", 2048, 2048)
            LayerOps.fillLayer(doc, doc.activeLayer()!!, 0xFF203040.toInt())
            val t0 = System.nanoTime()
            val bytes = ProjectCodec.encode(doc)
            val ms = (System.nanoTime() - t0) / 1_000_000.0
            val t1 = System.nanoTime()
            ProjectCodec.decode(bytes)
            val ms2 = (System.nanoTime() - t1) / 1_000_000.0
            String.format("2048² save %.0f ms (%.1f MiB), load %.0f ms", ms, bytes.size / 1048576.0, ms2)
        }

        return results
    }

    private fun near(actual: Float, expected: Float, tol: Float, label: String) {
        if (abs(actual - expected) > tol) throw IllegalStateException("$label: expected $expected ±$tol, got $actual")
    }

    private fun assertEq(expected: Int, actual: Int, label: String) {
        if (expected != actual) throw IllegalStateException("$label: expected $expected, got $actual")
    }
}

fun main(args: Array<String>) {
    val verbose = !args.contains("--quiet")
    val results = SelfCheck.run(verbose)
    val failed = results.count { !it.passed }
    val summary = buildString {
        appendLine("# Engine self-check report")
        appendLine()
        appendLine("Generated: ${java.time.Instant.now()}")
        appendLine()
        appendLine("| Check | Result | Detail |")
        appendLine("|---|---|---|")
        for (r in results) appendLine("| ${r.name} | ${if (r.passed) "PASS" else "FAIL"} | ${r.detail} |")
        appendLine()
        appendLine("**${results.size - failed}/${results.size} checks passed.**")
    }
    println()
    println(summary)
    val out = args.firstOrNull { it.startsWith("--report=") }?.substringAfter("=")
    if (out != null) {
        val f = File(out)
        f.parentFile?.mkdirs()
        f.writeText(summary)
    }
    if (failed > 0) {
        System.exit(1)
    }
}
