package com.monaart.engine.io

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.straightToPremul
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Byte-level structural validation of the PSD writer, independent of the writer
 * itself: header, section lengths, layer records, the *planar* per-channel
 * encoding a real editor reads back, and the composite image data.
 */
class PsdStructureTest {

    private class Reader(val data: ByteArray) {
        var pos = 0
        fun byte(): Int {
            check(pos < data.size) { "unexpected end of PSD at $pos" }
            return data[pos++].toInt() and 0xFF
        }
        fun bytes(n: Int): ByteArray = data.copyOfRange(pos, pos + n).also { pos += n }
        fun skip(n: Int) { pos += n }
        fun short(): Int = (byte() shl 8) or byte()
        fun int(): Int = (short() shl 16) or short()
        fun expect(ascii: String) = assertEquals(ascii, String(bytes(ascii.length), Charsets.US_ASCII))
    }

    /** Two raster layers: the base paints one pixel, the "Decal" layer above it another. */
    private fun docWithTwoLayers(): ArtDocument {
        val d = ArtDocument.create("psd", 4, 4, transparentBackground = true)
        val baseS = requireNotNull(d.activeLayer()).requireSurface()
        baseS.setPixel(0, 0, straightToPremul(0xFF112233.toInt()))
        val top = com.monaart.engine.document.LayerOps.addRasterLayer(d, requireNotNull(d.activeLayer()), "Decal")
        requireNotNull(top.surface).setPixel(1, 1, straightToPremul(0xFF00FF00.toInt()))
        return d
    }

    /** Skips the file header/color-mode/image-resources; reader then sits at layer-info length. */
    private fun Reader.skipToLayerInfo() {
        expect("8BPS")
        assertEquals(1, short())
        skip(6) // reserved
        short(); int(); int(); short(); short() // channels, height, width, depth, mode
        int() // color mode data length
        int() // image resources length
    }

    @Test
    fun `layer records declare four planar channels with exact lengths`() {
        val r = Reader(PsdWriter.write(docWithTwoLayers()))
        r.skipToLayerInfo()

        val layerInfoLen = r.int()
        assertTrue(layerInfoLen > 0)
        val innerInfoLen = r.int()
        val sectionStart = r.pos
        val layerCount = r.short()
        assertEquals(2, layerCount)
        assertEquals(
            "inner length counts records + channel data",
            2 + layerRecordSize("Layer 1") + layerRecordSize("Decal") + 2 * 4 * 16,
            innerInfoLen,
        )

        repeat(2) { index ->
            val (top, left, bottom, right) = makeRect(r)
            assertEquals(0, top); assertEquals(0, left)
            assertEquals(4, bottom); assertEquals(4, right)
            assertEquals(4, r.short()) // channel count
            val ids = IntArray(4) {
                val id = channelIdAt(r)
                val len = r.int()
                assertEquals("layer $index channel raw length", 16, len)
                id
            }
            assertArrayEquals(
                "layer $index channel ids must be R,G,B,alpha",
                intArrayOf(0, 1, 2, -1),
                ids,
            )
            r.expect("8BIM"); r.expect("norm")
            assertEquals(255, r.byte()) // opacity
            assertEquals(0, r.byte())   // clipping
            r.byte()                    // flags
            r.byte()                    // filler
            val extraLen = r.int()
            r.skip(extraLen)
        }

        // Channel data for both layers must fit inside the declared section.
        val channelDataBytes = 2 * 4 * 16
        assertTrue("section must contain the channel data", sectionStart + channelDataBytes <= sectionStart + layerInfoLen)
        // After the channel data: global layer mask info of length 0.
        r.skip(channelDataBytes)
        assertEquals(0, r.int())
        // The whole layer-and-mask section body is padded to a multiple of 4.
        val written = r.pos - sectionStart
        assertTrue(
            "padding would exceed the declared length ($written > $layerInfoLen)",
            written <= layerInfoLen,
        )
        assertEquals(0, layerInfoLen % 4)
    }

    private fun makeRect(r: Reader): IntArray = intArrayOf(r.int(), r.int(), r.int(), r.int())

