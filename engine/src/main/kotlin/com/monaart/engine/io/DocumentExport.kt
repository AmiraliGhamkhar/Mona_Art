package com.monaart.engine.io

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf
import java.io.ByteArrayOutputStream

/** Export formats offered to the user. */
enum class ExportFormat(val label: String, val mime: String, val extension: String, val supportsLayers: Boolean) {
    MONA("Mona Art project", "application/octet-stream", "mona", true),
    PNG("PNG", "image/png", "png", false),
    JPEG("JPEG", "image/jpeg", "jpg", false),
    TIFF("TIFF", "image/tiff", "tif", false),
    PSD("Photoshop PSD", "image/vnd.adobe.photoshop", "psd", true),
    PDF("PDF", "application/pdf", "pdf", false),
}

/** Options applied at export time. */
data class ExportOptions(
    val format: ExportFormat = ExportFormat.PNG,
    val scale: Float = 1f,
    /** JPEG/WEBP quality 0..100. */
    val quality: Int = 92,
    /** Flatten transparency onto white when the format has no alpha. */
    val flattenToWhite: Boolean = true,
    /** Crop to the document bounds instead of exporting the viewport. */
    val transparentBackground: Boolean = true,
)

/**
 * Orchestrates export. Rendering uses the same compositor as the on-screen
 * canvas so exports match the artist's view exactly.
 */
object DocumentExport {

    fun renderForExport(doc: ArtDocument, options: ExportOptions): Pixmap {
        val base = doc.flatten()
        val scaled = if (options.scale != 1f) {
            val w = (base.width * options.scale).toInt().coerceAtLeast(1)
            val h = (base.height * options.scale).toInt().coerceAtLeast(1)
            base.scaledTo(w, h)
        } else base
        if (options.flattenToWhite && !options.transparentBackground) {
            val white = Pixmap.filled(scaled.width, scaled.height, com.monaart.engine.paint.ColorInts.WHITE)
            white.blit(scaled)
            return white
        }
        return scaled
    }

    fun export(doc: ArtDocument, options: ExportOptions): ByteArray {
        return when (options.format) {
            ExportFormat.MONA -> ProjectCodec.encode(doc)
            ExportFormat.PSD -> PsdWriter.write(doc)
            ExportFormat.PNG -> {
                val p = renderForExport(doc, options.copy(transparentBackground = true))
                PngCodec.encode(p)
            }
            ExportFormat.JPEG -> {
                val p = renderForExport(doc, options.copy(transparentBackground = false))
                JpegCodec.encode(p, options.quality)
            }
            ExportFormat.TIFF -> {
                val p = renderForExport(doc, options.copy(transparentBackground = true))
                TiffWriter.write(p)
            }
            ExportFormat.PDF -> {
                val p = renderForExport(doc, options.copy(transparentBackground = false))
                PdfWriter.write(p, doc.name)
            }
        }
    }
}

