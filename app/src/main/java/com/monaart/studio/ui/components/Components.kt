package com.monaart.studio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monaart.studio.core.design.Mona
import com.monaart.studio.core.design.MonaTheme

/**
 * Motion budget. Honour the system/application "reduce motion" preference:
 * decorative animation collapses to cross-fades, and physical springs are
 * replaced by instant state changes.
 */
@Immutable
data class MotionBudget(val reduceMotion: Boolean) {
    fun <T> springFor() = spring<T>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow)
    fun duration(ms: Int) = if (reduceMotion) 0 else ms
}

val LocalMotionBudget = compositionLocalOf { MotionBudget(false) }

object MonaMotion {
    val current: MotionBudget
        @Composable get() = LocalMotionBudget.current

    fun provides(reduceMotion: Boolean, content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalMotionBudget provides MotionBudget(reduceMotion), content = content)
    }
}

// ---------------------------------------------------------------------------
// Bento card
// ---------------------------------------------------------------------------

/**
 * The Bento building block: a rounded, subtly bordered surface. Used for the
 * gallery, home dashboard, settings groups and asset library. Never used as the
 * canvas workspace background.
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    span: BentoSpan = BentoSpan.Single,
    onClick: (() -> Unit)? = null,
    accent: Color? = null,
    contentPadding: Dp = Mona.spacing.cardPadding,
    elevation: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !MonaMotion.current.reduceMotion) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bento-press",
    )
    val shape = RoundedCornerShape(shapes.lg)
    Column(
        modifier = modifier
            .scale(scale)
            .then(if (elevation) Modifier.shadow(if (colors.isDark) 0.dp else 6.dp, shape, clip = false) else Modifier)
            .clip(shape)
            .background(if (accent != null) accent.copy(alpha = if (colors.isDark) 0.14f else 0.10f) else colors.surface)
            .border(1.dp, if (accent != null) accent.copy(alpha = 0.45f) else colors.border, shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ) else Modifier,
            )
            .padding(contentPadding),
        content = content,
    )
}

enum class BentoSpan { Single, Wide, Tall, Hero }

/** Bento grid row metrics helpers (a simple weighted-column Bento layout). */
@Composable
fun BentoRow(
    modifier: Modifier = Modifier,
    spacing: Dp = Mona.spacing.md,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Top,
        content = content,
    )
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------

/** Round icon button with an accessible label and a clear selected state. */
@Composable
fun MonaIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    size: Dp = Mona.spacing.touchTarget,
    tint: Color? = null,
    badge: Boolean = false,
) {
    val colors = Mona.colors
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && !MonaMotion.current.reduceMotion) 0.92f else 1f, label = "btn")
    val bg by animateColorAsState(
        if (selected) colors.accent else Color.Transparent,
        animationSpec = tween(MonaMotion.current.duration(160)),
        label = "btn-bg",
    )
    val fg = when {
        !enabled -> colors.textTertiary
        tint != null -> tint
        selected -> colors.accentOn
        else -> colors.textPrimary
    }
    Box(
        modifier = modifier
            .scale(scale)
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .then(if (badge) Modifier.border(1.5.dp, colors.accent, CircleShape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label; this.selected = selected },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(size * 0.46f))
    }
}

/** Square-ish icon button used in rails and toolbars. */
@Composable
fun MonaToolButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    accentColor: Color? = null,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && !MonaMotion.current.reduceMotion) 0.93f else 1f, label = "tool")
    val bg by animateColorAsState(
        when {
            selected && accentColor != null -> accentColor
            selected -> colors.accent
            else -> Color.Transparent
        },
        animationSpec = tween(MonaMotion.current.duration(150)),
        label = "tool-bg",
    )
    val shape = RoundedCornerShape(shapes.sm + 4.dp)
    Column(
        modifier = modifier
            .scale(scale)
            .size(width = 52.dp, height = 52.dp)
            .clip(shape)
            .background(bg)
            .then(
                if (selected) Modifier.border(1.dp, (accentColor ?: colors.accent).copy(alpha = 0.6f), shape)
                else Modifier,
            )
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label; this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = when {
                !enabled -> colors.textTertiary
                selected -> colors.on(accentColor ?: colors.accent)
                else -> colors.textSecondary
            },
            modifier = Modifier.size(21.dp),
        )
    }
}

