package com.monaart.studio.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monaart.engine.brush.BrushKind
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.BrushSpec
import com.monaart.engine.brush.PressureTarget
import com.monaart.engine.brush.TaperMode
import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.packStraight
import com.monaart.engine.paint.redOf
import com.monaart.studio.core.design.Mona
import com.monaart.studio.core.design.MonaPalettes
import com.monaart.studio.core.design.MonaType
import com.monaart.studio.domain.tool.ToolId
import com.monaart.studio.presentation.common.formatBytesShort
import com.monaart.studio.ui.components.ColorSwatch
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.MonaSegmented
import com.monaart.studio.ui.components.MonaSlider
import com.monaart.studio.ui.components.ParamSlider
import com.monaart.studio.ui.components.SectionHeader

// ---------------------------------------------------------------------------
// Inspector (tablet right panel)
// ---------------------------------------------------------------------------

@Composable
fun InspectorPanel(editor: EditorViewModel, state: EditorUiState, modifier: Modifier = Modifier) {
    val colors = Mona.colors
    var tab by remember { mutableStateOf(0) }
    Column(
        modifier
            .background(colors.surface)
            .padding(horizontal = 12.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        MonaSegmented(
            options = listOf(0, 1, 2),
            selected = tab,
            onSelect = { tab = it },
            label = { listOf("Brush", "Colour", "Layers")[it] },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (tab) {
                0 -> BrushControls(editor, state)
                1 -> ColorControls(editor, state)
                else -> LayerList(editor, state, compact = true)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Brush controls
// ---------------------------------------------------------------------------

@Composable
fun BrushControls(
    editor: EditorViewModel,
    state: EditorUiState,
    showPresets: Boolean = true,
    /** Replaces the default "write into the open document" brush edit (brush studio). */
    onBrushChange: ((BrushSpec) -> Unit)? = null,
    /** Presets offered in the header row; defaults to the app's preset library. */
    presets: List<BrushSpec>? = null,
) {
    val colors = Mona.colors
    val brush = state.brush
    val edit: ((BrushSpec) -> BrushSpec) -> Unit = { transform ->
        if (onBrushChange != null) onBrushChange(transform(brush)) else editor.updateBrush(transform)
    }
    if (showPresets) {
        SectionHeader("Preset", subtitle = "${BrushPresets.all.size} built-in brushes")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(presets ?: editor.appBrushPresets()) { preset ->
                BrushChip(preset, preset.id == brush.id) {
                    if (onBrushChange != null) onBrushChange(preset) else editor.selectBrush(preset)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    SectionHeader("Shape & flow")
    ParamSlider("Size", brush.size, { v -> edit { it.copy(size = v) } }, range = 1f..800f,
        format = { "%.0f px".format(it) })
    ParamSlider("Opacity", brush.opacity, { v -> edit { it.copy(opacity = v) } },
        format = { "${(it * 100).toInt()}%" })
    ParamSlider("Flow", brush.flow, { v -> edit { it.copy(flow = v) } },
        format = { "${(it * 100).toInt()}%" })
    ParamSlider("Spacing", brush.spacing, { v -> edit { it.copy(spacing = v) } },
        range = 0.01f..1f, format = { "%.2f×".format(it) })
    ParamSlider("Hardness", brush.hardness, { v -> edit { it.copy(hardness = v) } },
        format = { "${(it * 100).toInt()}%" })
    ParamSlider("Roundness", brush.roundness, { v -> edit { it.copy(roundness = v) } },
        range = 0.03f..1f, format = { "%.2f".format(it) })
    ParamSlider("Angle", brush.angle, { v -> edit { it.copy(angle = v) } },
        range = -90f..90f, format = { "%.0f°".format(it) })

    Spacer(Modifier.height(10.dp))
    SectionHeader("Pressure")
    ParamSlider("Size response", brush.pressureSize, { v -> edit { it.copy(pressureSize = v) } },
        format = { "${(it * 100).toInt()}%" })
    ParamSlider("Opacity response", brush.pressureOpacity, { v -> edit { it.copy(pressureOpacity = v) } },
        format = { "${(it * 100).toInt()}%" })
    ParamSlider("Curve gamma", brush.curve.gamma, { v ->
        edit { it.copy(curve = it.curve.copy(gamma = v)) }
    }, range = 0.3f..3f, format = { "%.2f".format(it) })
    MonaSegmented(
        options = PressureTarget.entries,
        selected = brush.pressureTarget,
        onSelect = { target -> edit { it.copy(pressureTarget = target) } },
        label = { it.name.lowercase().replace('_', ' ') },
        modifier = Modifier.fillMaxWidth(),
    )

    Spacer(Modifier.height(10.dp))
    SectionHeader("Dynamic")
    ParamSlider("Stabilisation", brush.stabilization, { v -> edit { it.copy(stabilization = v) } },
        format = { "${(it * 100).toInt()}%" })
    ParamSlider("Scatter", brush.scatter, { v -> edit { it.copy(scatter = v) } },
        range = 0f..1.5f, format = { "%.2f".format(it) })
    ParamSlider("Size jitter", brush.sizeJitter, { v -> edit { it.copy(sizeJitter = v) } },
        format = { "${(it * 100).toInt()}%" })
    ParamSlider("Hue jitter", brush.hueJitter, { v -> edit { it.copy(hueJitter = v) } },
        range = 0f..90f, format = { "%.0f°".format(it) })
    MonaSegmented(
        options = TaperMode.entries,
        selected = brush.taperMode,
        onSelect = { mode -> edit { it.copy(taperMode = mode) } },
        label = { it.name.lowercase() },
        modifier = Modifier.fillMaxWidth(),
    )

    if (state.tool == ToolId.Liquify || state.tool == ToolId.Transform) {
        Spacer(Modifier.height(10.dp))
        SectionHeader(
            if (state.tool == ToolId.Liquify) "Liquify tool" else "Transform tool",
            subtitle = if (state.tool == ToolId.Liquify) {
                "Drag on the canvas to push pixels; one undo step per gesture"
            } else {
                "Drag on the canvas to scale and move the active layer"
            },
        )
        if (state.tool == ToolId.Liquify) {
            ParamSlider("Brush radius", state.liquifyRadius, { editor.setLiquifyRadius(it) },
                range = 8f..600f, format = { "%.0f px".format(it) })
            ParamSlider("Push strength", state.liquifyStrength, { editor.setLiquifyStrength(it) },
                range = 0.05f..1.5f, format = { "%.2f×".format(it) })
        } else {
            ParamSlider("Scale", state.transformScalePercent, { editor.setTransformScale(it) },
                range = 10f..400f, format = { "%.0f%%".format(it) })
            ParamSlider("Rotate", state.transformRotationDeg, { editor.setTransformRotation(it) },
                range = -180f..180f, format = { "%.0f°".format(it) })
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MonaPill("Apply", onClick = { editor.commitTransform() })
                MonaPill("Reset", onClick = { editor.resetTransform() })
            }
        }
    }

    Spacer(Modifier.height(10.dp))
    SectionHeader("Grain", subtitle = "Procedural textures rendered per dab")
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MonaPill("None", selected = brush.texture == null, onClick = { edit { it.copy(texture = null) } })
        com.monaart.engine.brush.Patterns.GRAIN_NAMES.forEach { name ->
            MonaPill(name.removePrefix("grain."), selected = brush.texture == name, onClick = {
                edit { it.copy(texture = name) }
            })
        }
    }
    if (brush.texture != null) {
        ParamSlider("Grain strength", brush.grainStrength, { v -> edit { it.copy(grainStrength = v) } },
            format = { "${(it * 100).toInt()}%" })
        ParamSlider("Grain scale", brush.grainScale, { v -> edit { it.copy(grainScale = v) } },
            range = 0.1f..4f, format = { "%.2f×".format(it) })
    }
    if (brush.kind == BrushKind.OIL || brush.kind == BrushKind.WATERCOLOR) {
        ParamSlider("Wetness", brush.wetness, { v -> edit { it.copy(wetness = v) } },
            format = { "${(it * 100).toInt()}%" })
    }
    if (brush.kind == BrushKind.SMUDGE) {
        ParamSlider("Smudge rate", brush.smudgeRate, { v -> edit { it.copy(smudgeRate = v) } },
            format = { "${(it * 100).toInt()}%" })
    }
    if (brush.kind == BrushKind.PARTICLE || brush.kind == BrushKind.STAMP) {
        ParamSlider("Particles", brush.particleCount.toFloat(), { v -> edit { it.copy(particleCount = v.toInt()) } },
            range = 1f..48f, steps = 48, format = { "${it.toInt()}" })
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MonaButton(
            "Save preset",
            {
                val toSave = brush
                if (onBrushChange != null) editor.saveBrushAsPreset(toSave) else editor.saveBrushAsPreset(toSave)
            },
            tonal = true,
        )
    }
}

@Composable
private fun BrushChip(preset: BrushSpec, selected: Boolean, onClick: () -> Unit) {
    val colors = Mona.colors
    Column(
        Modifier
            .width(76.dp)
            .clip(RoundedCornerShape(Mona.shapes.sm))
            .background(if (selected) colors.accentMuted else colors.surface2)
            .border(1.dp, if (selected) colors.accent else colors.border, RoundedCornerShape(Mona.shapes.sm))
            .clickable(onClick = onClick)
            .padding(6.dp),
    ) {
        Text(
            preset.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.accent else colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.heightIn(min = 26.dp),
        )
        Text(
            "${preset.size.toInt()}px · ${(preset.opacity * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Colour controls
// ---------------------------------------------------------------------------

@Composable
fun ColorControls(editor: EditorViewModel, state: EditorUiState) {
    val colors = Mona.colors
    var hsv by remember(state.color) { mutableStateOf(toHsv(state.color)) }
    SectionHeader("Colour", subtitle = "HSB picker · hex · palettes")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(Mona.shapes.sm))
                .background(Color(state.color))
                .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.sm)),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            HexField(state.color) { argb -> editor.setColor(argb); hsv = toHsv(argb) }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MonaPill("Swap", onClick = { editor.swapColors(); hsv = toHsv(editor.state.value.color) })
                MonaPill("Black", onClick = { editor.setColor(0xFF000000.toInt()); hsv = toHsv(0xFF000000.toInt()) })
                MonaPill("White", onClick = { editor.setColor(0xFFFFFFFF.toInt()); hsv = toHsv(0xFFFFFFFF.toInt()) })
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    SaturationValuePicker(
        hue = hsv.first, sat = hsv.second, value = hsv.third,
        modifier = Modifier.fillMaxWidth().height(170.dp),
    ) { s, v ->
        hsv = Triple(hsv.first, s, v)
        editor.setColor(fromHsv(255, hsv.first, s, v))
    }
    Spacer(Modifier.height(8.dp))
    HueStrip(hsv.first, Modifier.fillMaxWidth().height(24.dp)) { hue ->
        hsv = Triple(hue, hsv.second, hsv.third)
        editor.setColor(fromHsv(255, hue, hsv.second, hsv.third))
    }
    Spacer(Modifier.height(8.dp))
    ParamSlider("Alpha", 1f, { }, format = { "${(it * 100).toInt()}%" }, enabled = false)

    Spacer(Modifier.height(10.dp))
    SectionHeader("Artist palettes")
    MonaPalettes.artistPalettes.forEach { (name, swatches) ->
                Text(name, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary, modifier =
            Modifier.padding(top = 6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
            swatches.forEach { c ->
                ColorSwatch(c, size = 28.dp, onClick = {
                    editor.setColor((c.value shr 32).toInt())
                })
            }
        }
    }
}

@Composable
private fun SaturationValuePicker(
    hue: Float,
    sat: Float,
    value: Float,
    modifier: Modifier = Modifier,
    onChange: (Float, Float) -> Unit,
) {
    val colors = Mona.colors
    Canvas(
        modifier
            .clip(RoundedCornerShape(Mona.shapes.md))
            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.md))
            .pointerInput(hue) {
                detectTapGestures { off ->
                    onChange((off.x / size.width).coerceIn(0f, 1f), 1f - (off.y / size.height).coerceIn(0f, 1f))
                }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    onChange(
                        (change.position.x / size.width).coerceIn(0f, 1f),
                        1f - (change.position.y / size.height).coerceIn(0f, 1f),
                    )
                }
            },
    ) {
        val hueColor = Color(fromHsv(255, hue, 1f, 1f))
        drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        val cx = sat * size.width
        val cy = (1f - value) * size.height
                drawCircle(Color.White, radius = 7.dp.toPx(), center = Offset(cx, cy), style =
            androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
    }
}

@Composable
private fun HueStrip(hue: Float, modifier: Modifier = Modifier, onChange: (Float) -> Unit) {
    val colors = Mona.colors
    Canvas(
        modifier
            .clip(RoundedCornerShape(999.dp))
            .border(1.dp, colors.border, RoundedCornerShape(999.dp))
            .pointerInput(Unit) {
                detectTapGestures { off -> onChange((off.x / size.width).coerceIn(0f, 0.999f) * 360f) }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    onChange((change.position.x / size.width).coerceIn(0f, 0.999f) * 360f)
                }
            },
    ) {
        val stops = (0..6).map { i -> Color(fromHsv(255, i * 60f, 1f, 1f)) }
        drawRect(Brush.horizontalGradient(stops))
        val x = (hue / 360f) * size.width
                drawCircle(Color.White, radius = size.height / 2.4f, center = Offset(x, size.height / 2), style =
            androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
    }
}

@Composable
private fun HexField(argb: Int, onChange: (Int) -> Unit) {
    val colors = Mona.colors
    val focus = LocalFocusManager.current
    var text by remember(argb) { mutableStateOf("#%06X".format(argb and 0x00FFFFFF)) }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Mona.shapes.sm))
            .background(colors.surface2)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = text,
            onValueChange = { input ->
                text = input.take(7).uppercase()
                val hex = text.removePrefix("#")
                if (hex.length == 6) {
                    hex.toIntOrNull(16)?.let { onChange((0xFF shl 24) or it) }
                }
            },
            singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary, fontFamily =
                MonaType.numeric.fontFamily),
            modifier = Modifier.weight(1f),
        )
        Text("HEX", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary,
            modifier = Modifier.clickable { focus.clearFocus() })
    }
}