    /**
     * Channel ids are signed shorts written with writeShort; decode them signed.
     */
    private fun channelIdAt(r: Reader): Int {
        val hi = r.byte() and 0xFF
        val lo = r.byte() and 0xFF
        return ((hi shl 8) or lo).toShort().toInt()
    }

    /**
     * Size of one layer record produced by the writer: rect(16) + channel
     * declarations (26) + blend key/mode/opacity/flags (12) + extra-data
     * length field (4) + the extra content.
     */
    private fun layerRecordSize(name: String): Int {
        val n = name.length.coerceAtMost(255)
        val pascal = 1 + n + ((4 - ((n + 1) % 4)) % 4)
        val utf16 = 2 * n
        val luni = 4 + 4 + 4 + 4 + utf16 + ((4 - (utf16 % 4)) % 4)
        val extra = 4 + 8 + pascal + luni
        return 16 + 26 + 12 + 4 + extra + ((4 - (extra % 4)) % 4)
    }

    @Test
    fun `layer channel data is planar R,G,B,alpha matching the layer pixels`() {
        val r = Reader(PsdWriter.write(docWithTwoLayers()))
        r.skipToLayerInfo()
        r.int()          // layer-and-mask length
        r.int()          // layer information length
        assertEquals(2, r.short())
        repeat(2) {
            r.skip(4 * 4) // rect
            r.skip(2)
            repeat(4) { r.skip(6) }
            r.skip(12)    // 8BIM + norm + opacity + clipping + flags + filler
            val extraLen = r.int()
            r.skip(extraLen)
        }

        // Bottom layer first: pixel (0,0) = 0xFF112233 straight, rest transparent.
        fun plane(): IntArray = IntArray(16) { r.byte() }
        val red = plane()
        val green = plane()
        val blue = plane()
        val alpha = plane()
        assertEquals(0x11, red[0])
        assertEquals(0x22, green[0])
        assertEquals(0x33, blue[0])
        assertEquals(0xFF, alpha[0])
        for (i in 1 until 16) {
            assertEquals("plane $i transparent", 0, red[i])
            assertEquals(0, green[i])
            assertEquals(0, blue[i])
            assertEquals(0, alpha[i])
        }
        // Top layer: pixel (1,1) = 0xFF00FF00 straight.
        val tRed = plane(); val tGreen = plane(); val tBlue = plane(); val tAlpha = plane()
        assertEquals(0x00, tRed[5])
        assertEquals(0xFF, tGreen[5])
        assertEquals(0x00, tBlue[5])
        assertEquals(0xFF, tAlpha[5])
        for (i in listOf(0, 2, 3)) {
            assertEquals(0, tRed[i]); assertEquals(0, tAlpha[i])
        }
    }

    @Test
    fun `composite image data is raw and planar`() {
        val d = ArtDocument.create("flat", 2, 2, transparentBackground = false)
        requireNotNull(d.activeLayer()).requireSurface().setPixel(0, 0, straightToPremul(0xFFFFFFFF.toInt()))

        val r = Reader(PsdWriter.write(d))
        r.skipToLayerInfo()
        // A single layer, since the document has one.
        r.int()
        r.int()
        assertEquals(1, r.short())
        // One empty-channel-less record set: one layer record...
        r.skip(4 * 4)
        r.skip(2)
        repeat(4) { r.skip(6) }
        r.skip(12)
        val extraLen = r.int()
        r.skip(extraLen)
        // ...then 4 planes of 4 bytes each (= 16 bytes), then global mask info.
        r.skip(4 * 4)
        r.int() // global layer mask info length
        r.int() // global additional info length
        assertEquals(0, r.short()) // composite compression: raw
        val red = IntArray(4) { r.byte() }
        assertEquals(0xFF, red[0])
        assertEquals(0xFF, red[1]) // white background
        assertEquals(0xFF, red[2])
        assertEquals(0xFF, red[3])
        val green = IntArray(4) { r.byte() }
        assertEquals(0xFF, green[0])
        val blue = IntArray(4) { r.byte() }
        assertEquals(0xFF, blue[0])
    }
}
