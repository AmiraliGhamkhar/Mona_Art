package com.monaart.studio.core.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monaart.studio.R
import com.monaart.studio.domain.model.ThemeMode
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.ui.components.MonaMotion

/**
 * Typography. Inter is bundled (SIL OFL 1.1) because a consistent type ramp is
 * part of the identity; system fonts are the fallback when the resource cannot
 * be loaded.
 */
object MonaType {
    private val inter: FontFamily? by lazy {
        runCatching {
            FontFamily(
                Font(R.font.inter_regular, FontWeight.Normal),
                Font(R.font.inter_medium, FontWeight.Medium),
                Font(R.font.inter_semibold, FontWeight.SemiBold),
                Font(R.font.inter_bold, FontWeight.Bold),
            )
        }.getOrNull()
    }

    val family: FontFamily get() = inter ?: FontFamily.SansSerif

    fun typography(fontScale: Float = 1f): Typography {
        fun s(size: Float, weight: FontWeight, lineHeight: Float, tracking: Float = 0f): TextStyle = TextStyle(
            fontFamily = family,
            fontWeight = weight,
            fontSize = (size * fontScale).sp,
            lineHeight = (lineHeight * fontScale).sp,
            letterSpacing = tracking.sp,
        )
        return Typography(
            // Screen titles (gallery header, settings header)
            displaySmall = s(38f, FontWeight.Bold, 44f, -0.6f),
            headlineLarge = s(30f, FontWeight.Bold, 36f, -0.4f),
            headlineMedium = s(25f, FontWeight.SemiBold, 31f, -0.3f),
            // Page headings
            titleLarge = s(21f, FontWeight.SemiBold, 27f, -0.2f),
            // Section headings
            titleMedium = s(16f, FontWeight.SemiBold, 22f),
            titleSmall = s(14f, FontWeight.Medium, 19f),
            // Body
            bodyLarge = s(15f, FontWeight.Normal, 22f),
            bodyMedium = s(13.5f, FontWeight.Normal, 19f),
            bodySmall = s(12f, FontWeight.Normal, 16f),
            // Labels and tool names
            labelLarge = s(13f, FontWeight.Medium, 17f, 0.1f),
            labelMedium = s(11.5f, FontWeight.Medium, 15f, 0.2f),
            labelSmall = s(10.5f, FontWeight.Medium, 13f, 0.3f),
        )
    }

    /** Numeric parameters (brush size, opacity) use tabular-ish tight styling. */
    val numeric = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = (-0.2).sp,
    )
}

@Composable
fun MonaTheme(
    settings: UiSettings,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (settings.themeMode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.Amoled -> true
        ThemeMode.System -> systemDark
    }
    val base = when {
        !dark -> MonaPalettes.light
        settings.themeMode == ThemeMode.Amoled -> MonaPalettes.amoled
        else -> MonaPalettes.dark
    }
    val colors = remember(base, settings.accentArgb) {
        if (settings.accentArgb == 0) {
            base
        } else {
            base.copy(
                accent = Color(settings.accentArgb),
                accentMuted = Color(settings.accentArgb).copy(alpha = 0.20f),
                accentOn = if (Color(settings.accentArgb).luminanceOf() > 0.5f) Color(0xFF14120A) else Color.White,
            )
        }
    }
    val shapes = remember(settings.cornerStyle) { MonaShapes().scaled(settings.cornerStyle.factor) }
    val spacing = remember(settings.density) { MonaSpacing().dense(settings.density.factor) }
    val typography = remember(settings.fontScale) { MonaType.typography(settings.fontScale) }

    val scheme = remember(colors) {
        val s = if (colors.isDark) darkColorScheme() else lightColorScheme()
        s.copy(
            primary = colors.accent,
            onPrimary = colors.accentOn,
            primaryContainer = colors.accentMuted,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.cyan,
            onSecondary = colors.on(colors.cyan),
            tertiary = colors.pink,
            background = colors.ground,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surface2,
            onSurfaceVariant = colors.textSecondary,
            surfaceContainer = colors.surface,
            surfaceContainerHigh = colors.surface2,
            surfaceContainerHighest = colors.surface3,
            surfaceContainerLow = colors.surface,
            surfaceContainerLowest = colors.ground,
            outline = colors.border,
            outlineVariant = colors.borderStrong,
            error = colors.error,
            onError = colors.on(colors.error),
            scrim = Color.Black,
        )
    }

    CompositionLocalProvider(
        LocalMonaColors provides colors,
        LocalMonaShapes provides shapes,
        LocalMonaSpacing provides spacing,
    ) {
        MonaMotion.provides(settings.reduceMotion) {
            MaterialTheme(
                colorScheme = scheme,
                typography = typography,
                shapes = MaterialTheme.shapes.copy(
                    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(shapes.xs),
                    small = androidx.compose.foundation.shape.RoundedCornerShape(shapes.sm),
                    medium = androidx.compose.foundation.shape.RoundedCornerShape(shapes.md),
                    large = androidx.compose.foundation.shape.RoundedCornerShape(shapes.lg),
                    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(shapes.xl),
                ),
                content = content,
            )
        }
    }
}

private fun Color.luminanceOf(): Float {
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

/** Convenience accessors used across screens. */
object Mona {
    val colors: MonaColors
        @Composable get() = LocalMonaColors.current
    val shapes: MonaShapes
        @Composable get() = LocalMonaShapes.current
    val spacing: MonaSpacing
        @Composable get() = LocalMonaSpacing.current
}

val HairlineWidth = 1.dp
val isAtLeastS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
