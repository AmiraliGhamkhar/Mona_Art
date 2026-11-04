package com.monaart.engine.io

import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.Inflater

/**
 * Dependency-free PNG encoder/decoder operating on premultiplied pixmaps.
 *
 * Writing our own (instead of Bitmap.compress) means PNG export is verifiable in
 * JVM tests and identical on every device, and lets us stream straight-alpha
 * data without an intermediate Android Bitmap.
 */
object PngCodec {

    /** Encodes [pixmap] as a straight-alpha 8-bit RGBA PNG. */
    fun encode(pixmap: Pixmap, compression: Int = 6): ByteArray {
        val w = pixmap.width
        val h = pixmap.height
        val raw = ByteArray(h * (1 + w * 4))
        var o = 0
        for (y in 0 until h) {
            raw[o++] = 0 // filter: none (simple + fast; deflate still compresses well)
            for (x in 0 until w) {
                val p = pixmap.pixels[y * w + x]
                if (p == 0) {
                    o += 4
                    continue
                }
                val a = alphaOf(p)
                if (a == 255) {
                    raw[o++] = redOf(p).toByte()
                    raw[o++] = greenOf(p).toByte()
                    raw[o++] = blueOf(p).toByte()
                    raw[o++] = 255.toByte()
                } else {
                    raw[o++] = ((redOf(p) * 255 + a / 2) / a).toByte()
                    raw[o++] = ((greenOf(p) * 255 + a / 2) / a).toByte()
                    raw[o++] = ((blueOf(p) * 255 + a / 2) / a).toByte()
                    raw[o++] = a.toByte()
                }
            }
        }
        val out = ByteArrayOutputStream(raw.size / 2 + 1024)
        out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        val ihdr = ByteArray(13)
        writeInt(ihdr, 0, w)
        writeInt(ihdr, 4, h)
        ihdr[8] = 8   // bit depth
        ihdr[9] = 6   // colour type RGBA
        ihdr[10] = 0  // compression
        ihdr[11] = 0  // filter
        ihdr[12] = 0  // interlace
        writeChunk(out, "IHDR", ihdr)
        val compressed = ByteArrayOutputStream(raw.size / 3 + 64)
        DeflaterOutputStream(compressed, Deflater(compression)).use { it.write(raw) }
        writeChunk(out, "IDAT", compressed.toByteArray())
        writeChunk(out, "IEND", ByteArray(0))
        return out.toByteArray()
    }

    fun encodeTo(pixmap: Pixmap, out: OutputStream, compression: Int = 6) {
        out.write(encode(pixmap, compression))
        out.flush()
    }

    /** Decodes an RGBA/RGB/grayscale PNG into a premultiplied pixmap. */
    fun decode(bytes: ByteArray): Pixmap {
        require(bytes.size > 8 && bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte()) { "Not a PNG file" }
        var pos = 8
        var w = 0
        var h = 0
        var bitDepth = 8
        var colorType = 6
        val idat = ByteArrayOutputStream()
        var palette: ByteArray? = null
        var trns: ByteArray? = null
        while (pos + 8 <= bytes.size) {
            val len = readInt(bytes, pos)
            val type = String(bytes, pos + 4, 4, Charsets.US_ASCII)
            pos += 8
            if (pos + len > bytes.size) break
            when (type) {
                "IHDR" -> {
                    w = readInt(bytes, pos)
                    h = readInt(bytes, pos + 4)
                    bitDepth = bytes[pos + 8].toInt() and 0xFF
                    colorType = bytes[pos + 9].toInt() and 0xFF
                    require(bytes[pos + 12].toInt() == 0) { "Interlaced PNG not supported" }
                }
                "PLTE" -> palette = bytes.copyOfRange(pos, pos + len)
                "tRNS" -> trns = bytes.copyOfRange(pos, pos + len)
                "IDAT" -> idat.write(bytes, pos, len)
            }
            pos += len + 4
            if (type == "IEND") break
        }
        require(w > 0 && h > 0) { "Invalid PNG dimensions" }
        val bpp = when (colorType) {
            0 -> 1; 2 -> 3; 3 -> 1; 4 -> 2; 6 -> 4
            else -> throw IllegalArgumentException("Unsupported PNG color type $colorType")
        }
        require(bitDepth == 8 || (colorType == 3 && bitDepth <= 8)) { "Unsupported PNG bit depth $bitDepth" }
        val inflated = inflate(idat.toByteArray())
        val stride = 1 + w * bpp
        val rows = Array(h) { IntArray(w) }
        val prev = ByteArray(w * bpp)
        val cur = ByteArray(w * bpp)
        for (y in 0 until h) {
            val base = y * stride
            val filter = inflated[base].toInt() and 0xFF
            System.arraycopy(inflated, base + 1, cur, 0, w * bpp)
            unfilter(filter, cur, prev, bpp)
            for (x in 0 until w) {
                val i = x * bpp
                rows[y][x] = when (colorType) {
                    0 -> {
                        val g = cur[i].toInt() and 0xFF
                        (255 shl 24) or (g shl 16) or (g shl 8) or g
                    }
                    2 -> (255 shl 24) or ((cur[i].toInt() and 0xFF) shl 16) or
                        ((cur[i + 1].toInt() and 0xFF) shl 8) or (cur[i + 2].toInt() and 0xFF)
                    3 -> {
                        val idx = cur[i].toInt() and 0xFF
                        val pl = palette ?: throw IllegalArgumentException("PNG palette missing")
                        val a = if (trns != null && idx < trns.size) trns[idx].toInt() and 0xFF else 255
                        val r = pl[idx * 3].toInt() and 0xFF
                        val g = pl[idx * 3 + 1].toInt() and 0xFF
                        val b = pl[idx * 3 + 2].toInt() and 0xFF
                        (a shl 24) or (r shl 16) or (g shl 8) or b
                    }
                    4 -> {
                        val g = cur[i].toInt() and 0xFF
                        val a = cur[i + 1].toInt() and 0xFF
                        (a shl 24) or (g shl 16) or (g shl 8) or g
                    }
                    else -> ((cur[i + 3].toInt() and 0xFF) shl 24) or
                        ((cur[i].toInt() and 0xFF) shl 16) or
                        ((cur[i + 1].toInt() and 0xFF) shl 8) or
                        (cur[i + 2].toInt() and 0xFF)
                }
            }
            System.arraycopy(cur, 0, prev, 0, cur.size)
        }
        val out = Pixmap(w, h)
        for (y in 0 until h) for (x in 0 until w) {
            out.pixels[y * w + x] = com.monaart.engine.paint.straightToPremul(rows[y][x])
        }
        return out
    }