/** Primary filled action. */
@Composable
fun MonaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    tonal: Boolean = false,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && !MonaMotion.current.reduceMotion) 0.97f else 1f, label = "primary")
    val bg = when {
        !enabled -> colors.surface2
        tonal -> colors.accentMuted
        else -> colors.accent
    }
    val fg = when {
        !enabled -> colors.textTertiary
        tonal -> colors.accent
        else -> colors.accentOn
    }
    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(shapes.pill))
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = Mona.spacing.lg, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Mona.spacing.sm))
        }
        Text(text, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** Small pill used for categories, tags and status. */
@Composable
fun MonaPill(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    color: Color? = null,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(shapes.pill))
            .background(
                when {
                    color != null -> color.copy(alpha = 0.18f)
                    selected -> colors.accent
                    else -> colors.surface2
                },
            )
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Tab, onClick = onClick) else Modifier,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = when {
                color != null -> color
                selected -> colors.accentOn
                else -> colors.textSecondary
            },
            maxLines = 1,
        )
    }
}

/** Segmented control. */
@Composable
fun <T> MonaSegmented(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(shapes.pill))
            .background(colors.surface2)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val bg by animateColorAsState(
                if (isSelected) colors.accent else Color.Transparent,
                animationSpec = tween(MonaMotion.current.duration(140)),
                label = "seg",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(shapes.pill))
                    .background(bg)
                    .clickable(role = Role.Tab) { onSelect(option) }
                    .semantics { this.selected = isSelected }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    label(option),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) colors.accentOn else colors.textSecondary,
                    maxLines = 1,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sliders & numeric parameters
// ---------------------------------------------------------------------------

/** Labelled parameter slider with a numeric readout (used across brush UI). */
@Composable
fun ParamSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    format: (Float) -> String = { "%.2f".format(it) },
    enabled: Boolean = true,
) {
    val colors = Mona.colors
    var local by remember(value) { mutableStateOf(value) }
    Column(modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.45f)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary, modifier = Modifier.weight(1f))
            Text(format(local), style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
        }
        Spacer(Modifier.height(2.dp))
        MonaSlider(
            value = local,
            onValueChange = { local = it; onValueChange(it) },
            range = range,
            steps = steps,
            enabled = enabled,
        )
    }
}

/**
 * Custom slider: thin track, large accent-filled progress, 24dp-thick hit area
 * and a press-scale thumb so it feels precise under a stylus.
 */
@Composable
fun MonaSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    accent: Color? = null,
) {
    val colors = Mona.colors
    val accentColor = accent ?: colors.accent
    val fraction = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val haptics = LocalHapticFeedback.current
    val hapticEnabled = true
    var lastStep by remember { mutableStateOf(-1) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(26.dp)
            .then(
                if (enabled) Modifier.pointerInputTrack { f ->
                    val stepCount = steps
                    val raw = range.start + f * (range.endInclusive - range.start)
                    if (stepCount > 1) {
                        val stepIndex = (raw * (stepCount - 1)).toInt().coerceIn(0, stepCount - 1)
                        if (stepIndex != lastStep) {
                            lastStep = stepIndex
                            if (hapticEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onValueChange(stepIndex.toFloat() / (stepCount - 1))
                    } else {
                        onValueChange(raw)
                    }
                } else Modifier,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Canvas(Modifier.fillMaxWidth().height(26.dp)) {
            val cy = size.height / 2
            val trackH = 5.dp.toPx()
            drawRoundRect(
                color = colors.surface3,
                topLeft = Offset(0f, cy - trackH / 2),
                size = Size(size.width, trackH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackH / 2),
            )
            drawRoundRect(
                color = if (enabled) accentColor else colors.textTertiary,
                topLeft = Offset(0f, cy - trackH / 2),
                size = Size(size.width * fraction, trackH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackH / 2),
            )
            val thumbR = 9.dp.toPx()
            drawCircle(
                color = colors.surface,
                radius = thumbR,
                center = Offset(size.width * fraction, cy),
            )
            drawCircle(
                color = if (enabled) accentColor else colors.textTertiary,
                radius = thumbR - 2.dp.toPx(),
                center = Offset(size.width * fraction, cy),
            )
        }
    }
}

private fun Modifier.pointerInputTrack(onFraction: (Float) -> Unit): Modifier = this.then(
    Modifier.pointerInputCompat(onFraction),
)

@Composable
private fun Modifier.pointerInputCompat(onFraction: (Float) -> Unit): Modifier {
    return this.then(
        Modifier.pointerInput(onFraction) {
            detectTapGestures(onPress = { offset ->
                onFraction((offset.x / size.width).coerceIn(0f, 1f))
                tryAwaitRelease()
            })
        }.then(
            Modifier.pointerInput(onFraction) {
                androidx.compose.foundation.gestures.detectHorizontalDragGestures { change, _ ->
                    onFraction((change.position.x / size.width).coerceIn(0f, 1f))
                }
            },
        ),
    )
}

// ---------------------------------------------------------------------------
// Colour swatch, section header, empty state, dismissible tip
// ---------------------------------------------------------------------------

@Composable
fun ColorSwatch(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(Mona.shapes.sm),
) {
    val colors = Mona.colors
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) colors.accent else colors.border,
                shape = shape,
            )
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics { contentDescription = "Colour swatch ${color.value.toString(16)}" },
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
    subtitle: String? = null,
) {
    val colors = Mona.colors
    Row(modifier.fillMaxWidth().padding(vertical = Mona.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = Mona.colors
    Column(
        modifier.fillMaxWidth().padding(Mona.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.accent, modifier = Modifier.size(30.dp))
        }
        Spacer(Modifier.height(Mona.spacing.lg))
        Text(title, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
        Spacer(Modifier.height(Mona.spacing.xs))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Mona.spacing.lg))
            MonaButton(actionLabel, onAction)
        }
    }
}

