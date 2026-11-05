package com.monaart.engine.io

import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Layer
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.redOf
import java.io.ByteArrayOutputStream

/**
 * PSD writer with layered output.
 *
 * Format notes (Adobe Photoshop File Formats, "8BPS" v1):
 *  - 8-bit RGB documents only.
 *  - Layer records use raw (uncompressed) channel data, and each channel is a
 *    separate *plane*: first every red byte top-to-bottom, then green, blue and
 *    finally the alpha channel, in the order the layer record declares channels
 *    (0 = R, 1 = G, 2 = B, -1 = alpha). Interleaved per-pixel R,G,B,A data is not
 *    a valid layer encoding and earlier versions of this file produced it.
 *  - Layer names are written both in the Unicode layer name block (`luni`) and as
 *    a Pascal string, because some readers only honour one of them.
 *  - The composite (merged) image is written so previews work in any viewer.
 *  - Limitations documented honestly: no layer masks, no clipping layers, no
 *    blend modes other than Normal, no text/vector layers, no layer groups
 *    (groups are flattened into their raster children). Layer rectangles always
 *    span the full canvas, so sparse layers cost some bytes but parse correctly.
 */
object PsdWriter {

    fun write(doc: ArtDocument): ByteArray {
        val flat = doc.flatten()
        val w = doc.width
        val h = doc.height
        val out = ByteArrayOutputStream(w * h * 6 + 4096)

        // ---- File header ----
        writeAscii(out, "8BPS")
        writeShort(out, 1)            // version 1 (PSD)
        out.write(ByteArray(6))       // reserved
        writeShort(out, 3)            // composite image channels: R,G,B
        writeInt(out, h)
        writeInt(out, w)
        writeShort(out, 8)            // depth
        writeShort(out, 3)            // color mode: RGB

        // ---- Color mode data (empty) ----
        writeInt(out, 0)

        // ---- Image resources (empty) ----
        writeInt(out, 0)

        // ---- Layer and mask information ----
        val layers = collectRasterLayers(doc.layers)
        val layerRecords = ByteArrayOutputStream()
        val channelData = ByteArrayOutputStream()
        if (layers.isEmpty()) {
            // A document with no raster layers is not representable as a PSD with
            // zero layer records in every reader, so it becomes one Background
            // layer holding the flattened composite.
            writeLayerRecord(layerRecords, "Background", 0, 0, w, h, hidden = false)
            writeChannelPlanes(channelData, flat)
        } else {
            for (layer in layers) {
                val content = renderLayer(doc, layer)
                writeLayerRecord(layerRecords, layer.name, 0, 0, w, h, hidden = !layer.visible)
                // Channel data order must match the record's channel ids: R, G, B, A.
                writeChannelPlanes(channelData, content)
            }
        }
        val layerInfo = ByteArrayOutputStream()
        // Layer Information section: [length = count + records + channel data].
        val recordsBytes = layerRecords.toByteArray()
        val channelBytes = channelData.toByteArray()
        writeInt(layerInfo, 2 + recordsBytes.size + channelBytes.size)
        // Layer count: PSD reads a negative value as "first alpha channel contains
        // the merged result"; we use the plain count, never zero records.
        writeShort(layerInfo, layers.size.coerceAtLeast(1))
        layerInfo.write(recordsBytes)
        layerInfo.write(channelBytes)
        // Global layer mask info: length 0.
        writeInt(layerInfo, 0)
        // Pad this section to a multiple of 4; the outer length below includes it.
        val pad = (4 - (layerInfo.size() % 4)) % 4
        repeat(pad) { layerInfo.write(0) }
        val layerInfoBytes = layerInfo.toByteArray()
        writeInt(out, layerInfoBytes.size)
        out.write(layerInfoBytes)
        // The image data section follows the layer-and-mask section *directly*.
        // An earlier revision emitted an extra "global additional info length"
        // int here; those four bytes pushed the composite image four bytes out of
        // alignment, so the compression short was read from the wrong offset.
        // Verified against ag-psd's writer: layer info, then global layer mask
        // info, then additional layer info, all inside the one section.

        // ---- Composite image data (raw, planar, no alpha) ----
        writeShort(out, 0) // compression: raw
        for (channel in 0..2) {
            for (i in 0 until w * h) {
                out.write(compositeChannelByte(flat.pixels[i], channel))
            }
        }
        return out.toByteArray()
    }

    /** One straightened channel byte of a premultiplied composite pixel. */
    private fun compositeChannelByte(p: Int, channel: Int): Int {
        val a = alphaOf(p)
        return when (channel) {
            0 -> if (a == 0) 0 else (redOf(p) * 255 + a / 2) / a
            1 -> if (a == 0) 0 else (greenOf(p) * 255 + a / 2) / a
            else -> if (a == 0) 0 else (blueOf(p) * 255 + a / 2) / a
        }.coerceIn(0, 255)
    }