    private fun unfilter(filter: Int, cur: ByteArray, prev: ByteArray, bpp: Int) {
        when (filter) {
            0 -> {}
            1 -> for (i in bpp until cur.size) {
                cur[i] = ((cur[i].toInt() and 0xFF) + (cur[i - bpp].toInt() and 0xFF)).toByte()
            }
            2 -> for (i in cur.indices) {
                cur[i] = ((cur[i].toInt() and 0xFF) + (prev[i].toInt() and 0xFF)).toByte()
            }
            3 -> for (i in cur.indices) {
                val left = if (i >= bpp) cur[i - bpp].toInt() and 0xFF else 0
                cur[i] = ((cur[i].toInt() and 0xFF) + ((left + (prev[i].toInt() and 0xFF)) / 2)).toByte()
            }
            4 -> for (i in cur.indices) {
                val a = if (i >= bpp) cur[i - bpp].toInt() and 0xFF else 0
                val b = prev[i].toInt() and 0xFF
                val c = if (i >= bpp) prev[i - bpp].toInt() and 0xFF else 0
                val p = a + b - c
                val pa = kotlin.math.abs(p - a)
                val pb = kotlin.math.abs(p - b)
                val pc = kotlin.math.abs(p - c)
                val pred = if (pa <= pb && pa <= pc) a else if (pb <= pc) b else c
                cur[i] = ((cur[i].toInt() and 0xFF) + pred).toByte()
            }
            else -> throw IllegalArgumentException("Unknown PNG filter $filter")
        }
    }

    private fun inflate(data: ByteArray): ByteArray {
        val inf = Inflater()
        inf.setInput(data)
        val out = ByteArrayOutputStream(data.size * 4)
        val buf = ByteArray(64 * 1024)
        while (!inf.finished()) {
            val n = inf.inflate(buf)
            if (n == 0) {
                if (inf.needsInput() || inf.needsDictionary()) break
            }
            out.write(buf, 0, n)
        }
        inf.end()
        return out.toByteArray()
    }

    private fun writeChunk(out: ByteArrayOutputStream, type: String, data: ByteArray) {
        val len = ByteArray(4)
        writeInt(len, 0, data.size)
        out.write(len)
        val typeBytes = type.toByteArray(Charsets.US_ASCII)
        out.write(typeBytes)
        out.write(data)
        val crc = CRC32()
        crc.update(typeBytes)
        crc.update(data)
        val c = ByteArray(4)
        writeInt(c, 0, crc.value.toInt())
        out.write(c)
    }

    private fun writeInt(b: ByteArray, off: Int, v: Int) {
        b[off] = (v ushr 24).toByte()
        b[off + 1] = (v ushr 16).toByte()
        b[off + 2] = (v ushr 8).toByte()
        b[off + 3] = v.toByte()
    }

    private fun readInt(b: ByteArray, off: Int): Int =
        ((b[off].toInt() and 0xFF) shl 24) or ((b[off + 1].toInt() and 0xFF) shl 16) or
            ((b[off + 2].toInt() and 0xFF) shl 8) or (b[off + 3].toInt() and 0xFF)
}