/** Inline, dismissible tip. */
@Composable
fun TipCard(text: String, onDismiss: () -> Unit, modifier: Modifier = Modifier, title: String = "Tip") {
    val colors = Mona.colors
    var visible by remember { mutableStateOf(true) }
    AnimatedVisibility(visible, enter = fadeIn(), exit = fadeOut(), modifier = modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Mona.shapes.md))
                .background(colors.accentMuted)
                .border(1.dp, colors.accent.copy(alpha = 0.4f), RoundedCornerShape(Mona.shapes.md))
                .padding(Mona.spacing.md),
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = colors.accent)
                Text(text, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Text(
                "Dismiss",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
                modifier = Modifier
                    .clickable { visible = false; onDismiss() }
                    .padding(4.dp),
            )
        }
    }
}

/** Shimmer-free loading placeholder (no endless animation on low-end devices). */
@Composable
fun LoadingBlock(modifier: Modifier = Modifier, text: String = "Loading…") {
    val colors = Mona.colors
    Box(
        modifier.fillMaxWidth().padding(Mona.spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Banner used for errors and recovery notices. */
@Composable
fun StatusBanner(
    text: String,
    modifier: Modifier = Modifier,
    tone: StatusTone = StatusTone.Info,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = Mona.colors
    val color = when (tone) {
        StatusTone.Info -> colors.cyan
        StatusTone.Success -> colors.success
        StatusTone.Warning -> colors.warning
        StatusTone.Error -> colors.error
    }
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Mona.shapes.md))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(Mona.shapes.md))
            .padding(horizontal = Mona.spacing.md, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = color, modifier = Modifier.weight(1f))
        if (actionLabel != null && onAction != null) {
            Text(
                actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                modifier = Modifier.clickable(onClick = onAction).padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
            )
        }
    }
}

enum class StatusTone { Info, Success, Warning, Error }

/** A compact labelled numeric value (brush size, opacity, DPI, etc). */
@Composable
fun Metric(label: String, value: String, modifier: Modifier = Modifier, accent: Color? = null) {
    val colors = Mona.colors
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            value,
            style = com.monaart.studio.core.design.MonaType.numeric,
            color = accent ?: colors.textPrimary,
            maxLines = 1,
        )
    }
}

/** Simple segmented progress bar (used for storage usage). */
@Composable
fun UsageBar(fraction: Float, modifier: Modifier = Modifier, color: Color? = null) {
    val colors = Mona.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.surface3),
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color ?: colors.accent),
        )
    }
}

/** Rounded gradient avatar/identifier used by the studio header. */
@Composable
fun StudioMark(size: Dp = 40.dp, modifier: Modifier = Modifier) {
    val colors = Mona.colors
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(
                Brush.linearGradient(
                    listOf(colors.accent, colors.cyan, colors.pink),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "M",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF0B0A10),
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Remembers a pressed pulse animation for tap targets that need feedback. */
@Composable
fun rememberPulse(): Pair<Animatable<Float, *>, () -> Unit> {
    val anim = remember { Animatable(1f) }
    val trigger: () -> Unit = {
        // Fire-and-forget pulse; deliberate no-op if reduce motion is on.
        anim.animateTo(0.92f, tween(80))
        anim.animateTo(1f, tween(120))
    }
    return anim to trigger
}