/** Baseline JPEG writer (4:4:4, standard tables) so JPEG export works without Android. */
object JpegCodec {
    fun encode(pixmap: Pixmap, quality: Int): ByteArray {
        val w = pixmap.width
        val h = pixmap.height
        val out = ByteArrayOutputStream(w * h / 2 + 1024)
        // SOI
        out.write(0xFF); out.write(0xD8)
        // APP0 JFIF
        out.write(0xFF); out.write(0xE0)
        writeShort(out, 16)
        "JFIF".forEach { out.write(it.code) }
        out.write(0); out.write(1); out.write(1); out.write(0)
        writeShort(out, 1); writeShort(out, 1)
        out.write(0); out.write(0)
        // DQT
        val luma = intArrayOf(
            16, 11, 10, 16, 24, 40, 51, 61, 12, 12, 14, 19, 26, 58, 60, 55,
            14, 13, 16, 24, 40, 57, 69, 56, 14, 17, 22, 29, 51, 87, 80, 62,
            18, 22, 37, 56, 68, 109, 103, 77, 24, 35, 55, 64, 81, 104, 113, 92,
            49, 64, 78, 87, 103, 121, 120, 101, 72, 92, 95, 98, 112, 100, 103, 99,
        )
        val chroma = intArrayOf(
            17, 18, 24, 47, 99, 99, 99, 99, 18, 21, 26, 66, 99, 99, 99, 99,
            24, 26, 56, 99, 99, 99, 99, 99, 47, 66, 99, 99, 99, 99, 99, 99,
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
            99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99,
        )
        val q = quality.coerceIn(1, 100)
        val scale = if (q < 50) 5000 / q else 200 - q * 2
        val lq = IntArray(64) { ((luma[ZIGZAG[it]] * scale + 50) / 100).coerceIn(1, 255) }
        val cq = IntArray(64) { ((chroma[ZIGZAG[it]] * scale + 50) / 100).coerceIn(1, 255) }
        writeDqt(out, 0, lq)
        writeDqt(out, 1, cq)
        // SOF0
        out.write(0xFF); out.write(0xC0)
        writeShort(out, 17)
        out.write(8)
        writeShort(out, h); writeShort(out, w)
        out.write(3)
        out.write(1); out.write(0x11); out.write(0)
        out.write(2); out.write(0x11); out.write(1)
        out.write(3); out.write(0x11); out.write(1)
        // DHT (standard tables)
        writeHuffmanTables(out)
        // SOS
        out.write(0xFF); out.write(0xDA)
        writeShort(out, 12)
        out.write(3)
        out.write(1); out.write(0x00)
        out.write(2); out.write(0x11)
        out.write(3); out.write(0x11)
        out.write(0); out.write(63); out.write(0)

        val dcState = DcState()
        val bmp = ByteArrayOutputStream()
        val bitBuffer = BitBuffer(bmp)
        val yBlock = IntArray(64)
        val cbBlock = IntArray(64)
        val crBlock = IntArray(64)
        val rgb = ByteArray(w * h * 3)
        for (i in 0 until w * h) {
            val p = pixmap.pixels[i]
            val a = alphaOf(p)
            val inv = 255 - a
            val r = if (a == 0) 255 else (redOf(p) * 255 + a / 2) / a
            val g = if (a == 0) 255 else (greenOf(p) * 255 + a / 2) / a
            val b = if (a == 0) 255 else (blueOf(p) * 255 + a / 2) / a
            // flatten onto white
            rgb[i * 3] = ((r * a + 255 * inv) / 255).toByte()
            rgb[i * 3 + 1] = ((g * a + 255 * inv) / 255).toByte()
            rgb[i * 3 + 2] = ((b * a + 255 * inv) / 255).toByte()
        }
        var y = 0
        while (y < h) {
            var x = 0
            while (x < w) {
                for (c in 0..2) {
                    for (yy in 0 until 8) for (xx in 0 until 8) {
                        val px = (x + xx).coerceAtMost(w - 1)
                        val py = (y + yy).coerceAtMost(h - 1)
                        val i = (py * w + px) * 3
                        val r = rgb[i].toInt() and 0xFF
                        val g = rgb[i + 1].toInt() and 0xFF
                        val b = rgb[i + 2].toInt() and 0xFF
                        val v = when (c) {
                            0 -> 0.299f * r + 0.587f * g + 0.114f * b - 128f
                            1 -> -0.168736f * r - 0.331264f * g + 0.5f * b
                            else -> 0.5f * r - 0.418688f * g - 0.081312f * b
                        }
                        val block = when (c) { 0 -> yBlock; 1 -> cbBlock; else -> crBlock }
                        block[yy * 8 + xx] = v.toInt()
                    }
                    val block = when (c) { 0 -> yBlock; 1 -> cbBlock; else -> crBlock }
                    val qtab = if (c == 0) lq else cq
                    val coef = forwardDct(block, qtab)
                    if (c == 0) {
                        encodeBlock(bitBuffer, coef, DC_LUMA_TABLE, AC_LUMA_TABLE, 0, dcState)
                    } else {
                        encodeBlock(bitBuffer, coef, DC_CHROMA_TABLE, AC_CHROMA_TABLE, c, dcState)
                    }
                }
                x += 8
            }
            y += 8
        }
        bitBuffer.flush()
        out.write(bmp.toByteArray())
        out.write(0xFF); out.write(0xD9)
        return out.toByteArray()
    }

