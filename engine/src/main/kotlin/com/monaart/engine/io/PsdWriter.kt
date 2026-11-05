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
 * Design notes / interoperability:
 *  - 8-bit RGB ("8BPS") documents only, which is what every editor reads.
 *  - Layer records use raw (uncompressed) channel data for maximum compatibility.
 *  - Layer names are written both in the Unicode layer name block (`luni`) and as
 *    a Pascal string, because some readers only honour one of them.
 *  - Composite (merged) image data is included so previews work in any viewer.
 *  - Limitations: no masks, no clipping layers, no blend modes other than Normal,
 *    no text/vector layers, no layer groups (groups are flattened into their
 *    children in the output). Adjustments made in Mona Art remain raster.
 */
object PsdWriter {

    fun write(doc: ArtDocument): ByteArray {
        val flat = doc.flatten()
        val out = ByteArrayOutputStream(flat.width * flat.height * 6 + 4096)

        // ---- File header ----
        writeAscii(out, "8BPS")
        writeShort(out, 1)            // version 1 (PSD)
        out.write(ByteArray(6))       // reserved
        writeShort(out, 3)            // channels: R,G,B
        writeInt(out, doc.height)
        writeInt(out, doc.width)
        writeShort(out, 8)            // depth
        writeShort(out, 3)            // color mode: RGB

        // ---- Color mode data ----
        writeInt(out, 0)

        // ---- Image resources (empty) ----
        writeInt(out, 0)

        // ---- Layer and mask information ----
        val layerInfo = ByteArrayOutputStream()
        val layers = collectRasterLayers(doc.layers)
        writeShort(layerInfo, layers.size.coerceAtLeast(1))
        val layerRecords = ByteArrayOutputStream()
        val channelData = ByteArrayOutputStream()
        if (layers.isEmpty()) {
            writeLayerRecord(layerRecords, "Background", 0, 0, doc.width, doc.height, emptyChannel = true, hidden = false)
            writeChannelData(channelData, flat)
        } else {
            for ((index, layer) in layers.withIndex()) {
                val content = renderLayer(doc, layer)
                writeLayerRecord(layerRecords, layer.name, 0, 0, doc.width, doc.height, emptyChannel = false, hidden = !layer.visible)
                writeChannelData(channelData, content)
            }
        }
        val layerRecordsBytes = layerRecords.toByteArray()
        val channelDataBytes = channelData.toByteArray()
        writeInt(layerInfo, layerRecordsBytes.size)
        layerInfo.write(layerRecordsBytes)
        layerInfo.write(channelDataBytes)
        // Global layer mask info: length 0
        writeInt(layerInfo, 0)

        val layerInfoBytes = layerInfo.toByteArray()
        writeInt(out, layerInfoBytes.size)
        out.write(layerInfoBytes)
        writeInt(out, 0) // global additional info

        // ---- Composite image data (raw) ----
        writeShort(out, 0)
        val w = doc.width
        val h = doc.height
        for (channel in 0..2) {
            for (i in 0 until w * h) {
                val p = flat.pixels[i]
                val a = alphaOf(p)
                val straight = when (channel) {
                    0 -> if (a == 0) 0 else (redOf(p) * 255 + a / 2) / a
                    1 -> if (a == 0) 0 else (greenOf(p) * 255 + a / 2) / a
                    else -> if (a == 0) 0 else (blueOf(p) * 255 + a / 2) / a
                }
                out.write(straight.coerceIn(0, 255))
            }
        }
        return out.toByteArray()
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
        emptyChannel: Boolean,
        hidden: Boolean,
    ) {
        val bounds = intArrayOf(top, left, top + height, left + width)
        for (v in bounds) writeInt(out, v)
        writeShort(out, 4) // 3 colour channels + alpha

        // Channel info: (id, data length). Data length is filled with a fixed value
        // later implicitly: we write raw data of w*h bytes per channel.
        val channelLength = width * height
        writeShort(out, 0); writeInt(out, channelLength)
        writeShort(out, 1); writeInt(out, channelLength)
        writeShort(out, 2); writeInt(out, channelLength)
        writeShort(out, -1); writeInt(out, channelLength)

        writeAscii(out, "8BIM")
        writeAscii(out, "norm")
        out.write(255)   // opacity
        out.write(0)     // clipping
        out.write(if (hidden) 0x02 else 0x00) // flags: hidden
        out.write(0)     // filler

        // Extra data: layer mask (0), blending ranges (2x4 zero), name (pascal), unicode name.
        val extra = ByteArrayOutputStream()
        writeInt(extra, 0)              // layer mask data length
        writeInt(extra, 0); writeInt(extra, 0) // blending ranges
        val nameBytes = name.take(255).toByteArray(Charsets.ISO_8859_1)
        extra.write(nameBytes.size)
        extra.write(nameBytes)
        // Pad name to a multiple of 4 bytes.
        val pad = (4 - ((nameBytes.size + 1) % 4)) % 4
        extra.write(ByteArray(pad))
        // Unicode layer name block.
        val uni = ByteArrayOutputStream()
        writeAscii(uni, "8BIM")
        writeAscii(uni, "luni")
        val unicodeName = name.toByteArray(Charsets.UTF_16BE)
        val luniLen = 4 + unicodeName.size
        writeInt(uni, ((luniLen + 3) / 4) * 4)
        writeInt(uni, name.length)
        uni.write(unicodeName)
        var pad2 = (4 - (unicodeName.size % 4)) % 4
        while (pad2-- > 0) uni.write(0)
        extra.write(uni.toByteArray())

        val extraBytes = extra.toByteArray()
        writeInt(out, extraBytes.size)
        out.write(extraBytes)
        if (emptyChannel) Unit
    }

    private fun writeChannelData(out: ByteArrayOutputStream, pixmap: Pixmap) {
        val n = pixmap.width * pixmap.height
        val channels = IntArray(4)
        for (i in 0 until n) {
            val p = pixmap.pixels[i]
            val a = alphaOf(p)
            channels[0] = if (a == 0) 0 else (redOf(p) * 255 + a / 2) / a
            channels[1] = if (a == 0) 0 else (greenOf(p) * 255 + a / 2) / a
            channels[2] = if (a == 0) 0 else (blueOf(p) * 255 + a / 2) / a
            channels[3] = a
            for (c in 0..3) out.write(channels[c].coerceIn(0, 255))
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