private fun toHsv(argb: Int): Triple<Float, Float, Float> {
    val r = redOf(argb) / 255f
    val g = greenOf(argb) / 255f
    val b = blueOf(argb) / 255f
    val mx = maxOf(r, g, b)
    val mn = minOf(r, g, b)
    val d = mx - mn
    var h = 0f
    if (d > 1e-5f) {
        h = when (mx) {
            r -> 60f * (((g - b) / d) % 6f)
            g -> 60f * (((b - r) / d) + 2f)
            else -> 60f * (((r - g) / d) + 4f)
        }
        if (h < 0f) h += 360f
    }
    return Triple(h, if (mx <= 0f) 0f else d / mx, mx)
}

private fun fromHsv(a: Int, h: Float, s: Float, v: Float): Int {
    val c = v * s
    val hp = (h % 360f) / 60f
    val x = c * (1f - kotlin.math.abs(hp % 2f - 1f))
    val (r1, g1, b1) = when (hp.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = v - c
    return packStraight(r1 + m, g1 + m, b1 + m, a / 255f)
}

// ---------------------------------------------------------------------------
// Layer list
// ---------------------------------------------------------------------------

@Composable
fun LayerList(editor: EditorViewModel, state: EditorUiState, compact: Boolean) {
    val colors = Mona.colors
    var renaming by remember { mutableStateOf<Long?>(null) }
    SectionHeader("Layers", subtitle = "${state.layers.size} layers · ${formatBytesShort(state.memoryBytes)} tiles")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        MonaIconButton(Icons.Outlined.Add, "Add layer", { editor.addLayer() }, tint = colors.accent)
                MonaIconButton(Icons.Outlined.ContentCopy, "Duplicate layer", { editor.duplicateLayer() }, tint =
            colors.textSecondary)
        MonaIconButton(Icons.Outlined.Merge, "Merge down", { editor.mergeDown() }, tint = colors.textSecondary)
                MonaIconButton(Icons.Outlined.ArrowUpward, "Move up", { editor.moveLayer(state.activeLayerId, 1) },
            tint = colors.textSecondary)
                MonaIconButton(Icons.Outlined.ArrowDownward, "Move down", { editor.moveLayer(state.activeLayerId,
            -1) }, tint = colors.textSecondary)
        MonaIconButton(Icons.Outlined.Delete, "Delete layer", { editor.deleteLayer() }, tint = colors.error)
    }
    // Top-most layer first, matching how artists read a layer stack.
    val ordered = state.layers.asReversed()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ordered.forEach { layer ->
            val active = layer.id == state.activeLayerId
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Mona.shapes.sm))
                    .background(if (active) colors.accentMuted else colors.surface2)
                    .border(1.dp, if (active) colors.accent else colors.border, RoundedCornerShape(Mona.shapes.sm))
                    .clickable { editor.selectLayer(layer.id) }
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (layer.visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (layer.visible) "Hide layer" else "Show layer",
                        tint = if (layer.visible) colors.textSecondary else colors.textTertiary,
                        modifier = Modifier.size(18.dp).clickable { editor.toggleLayerVisibility(layer.id) },
                    )
                    Spacer(Modifier.width(8.dp))
                    if (renaming == layer.id) {
                        BasicTextField(
                            value = layer.name,
                            onValueChange = { editor.renameLayer(layer.id, it) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Text(
                            layer.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).clickable { renaming = layer.id },
                        )
                    }
                    Icon(
                        if (layer.locked) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                        contentDescription = if (layer.locked) "Unlock layer" else "Lock layer",
                        tint = if (layer.locked) colors.warning else colors.textTertiary,
                        modifier = Modifier.size(16.dp).clickable { editor.toggleLayerLock(layer.id) },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${(layer.opacity * 100).toInt()}%",
                        style = MonaType.numeric,
                        color = colors.textTertiary,
                        modifier = Modifier.clickable { renaming = null },
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        layer.blend.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.clickable {
                            val modes = BlendMode.entries
                            val next = modes[(modes.indexOf(layer.blend) + 1) % modes.size]
                            editor.setLayerBlend(layer.id, next)
                        },
                    )
                    Spacer(Modifier.width(10.dp))
                    if (layer.clipping) {
                        MonaPill("clip", color = colors.cyan)
                        Spacer(Modifier.width(6.dp))
                    }
                    if (layer.alphaLocked) {
                        MonaPill("alpha", color = colors.pink)
                        Spacer(Modifier.width(6.dp))
                    }
                    if (layer.isGroup) MonaPill("group")
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (layer.bytes > 0) formatBytesShort(layer.bytes) else "empty",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
                if (active) {
                    var opacityBefore by remember(layer.id) { mutableStateOf(layer.opacity) }
                    MonaSlider(
                        value = layer.opacity,
                        onValueChange = { editor.setLayerOpacity(layer.id, it) },
                        onValueChangeFinished = {
                            editor.commitLayerOpacity(layer.id, opacityBefore, layer.opacity)
                            opacityBefore = layer.opacity
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MonaPill(
                            text = if (layer.alphaLocked) "Alpha lock on" else "Alpha lock",
                            selected = layer.alphaLocked,
                            onClick = { editor.toggleAlphaLock(layer.id) },
                        )
                        MonaPill(
                            text = if (layer.clipping) "Clipping on" else "Clipping",
                            selected = layer.clipping,
                            onClick = { editor.toggleClipping(layer.id) },
                        )
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MonaPill("Flatten image", onClick = { editor.flattenImage() })
        MonaPill("Clear layer", onClick = { editor.clearActiveLayer() })
    }
}

// ---------------------------------------------------------------------------
// Quick brush bar (bottom strip)
// ---------------------------------------------------------------------------

@Composable
fun QuickBrushBar(editor: EditorViewModel, state: EditorUiState) {
    val colors = Mona.colors
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(state.color))
                .border(2.dp, colors.surface3, CircleShape)
                .clickable { editor.toggleColorPanel() },
        )
        Column(Modifier.weight(1.4f)) {
            Text("Size", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            MonaSlider(
                value = state.brush.size,
                onValueChange = { v -> editor.updateBrush { it.copy(size = v) } },
                range = 1f..400f,
            )
        }
        Column(Modifier.weight(1f)) {
            Text("Opacity", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
            MonaSlider(
                value = state.brush.opacity,
                onValueChange = { v -> editor.updateBrush { it.copy(opacity = v) } },
            )
        }
        Text(
            "${state.brush.size.toInt()} px",
            style = MonaType.numeric,
            color = colors.textSecondary,
        )
                MonaIconButton(Icons.Outlined.Tune, "Brush settings", { editor.toggleBrushPanel() }, tint =
            colors.textSecondary)
        MonaIconButton(Icons.Outlined.Layers, "Layers", { editor.toggleLayersPanel() }, tint = colors.textSecondary)
        MonaIconButton(Icons.Outlined.Brush, "Tool", { editor.selectTool(ToolId.Brush) },
            selected = state.tool == ToolId.Brush)
    }
}