    private val ZIGZAG = intArrayOf(
        0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5,
        12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28,
        35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51,
        58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63,
    )

    private fun writeDqt(out: ByteArrayOutputStream, id: Int, table: IntArray) {
        out.write(0xFF); out.write(0xDB)
        writeShort(out, 67)
        out.write(id)
        for (i in 0 until 64) out.write(table[i].coerceIn(1, 255))
    }

    /**
     * Huffman code tables indexed by *symbol*, which is what the encoder needs.
     *
     * The spec lists, per code length, how many symbols have that length, and
     * then the symbols in canonical order. An earlier version of this file used
     * that list as if it were indexed by symbol, so every emitted code was
     * wrong; the tables are now expanded with the canonical Huffman algorithm
     * (T.81 Annex C) and asserted to be complete before use.
     */
    private class HuffmanTable(counts: IntArray, symbols: IntArray) {
        val lengths = IntArray(256)
        val codes = IntArray(256)

        init {
            require(symbols.size == counts.sum()) {
                "Huffman table is inconsistent: ${symbols.size} symbols vs ${counts.sum()} counted"
            }
            var code = 0
            var k = 0
            for (len in 1..16) {
                for (n in 0 until counts[len - 1]) {
                    val symbol = symbols[k++]
                    lengths[symbol] = len
                    codes[symbol] = code
                    code++
                }
                code = code shl 1
            }
            // A complete table ends with all codes consumed; anything else means
            // the symbol list was not the canonical order.
            require(k == symbols.size) { "Huffman table did not consume all symbols" }
        }
    }

    private val DC_LUMA_TABLE by lazy { HuffmanTable(DC_LUMA_COUNTS, DC_LUMA) }
    private val DC_CHROMA_TABLE by lazy { HuffmanTable(DC_CHROMA_COUNTS, DC_CHROMA) }
    private val AC_LUMA_TABLE by lazy { HuffmanTable(AC_LUMA_COUNTS, AC_LUMA) }
    private val AC_CHROMA_TABLE by lazy { HuffmanTable(AC_CHROMA_COUNTS, AC_CHROMA) }

    private fun writeHuffmanTables(out: ByteArrayOutputStream) {
        fun table(id: Int, counts: IntArray, symbols: IntArray) {
            out.write(0xFF); out.write(0xC4)
            writeShort(out, 2 + 1 + 16 + symbols.size)
            out.write(id)
            for (c in counts) out.write(c)
            for (s in symbols) out.write(s)
        }
        table(0x00, DC_LUMA_COUNTS, DC_LUMA)
        table(0x10, AC_LUMA_COUNTS, AC_LUMA)
        table(0x01, DC_CHROMA_COUNTS, DC_CHROMA)
        table(0x11, AC_CHROMA_COUNTS, AC_CHROMA)
    }

