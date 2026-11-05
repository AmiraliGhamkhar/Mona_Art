package com.monaart.studio.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Bounded decoding of images received from outside the app (ACTION_VIEW,
 * file pickers). Nothing here depends on a specific screen so it can be unit
 * tested with Robolectric against real encoded bytes.
 */
object IncomingFiles {
    /** Cap on a single incoming file: ~512 MiB covers every real project/image. */
    const val MAX_FILE_BYTES: Int = 512 * 1024 * 1024

    /**
     * Pixel budget for one decoded image. A 24 MP bitmap in ARGB_8888 costs
     * ~96 MiB, and importing also builds tiles plus history, so a larger
     * allocation would risk OOM on mid-range devices.
     */
    const val MAX_PIXEL_COUNT: Long = 24_000_000L

    /**
     * Inspects [bytes] without allocating the full image, then decodes with the
     * smallest power-of-two sample size that keeps it inside [maxPixels]. Returns
     * null when the data is not a decodable image.
     */
    fun decodeImage(bytes: ByteArray, maxPixels: Long = MAX_PIXEL_COUNT): Bitmap? {
        if (bytes.isEmpty()) return null
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while ((bounds.outWidth / sample.toLong()) * (bounds.outHeight / sample.toLong()) > maxPixels) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
