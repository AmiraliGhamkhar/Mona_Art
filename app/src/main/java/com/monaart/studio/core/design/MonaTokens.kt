package com.monaart.studio.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens for Mona Art.
 *
 * The product uses one palette with three surface treatments (dark, light,
 * AMOLED) and a user-selectable accent. Colours live here and nowhere else:
 * every component reads them from [MonaTheme] so theming, contrast checks and
 * the "custom accent" setting all work from a single source of truth.
 */
@Immutable
data class MonaColors(
    val ground: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val borderStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentOn: Color,
    val accentMuted: Color,
    val violet: Color,
    val cyan: Color,
    val pink: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val canvasBackdrop: Color,
    val selection: Color,
    val isDark: Boolean,
) {
    /** Contrast-safe text colour for an arbitrary background. */
    fun on(background: Color): Color =
        if (background.luminance() > 0.55f) Color(0xFF14121A) else textPrimary
}

/** Corner radii tokens (the brief's 8–36dp scale). */
@Immutable
data class MonaShapes(
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 18.dp,
    val lg: Dp = 26.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 36.dp,
    val pill: Dp = 999.dp,
) {
    /** User preference for how round the UI should feel. */
    fun scaled(factor: Float): MonaShapes = copy(
        xs = (xs.value * factor).dp,
        sm = (sm.value * factor).dp,
        md = (md.value * factor).dp,
        lg = (lg.value * factor).dp,
        xl = (xl.value * factor).dp,
        xxl = (xxl.value * factor).dp,
    )
}

/** Spacing scale, also used by the "interface density" setting. */
@Immutable
data class MonaSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val gutter: Dp = 16.dp,
    val cardPadding: Dp = 18.dp,
    val touchTarget: Dp = 48.dp,
    val railWidth: Dp = 60.dp,
    val inspectorWidth: Dp = 300.dp,
) {
    fun dense(factor: Float): MonaSpacing = copy(
        sm = (sm.value * factor).dp,
        md = (md.value * factor).dp,
        lg = (lg.value * factor).dp,
        xl = (xl.value * factor).dp,
        xxl = (xxl.value * factor).dp,
        gutter = (gutter.value * factor).dp,
        cardPadding = (cardPadding.value * factor).dp,
    )
}

private fun Color.luminance(): Float {
    fun channel(c: Float): Float = if (c <= 0.03928f) {
        c / 12.92f
    } else {
        Math.pow(
            ((c + 0.055) / 1.055).toDouble(),
            2.4,
        ).toFloat()
    }
    return 0.2126f * channel(red) + 0.7152f * channel(green) + 0.0722f * channel(blue)
}

/** Palette definitions. */
object MonaPalettes {
    val dark = MonaColors(
        ground = Color(0xFF0B0A10),
        surface = Color(0xFF18161F),
        surface2 = Color(0xFF24212D),
        surface3 = Color(0xFF2E2A3A),
        border = Color(0xFF3A3646),
        borderStrong = Color(0xFF4C475C),
        textPrimary = Color(0xFFEEEAF6),
        textSecondary = Color(0xFFA6A0B8),
        textTertiary = Color(0xFF726C82),
        accent = Color(0xFFC7F24E),
        accentOn = Color(0xFF14180A),
        accentMuted = Color(0x33C7F24E),
        violet = Color(0xFF311B6B),
        cyan = Color(0xFF37E0D8),
        pink = Color(0xFFFF5FA2),
        success = Color(0xFF7BE495),
        warning = Color(0xFFF2C14E),
        error = Color(0xFFFF8A80),
        canvasBackdrop = Color(0xFF07060A),
        selection = Color(0xFF37E0D8),
        isDark = true,
    )

    val amoled = dark.copy(
        ground = Color(0xFF000000),
        surface = Color(0xFF0C0B10),
        surface2 = Color(0xFF151319),
        surface3 = Color(0xFF1E1B24),
        border = Color(0xFF2A2733),
        canvasBackdrop = Color(0xFF000000),
    )

    val light = MonaColors(
        ground = Color(0xFFF5F3F9),
        surface = Color(0xFFFFFFFF),
        surface2 = Color(0xFFEFEBF6),
        surface3 = Color(0xFFE4DFEF),
        border = Color(0xFFD5CFE2),
        borderStrong = Color(0xFFB9B1CB),
        textPrimary = Color(0xFF17141F),
        textSecondary = Color(0xFF544E63),
        textTertiary = Color(0xFF807A92),
        accent = Color(0xFF7BAE12),
        accentOn = Color(0xFFFFFFFF),
        accentMuted = Color(0x227BAE12),
        violet = Color(0xFF311B6B),
        cyan = Color(0xFF0E9C96),
        pink = Color(0xFFD9367C),
        success = Color(0xFF2E9B57),
        warning = Color(0xFFB07A08),
        error = Color(0xFFC5392E),
        canvasBackdrop = Color(0xFFDAD5E4),
        selection = Color(0xFF0E9C96),
        isDark = false,
    )

    /** Artist palettes used for brush swatches and colour harmony. */
    val artistPalettes: Map<String, List<Color>> = mapOf(
        "Ink & Lime" to listOf(
            Color(0xFF0B0A10), Color(0xFFC7F24E), Color(0xFF37E0D8), Color(0xFFFF5FA2),
            Color(0xFFEEEAF6),
        ),
        "Sunset Gouache" to listOf(
            Color(0xFF3B1F2B), Color(0xFFDB6A55), Color(0xFFF2B33D), Color(0xFFF7E0C0),
            Color(0xFF6E8C7B),
        ),
        "Cold Atelier" to listOf(
            Color(0xFF101820), Color(0xFF3C6E8F), Color(0xFF88A9C3), Color(0xFFE7E3D4),
            Color(0xFFB04A3C),
        ),
        "Botanic" to listOf(
            Color(0xFF1D2A21), Color(0xFF4E7A4A), Color(0xFFA3B565), Color(0xFFF0EAD6),
            Color(
                0xFFC97B4A,
            ),
        ),
        "Manga Mono" to listOf(
            Color(0xFF000000), Color(0xFF3D3D3D), Color(0xFF808080), Color(0xFFC9C9C9),
            Color(0xFFFFFFFF),
        ),
        "Neon Lab" to listOf(
            Color(0xFF120C1F), Color(0xFF7B4BFF), Color(0xFF00E5FF), Color(0xFFFF3D8B),
            Color(0xFFFFFF9E),
        ),
    )

    /** User-selectable accent colours (in addition to a custom picker). */
    val accentChoices: List<Color> = listOf(
        dark.accent, dark.cyan, dark.pink, Color(0xFF9D7BFF), Color(0xFFFFB74D), Color(0xFF7BE495),
    )
}

val LocalMonaColors = staticCompositionLocalOf { MonaPalettes.dark }
val LocalMonaShapes = staticCompositionLocalOf { MonaShapes() }
val LocalMonaSpacing = staticCompositionLocalOf { MonaSpacing() }