    private fun forwardDct(block: IntArray, q: IntArray): IntArray {
        val out = IntArray(64)
        for (u in 0 until 8) for (v in 0 until 8) {
            var sum = 0.0
            for (y in 0 until 8) for (x in 0 until 8) {
                sum += block[y * 8 + x] * kotlin.math.cos((2 * x + 1) * u * Math.PI / 16) *
                    kotlin.math.cos((2 * y + 1) * v * Math.PI / 16)
            }
            val cu = if (u == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
            val cv = if (v == 0) 1.0 / kotlin.math.sqrt(2.0) else 1.0
            val v2 = 0.25 * cu * cv * sum
            out[v * 8 + u] = (v2 / q[v * 8 + u]).toInt()
        }
        return out
    }

    /**
     * Per-component DC predictors.
     *
     * Each component keeps its own predictor (T.81 §A.3.1); sharing one between
     * Cb and Cr silently corrupts colours because the decoder resets them
     * independently at the start of every restart-free scan.
     */
    private class DcState {
        var y = 0
        var cb = 0
        var cr = 0
    }

    private fun encodeBlock(
        bb: BitBuffer,
        coef: IntArray,
        dcTable: HuffmanTable,
        acTable: HuffmanTable,
        component: Int,
        state: DcState,
    ) {
        val prev = when (component) {
            0 -> state.y
            1 -> state.cb
            else -> state.cr
        }
        val dc = coef[0]
        val diff = dc - prev
        when (component) {
            0 -> state.y = dc
            1 -> state.cb = dc
            else -> state.cr = dc
        }
        val dcCat = bitLength(kotlin.math.abs(diff))
        if (dcCat > 11) error("JPEG DC category $dcCat out of range")
        bb.writeHuffman(dcCat, dcTable)
        if (dcCat > 0) bb.writeBits(if (diff < 0) diff - 1 else diff, dcCat)
        var run = 0
        for (i in 1 until 64) {
            val v = coef[ZIGZAG[i]]
            if (v == 0) { run++; continue }
            while (run > 15) {
                bb.writeHuffman(0xF0, acTable)
                run -= 16
            }
            val cat = bitLength(kotlin.math.abs(v))
            if (cat > 10) error("JPEG AC category $cat out of range for coefficient $v")
            val sym = (run shl 4) or cat
            bb.writeHuffman(sym, acTable)
            bb.writeBits(if (v < 0) v - 1 else v, cat)
            run = 0
        }
        if (run > 0) bb.writeHuffman(0x00, acTable)
    }

    private fun bitLength(v: Int): Int = when {
        v == 0 -> 0
        else -> 32 - Integer.numberOfLeadingZeros(v)
    }

    private fun writeShort(out: ByteArrayOutputStream, v: Int) {
        out.write((v ushr 8) and 0xFF)
        out.write(v and 0xFF)
    }

    private class BitBuffer(private val out: ByteArrayOutputStream) {
        private var acc = 0
        private var nBits = 0
        fun writeBits(value: Int, count: Int) {
            for (i in count - 1 downTo 0) {
                acc = (acc shl 1) or ((value ushr i) and 1)
                nBits++
                if (nBits == 8) {
                    out.write(acc and 0xFF)
                    if ((acc and 0xFF) == 0xFF) out.write(0)
                    acc = 0
                    nBits = 0
                }
            }
        }
        fun writeHuffman(symbol: Int, table: HuffmanTable) {
            val len = table.lengths[symbol]
            val code = table.codes[symbol]
            require(len > 0) { "Symbol 0x${symbol.toString(16)} has no Huffman code" }
            for (i in len - 1 downTo 0) {
                acc = (acc shl 1) or ((code ushr i) and 1)
                nBits++
                if (nBits == 8) {
                    out.write(acc and 0xFF)
                    if ((acc and 0xFF) == 0xFF) out.write(0)
                    acc = 0
                    nBits = 0
                }
            }
        }
        fun flush() {
            while (nBits != 0) writeBits(1, 1)
        }
    }

    // Standard tables (ITU T.81 Annex K).
    private val DC_LUMA_COUNTS = intArrayOf(0, 1, 5, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0)
    private val DC_CHROMA_COUNTS = intArrayOf(0, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0)
    private val DC_LUMA = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
    private val DC_CHROMA = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
    private val AC_LUMA_COUNTS = intArrayOf(0, 2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 0x7d)
    private val AC_CHROMA_COUNTS = intArrayOf(0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 0x77)
    private val AC_LUMA = intArrayOf(
        0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41, 0x06, 0x13, 0x51, 0x61, 0x07,
        0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xa1, 0x08, 0x23, 0x42, 0xb1, 0xc1, 0x15, 0x52, 0xd1, 0xf0,
        0x24, 0x33, 0x62, 0x72, 0x82, 0x09, 0x0a, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x25, 0x26, 0x27, 0x28,
        0x29, 0x2a, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48, 0x49,
        0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69,
        0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89,
        0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7,
        0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3, 0xc4, 0xc5,
        0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xe1, 0xe2,
        0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
        0xf9, 0xfa,
    )
    private val AC_CHROMA = intArrayOf(
        0x00, 0x01, 0x02, 0x03, 0x11, 0x04, 0x05, 0x21, 0x31, 0x06, 0x12, 0x41, 0x51, 0x07, 0x61, 0x71,
        0x13, 0x22, 0x32, 0x81, 0x08, 0x14, 0x42, 0x91, 0xa1, 0xb1, 0xc1, 0x09, 0x23, 0x33, 0x52, 0xf0,
        0x15, 0x62, 0x72, 0xd1, 0x0a, 0x16, 0x24, 0x34, 0xe1, 0x25, 0xf1, 0x17, 0x18, 0x19, 0x1a, 0x26,
        0x27, 0x28, 0x29, 0x2a, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48,
        0x49, 0x4a, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
        0x69, 0x6a, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87,
        0x88, 0x89, 0x8a, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9a, 0xa2, 0xa3, 0xa4, 0xa5,
        0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xc2, 0xc3,
        0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda,
        0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8,
        0xf9, 0xfa,
    )

    // Canonical Huffman codes derived from the standard tables.

    }

/** Minimal lossless TIFF (RGBA, uncompressed, single strip). */
object TiffWriter {
    fun write(pixmap: Pixmap): ByteArray {
        val w = pixmap.width
        val h = pixmap.height
        val dataSize = w * h * 4
        val out = ByteArrayOutputStream(dataSize + 512)
        fun le16(v: Int) { out.write(v and 0xFF); out.write((v ushr 8) and 0xFF) }
        fun le32(v: Int) { le16(v and 0xFFFF); le16((v ushr 16) and 0xFFFF) }
        out.write('I'.code); out.write('I'.code); le16(42); le32(8)
        // IFD at offset 8, entries: 11
        val entries = 11
        val ifdOffset = 8
        val ifdSize = 2 + entries * 12 + 4
        val dataOffset = ifdOffset + ifdSize
        le16(entries)
        fun entry(tag: Int, type: Int, count: Int, value: Int) {
            le16(tag); le16(type); le32(count); le32(value)
        }
        entry(256, 3, 1, w)
        entry(257, 3, 1, h)
        entry(258, 3, 4, dataOffset)      // BitsPerSample array pointer
        entry(259, 3, 1, 1)               // no compression
        entry(262, 3, 1, 2)               // RGB
        entry(273, 4, 1, dataOffset + 8)  // StripOffsets
        entry(277, 3, 1, 4)               // SamplesPerPixel
        entry(278, 3, 1, h)               // RowsPerStrip
        entry(279, 4, 1, dataSize)        // StripByteCounts
        entry(284, 3, 1, 1)               // PlanarConfig chunky
        entry(338, 3, 1, 2)               // ExtraSamples: unassociated alpha
        le32(0)                           // next IFD
        // BitsPerSample values
        le16(8); le16(8); le16(8); le16(8)
        for (i in 0 until w * h) {
            val p = pixmap.pixels[i]
            val a = alphaOf(p)
            out.write(if (a == 0) 0 else (redOf(p) * 255 + a / 2) / a)
            out.write(if (a == 0) 0 else (greenOf(p) * 255 + a / 2) / a)
            out.write(if (a == 0) 0 else (blueOf(p) * 255 + a / 2) / a)
            out.write(a)
        }
        return out.toByteArray()
    }
}

/**
 * Minimal single-page PDF 1.4 writer embedding a lossless FlateDecode RGB image.
 * Text-free, dependency-free, and valid for any PDF reader; sizes are written in
 * points at 72 DPI so the exported page matches the document's physical size.
 */
object PdfWriter {
    fun write(pixmap: Pixmap, title: String): ByteArray {
        val w = pixmap.width
        val h = pixmap.height
        // RGB, alpha flattened onto white, Flate-compressed.
        val raw = ByteArray(w * h * 3)
        var o = 0
        for (i in 0 until w * h) {
            val p = pixmap.pixels[i]
            val a = alphaOf(p)
            val inv = 255 - a
            val r = if (a == 0) 255 else (redOf(p) * 255 + a / 2) / a
            val g = if (a == 0) 255 else (greenOf(p) * 255 + a / 2) / a
            val b = if (a == 0) 255 else (blueOf(p) * 255 + a / 2) / a
            raw[o++] = ((r * a + 255 * inv) / 255).toByte()
            raw[o++] = ((g * a + 255 * inv) / 255).toByte()
            raw[o++] = ((b * a + 255 * inv) / 255).toByte()
        }
        val compressed = java.io.ByteArrayOutputStream(raw.size / 3 + 64)
        java.util.zip.DeflaterOutputStream(compressed, java.util.zip.Deflater(6)).use { it.write(raw) }
        val imageData = compressed.toByteArray()

        val safeTitle = title.replace("(", "\\(").replace(")", "\\)")
        val objects = ArrayList<ByteArray>()
        objects.add("<< /Type /Catalog /Pages 2 0 R >>".toByteArray())
        objects.add("<< /Type /Pages /Kids [3 0 R] /Count 1 >>".toByteArray())
        objects.add(
            ("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 $w $h] " +
                "/Resources << /XObject << /Im0 5 0 R >> /ProcSet [/PDF /ImageC] >> " +
                "/Contents 4 0 R >>").toByteArray()
        )
        val content = "q $w 0 0 $h 0 0 cm /Im0 Do Q".toByteArray()
        objects.add(
                        ("<< /Length ${content.size} >>\nstream\n" + String(content,
                Charsets.US_ASCII) + "\nendstream").toByteArray()
        )
        val imgHeader = ("<< /Type /XObject /Subtype /Image /Width $w /Height $h /ColorSpace /DeviceRGB " +
            "/BitsPerComponent 8 /Filter /FlateDecode /Length ${imageData.size} >>\nstream\n").toByteArray()
        val imgFooter = "\nendstream".toByteArray()
        objects.add(imgHeader + imageData + imgFooter)
        val info = "<< /Title ($safeTitle) /Producer (Mona Art) /Creator (Mona Art) >>".toByteArray()

        val out = java.io.ByteArrayOutputStream(imageData.size + 2048)
        out.write("%PDF-1.4\n".toByteArray())
                out.write(byteArrayOf('%'.code.toByte(), 0xE2.toByte(), 0xE3.toByte(), 0xCF.toByte(), 0xD3.toByte(),
            '\n'.code.toByte()))
        val offsets = IntArray(objects.size + 2)
        for ((i, body) in objects.withIndex()) {
            offsets[i + 1] = out.size()
            out.write("${i + 1} 0 obj\n".toByteArray())
            out.write(body)
            out.write("\nendobj\n".toByteArray())
        }
        val infoIndex = objects.size + 1
        offsets[infoIndex] = out.size()
        out.write("$infoIndex 0 obj\n".toByteArray())
        out.write(info)
        out.write("\nendobj\n".toByteArray())
        val xrefPos = out.size()
        out.write("xref\n0 ${infoIndex + 1}\n".toByteArray())
        out.write("0000000000 65535 f \n".toByteArray())
        for (i in 1..infoIndex) {
            out.write("%010d 00000 n \n".format(offsets[i]).toByteArray())
        }
        out.write("trailer\n<< /Size ${infoIndex + 1} /Root 1 0 R /Info $infoIndex 0 R >>\n".toByteArray())
        out.write("startxref\n$xrefPos\n%%EOF\n".toByteArray())
        return out.toByteArray()
    }
}
