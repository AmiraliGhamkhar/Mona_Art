package com.monaart.studio.domain.model

/** Gallery/sort/filter state. */
enum class ProjectSort(val label: String) {
    RecentlyEdited("Last edited"),
    RecentlyCreated("Date created"),
    Name("Name"),
    Size("Canvas size"),
}

enum class ProjectFilter(val label: String) {
    All("All"),
    Favorites("Favorites"),
    Recent("Last 7 days"),
    Large("Large canvases"),
}

/** Lightweight project metadata shown in the gallery (no pixel data). */
data class ProjectSummary(
    val id: Long,
    val name: String,
    val width: Int,
    val height: Int,
    val dpi: Int,
    val layerCount: Int,
    val createdAt: Long,
    val modifiedAt: Long,
    val thumbnailPath: String?,
    val filePath: String,
    val sizeBytes: Long,
    val favorite: Boolean = false,
    val folder: String? = null,
    val lastOpenedAt: Long = 0,
    val recoveryState: RecoveryState = RecoveryState.Ok,
)

/** Health of a project file, surfaced in the gallery and document settings. */
enum class RecoveryState { Ok, Recovered, Missing, Corrupt }

/** A saved canvas preset. */
data class CanvasPreset(
    val id: String,
    val name: String,
    val width: Int,
    val height: Int,
    val dpi: Int = 300,
    val transparent: Boolean = false,
    val backgroundArgb: Int = 0xFFFFFFFF.toInt(),
    val custom: Boolean = false,
)

/** Values chosen in the New Canvas screen. */
data class CanvasSpec(
    val width: Int = 2048,
    val height: Int = 2048,
    val dpi: Int = 300,
    val transparent: Boolean = false,
    val backgroundArgb: Int = 0xFFFFFFFF.toInt(),
    val name: String = "Untitled",
    val presetId: String? = "square-2048",
) {
    val pixels: Long get() = width.toLong() * height.toLong()
    val megapixels: Float get() = pixels / 1_000_000f

    /** One ARGB layer's memory cost at full resolution (only painted tiles are allocated). */
    val estimatedLayerBytes: Long get() = pixels * 4

    val aspectLabel: String get() {
        val g = gcd(width, height)
        val w = width / g
        val h = height / g
        return if (w > 32 || h > 32) "%.2f:1".format(width.toFloat() / height) else "$w:$h"
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    companion object {
        val presets: List<CanvasPreset> = listOf(
            CanvasPreset("square-2048", "Square 2K", 2048, 2048, 300),
            CanvasPreset("square-4096", "Square 4K", 4096, 4096, 300),
            CanvasPreset("hd", "HD 1280 × 720", 1280, 720, 72),
            CanvasPreset("fhd", "Full HD 1920 × 1080", 1920, 1080, 72),
            CanvasPreset("2k", "QHD 2560 × 1440", 2560, 1440, 72),
            CanvasPreset("4k", "UHD 3840 × 2160", 3840, 2160, 72),
            CanvasPreset("a4-300", "A4 portrait · 300 DPI", 2480, 3508, 300),
            CanvasPreset("a4-landscape-300", "A4 landscape · 300 DPI", 3508, 2480, 300),
            CanvasPreset("letter-300", "US Letter · 300 DPI", 2550, 3300, 300),
            CanvasPreset("instagram", "Instagram post 1080", 1080, 1080, 72),
            CanvasPreset("instagram-story", "Story 1080 × 1920", 1080, 1920, 72),
            CanvasPreset("webtoon", "Webtoon strip 800 × 3000", 800, 3000, 144),
            CanvasPreset("comic-page", "Comic page · 300 DPI", 2550, 3855, 300),
            CanvasPreset("transparent-icon", "Transparent icon 1024", 1024, 1024, 144, transparent = true, backgroundArgb = 0),
        )

        fun fromPreset(p: CanvasPreset, name: String = "Untitled"): CanvasSpec = CanvasSpec(
            width = p.width, height = p.height, dpi = p.dpi,
            transparent = p.transparent, backgroundArgb = p.backgroundArgb,
            name = name, presetId = p.id,
        )

        /** Warnings that must be shown before creating an oversized canvas. */
        fun warnings(spec: CanvasSpec, deviceMaxTexture: Int = 8192): List<String> {
            val out = mutableListOf<String>()
            if (spec.width > deviceMaxTexture || spec.height > deviceMaxTexture) {
                out += "Larger than ${deviceMaxTexture}px on one side: display may be limited on this device."
            }
            if (spec.megapixels > 30f) {
                out += "%.1f MP canvas: expect slower compositing and larger project files.".format(spec.megapixels)
            }
            if (spec.dpi < 72) out += "Below 72 DPI is unusual for print work."
            if (spec.transparent && spec.dpi > 400) out += "Very high DPI with transparency increases memory use."
            return out
        }
    }
}