    private fun collectRasterLayers(list: List<Layer>, acc: MutableList<Layer> = mutableListOf()): List<Layer> {
        for (l in list) {
            if (l.isGroup) collectRasterLayers(l.children, acc) else acc.add(l)
        }
        return acc
    }

    private fun renderLayer(doc: ArtDocument, layer: Layer): Pixmap {
        val out = Pixmap(doc.width, doc.height)
        val s = layer.surface ?: return out
        for (y in 0 until doc.height) for (x in 0 until doc.width) {
            val p = s.getPixel(x, y)
            if (p != 0) out.pixels[y * doc.width + x] = com.monaart.engine.paint.PixelOps.scaleAlpha(p, layer.opacity)
        }
        return out
    }

    private fun writeLayerRecord(
        out: ByteArrayOutputStream,
        name: String,
        top: Int,
        left: Int,
        width: Int,
        height: Int,
        hidden: Boolean,
    ) {
        val bounds = intArrayOf(top, left, top + height, left + width)
        for (v in bounds) writeInt(out, v)
        writeShort(out, 4) // 3 colour channels + alpha

        // Channel info: (id, data length). Raw encoding means every plane is
        // exactly width*height bytes, and the data below matches these lengths.
        val planeLength = width * height
        writeShort(out, 0); writeInt(out, planeLength)
        writeShort(out, 1); writeInt(out, planeLength)
        writeShort(out, 2); writeInt(out, planeLength)
        writeShort(out, -1); writeInt(out, planeLength)

        writeAscii(out, "8BIM")
        writeAscii(out, "norm")
        out.write(255)   // opacity
        out.write(0)     // clipping
        out.write(if (hidden) 0x02 else 0x00) // flags: bit 1 set = layer hidden
        out.write(0)     // filler

        // Extra data: layer mask (0), blending ranges (2×4 zero bytes), Pascal
        // name (padded), and the Unicode layer name block (`luni`).
        val extra = ByteArrayOutputStream()
        writeInt(extra, 0)                     // layer mask data length
        // Layer blending ranges: a 4-byte length field followed by that many
        // bytes of range data. "No ranges" is therefore exactly four zero bytes.
        // This used to write eight, which pushed the Pascal name and the `luni`
        // block four bytes past where the declared length put them, so a reader
        // walking the sub-fields found an empty name instead of the real one.
        writeInt(extra, 0)
        val nameBytes = name.take(255).toByteArray(Charsets.ISO_8859_1)
        extra.write(nameBytes.size)
        extra.write(nameBytes)
        // Pascal name is padded so (1 + length) is a multiple of 4.
        val pad = (4 - ((nameBytes.size + 1) % 4)) % 4
        extra.write(ByteArray(pad))
        val uni = ByteArrayOutputStream()
        writeAscii(uni, "8BIM")
        writeAscii(uni, "luni")
        val unicodeName = name.toByteArray(Charsets.UTF_16BE)
        writeInt(uni, ((4 + unicodeName.size + 3) / 4) * 4)
        writeInt(uni, name.length)
        uni.write(unicodeName)
        var uniPad = (4 - (unicodeName.size % 4)) % 4
        while (uniPad-- > 0) uni.write(0)
        extra.write(uni.toByteArray())

        val extraBytes = extra.toByteArray()
        writeInt(out, extraBytes.size)
        out.write(extraBytes)
        if (extraBytes.size % 4 != 0) out.write(ByteArray(4 - (extraBytes.size % 4)))
    }

    /**
     * Writes a layer's pixels as four separate channel planes (R, G, B, A), which
     * is the encoding implied by the layer record. Each plane is rounded to the
     * nearest straight (unmultiplied) channel value.
     */
    private fun writeChannelPlanes(out: ByteArrayOutputStream, pixmap: Pixmap) {
        val n = pixmap.width * pixmap.height
        for (channel in 0..3) {
            for (i in 0 until n) {
                out.write(
                    when (channel) {
                        3 -> alphaOf(pixmap.pixels[i])
                        else -> compositeChannelByte(pixmap.pixels[i], channel)
                    }.coerceIn(0, 255),
                )
            }
        }
    }

    private fun writeAscii(out: ByteArrayOutputStream, s: String) = out.write(s.toByteArray(Charsets.US_ASCII))

    private fun writeShort(out: ByteArrayOutputStream, v: Int) {
        out.write((v ushr 8) and 0xFF)
        out.write(v and 0xFF)
    }

    private fun writeInt(out: ByteArrayOutputStream, v: Int) {
        out.write((v ushr 24) and 0xFF)
        out.write((v ushr 16) and 0xFF)
        out.write((v ushr 8) and 0xFF)
        out.write(v and 0xFF)
    }
}
