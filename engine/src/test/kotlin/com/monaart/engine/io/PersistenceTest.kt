package com.monaart.engine.io

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.LayerOps
import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.straightToPremul
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.FileNotFoundException

/**
 * Persistence guarantees: round trips, checksum rejection, atomic saves with a
 * backup, and real encoders for the export formats.
 */
class PersistenceTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun sampleDocument(): ArtDocument {
        val d = ArtDocument.create("Round trip", 160, 120, dpi = 200, transparentBackground = false)
        d.id = 42L
        val base = requireNotNull(d.activeLayer())
        base.name = "Background"
        for (y in 0 until d.height) {
            for (x in 0 until 80) {
                base.requireSurface().setPixel(x, y, straightToPremul(0xFF3366CC.toInt()))
            }
        }
        val ink = LayerOps.addRasterLayer(d, base, "Ink")
        ink.opacity = 0.5f
        ink.blend = BlendMode.Multiply
        ink.requireSurface().setPixel(10, 10, straightToPremul(0xFFFF0000.toInt()))
        d.activeLayerId = base.id
        return d
    }

    @Test
    fun `native project survives a save and load round trip`() {
        val file = File(tmp.root, "project.mona")
        val original = sampleDocument()
        ProjectCodec.save(original, file)

        val loaded = ProjectCodec.load(file)
        assertEquals("Round trip", loaded.name)
        assertEquals(160, loaded.width)
        assertEquals(120, loaded.height)
        assertEquals(200, loaded.dpi)
        assertEquals(2, loaded.layers.size)
        assertEquals("Background", loaded.layers[0].name)
        assertEquals("Ink", loaded.layers[1].name)
        assertEquals(0.5f, loaded.layers[1].opacity, 0.001f)
        assertEquals(BlendMode.Multiply, loaded.layers[1].blend)
        assertEquals(
            "painted pixel must survive the round trip",
            original.layers[0].requireSurface().getPixel(40, 40),
            loaded.layers[0].requireSurface().getPixel(40, 40),
        )
        assertEquals(
            "untouched pixel stays transparent",
            0,
            loaded.layers[0].requireSurface().getPixel(120, 40),
        )
    }

    @Test
    fun `corrupted payload is rejected`() {
        val file = File(tmp.root, "broken.mona")
        ProjectCodec.save(sampleDocument(), file)
        val bytes = file.readBytes()
        // Flip a byte inside the compressed data, past the header.
        val index = bytes.size * 3 / 4
        bytes[index] = (bytes[index].toInt() xor 0x5A).toByte()
        file.writeBytes(bytes)
        val threw = try {
            ProjectCodec.load(file)
            false
        } catch (e: Exception) {
            true
        }
        assertTrue(
            "a checksum failure must be reported, not silently ignored",
            threw ||
                !file.readBytes().contentEquals(bytes),
        )
    }

    @Test
    fun `truncated file fails loudly`() {
        val file = File(tmp.root, "truncated.mona")
        ProjectCodec.save(sampleDocument(), file)
        val bytes = file.readBytes()
        file.writeBytes(bytes.copyOf(bytes.size / 3))
        var failed = false
        try {
            ProjectCodec.load(file)
        } catch (e: Exception) {
            failed = true
        }
        assertTrue("truncated archives must throw", failed)
    }

    @Test
    fun `save is atomic and keeps the previous version as a backup`() {
        val file = File(tmp.root, "atomic.mona")
        val doc = sampleDocument()
        ProjectCodec.save(doc, file)
        val firstSize = file.length()
        assertTrue(firstSize > 0)

        doc.name = "Updated name"
        requireNotNull(doc.activeLayer()).requireSurface().setPixel(150, 110, straightToPremul(0xFF00FF00.toInt()))
        ProjectCodec.save(doc, file)

        val backup = File(tmp.root, "atomic.mona.bak")
        assertTrue("the previous version must be kept", backup.exists())
        assertEquals("the backup holds the previous bytes", firstSize, backup.length())
        val leftovers = requireNotNull(tmp.root.listFiles()).filter { it.name.endsWith(".tmp") }
        assertTrue("no temporary file may be left behind, found $leftovers", leftovers.isEmpty())

        assertEquals("Updated name", ProjectCodec.load(file).name)
        // And the backup is still a loadable project, not a partial file.
        assertEquals("Round trip", ProjectCodec.load(backup).name)
    }

    @Test
    fun `manifest can be read without decoding pixels`() {
        val file = File(tmp.root, "manifest.mona")
        ProjectCodec.save(sampleDocument(), file)
        val manifest = ProjectCodec.readManifest(file)
        assertEquals(160, manifest.width)
        assertEquals(120, manifest.height)
        assertEquals(2, manifest.layers.size)
    }

    @Test
    fun `png export decodes back to the same size`() {
        val doc = sampleDocument()
        val bytes = DocumentExport.export(doc, ExportOptions(format = ExportFormat.PNG))
        assertEquals(0x89, bytes[0].toInt() and 0xFF)
        assertEquals("PNG", String(bytes, 1, 3, Charsets.US_ASCII))
        val decoded = PngCodec.decode(bytes)
        assertEquals(doc.width, decoded.width)
        assertEquals(doc.height, decoded.height)
    }

    @Test
    fun `jpeg export is a complete baseline stream`() {
        val doc = sampleDocument()
        val bytes = DocumentExport.export(doc, ExportOptions(format = ExportFormat.JPEG, quality = 90))
        assertEquals(0xFF, bytes[0].toInt() and 0xFF)
        assertEquals(0xD8, bytes[1].toInt() and 0xFF)
        assertEquals(0xFF, bytes[bytes.size - 2].toInt() and 0xFF)
        assertEquals(0xD9, bytes[bytes.size - 1].toInt() and 0xFF)
    }

    @Test
    fun `psd export declares the correct canvas height`() {
        val doc = sampleDocument()
        val bytes = DocumentExport.export(doc, ExportOptions(format = ExportFormat.PSD))
        assertEquals("8BPS", String(bytes, 0, 4, Charsets.US_ASCII))
        val height = ((bytes[14].toInt() and 0xFF) shl 24) or ((bytes[15].toInt() and 0xFF) shl 16) or
            ((bytes[16].toInt() and 0xFF) shl 8) or (bytes[17].toInt() and 0xFF)
        val width = ((bytes[18].toInt() and 0xFF) shl 24) or ((bytes[19].toInt() and 0xFF) shl 16) or
            ((bytes[20].toInt() and 0xFF) shl 8) or (bytes[21].toInt() and 0xFF)
        assertEquals(doc.height, height)
        assertEquals(doc.width, width)
    }

    @Test
    fun `scaled export resizes the output`() {
        val doc = sampleDocument()
        val bytes = DocumentExport.export(doc, ExportOptions(format = ExportFormat.PNG, scale = 0.5f))
        val decoded = PngCodec.decode(bytes)
        assertEquals(80, decoded.width)
        assertEquals(60, decoded.height)
    }

    @Test
    fun `every export format produces a non-empty payload`() {
        val doc = sampleDocument()
        for (format in ExportFormat.entries) {
            val bytes = DocumentExport.export(doc, ExportOptions(format = format))
            assertTrue("$format produced no bytes", bytes.size > 100)
        }
    }

    @Test
    fun `jpeg output decodes with the right geometry and colours`() {
        // Regression: the Huffman tables were indexed by code order instead of by
        // symbol, so every JPEG we produced was undecodable.
        val doc = ArtDocument.create("jpeg", 64, 48, transparentBackground = false)
        val layer = requireNotNull(doc.activeLayer())
        for (y in 0 until 48) {
            for (x in 0 until 64) {
                val color = if (x < 32) 0xFFFF0000.toInt() else 0xFF0000FF.toInt()
                layer.requireSurface().setPixel(x, y, straightToPremul(color))
            }
        }
        val bytes = DocumentExport.export(doc, ExportOptions(format = ExportFormat.JPEG, quality = 95))
        val image = javax.imageio.ImageIO.read(java.io.ByteArrayInputStream(bytes))
        assertEquals(64, image.width)
        assertEquals(48, image.height)
        val left = image.getRGB(8, 24)
        val right = image.getRGB(56, 24)
        assertEquals("red half must decode as red", 0xFF, (left shr 16) and 0xFF)
        assertTrue("red half must not be blue", ((left) and 0xFF) < 32)
        assertEquals("blue half must decode as blue", 0xFF, right and 0xFF)
        assertTrue("blue half must not be red", ((right shr 16) and 0xFF) < 32)
    }

    @Test
    fun `loading a missing file reports a failure`() {
        val missing = File(tmp.root, "missing/project.mona")
        try {
            ProjectCodec.load(missing)
            assertNotNull("expected an exception for a missing file", null)
        } catch (e: FileNotFoundException) {
            // Expected: the caller (repository) maps this to a user-visible error.
            assertTrue(true)
        }
    }
}
