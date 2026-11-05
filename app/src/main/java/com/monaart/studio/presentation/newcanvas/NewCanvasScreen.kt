package com.monaart.studio.presentation.newcanvas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.monaart.studio.core.design.Mona
import com.monaart.studio.core.design.MonaType
import com.monaart.studio.domain.model.CanvasPreset
import com.monaart.studio.domain.model.CanvasSpec
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.presentation.common.formatBytesShort
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.MonaSegmented
import com.monaart.studio.ui.components.SectionHeader
import com.monaart.studio.ui.components.StatusBanner
import com.monaart.studio.ui.components.StatusTone
import kotlinx.coroutines.launch

/**
 * New canvas: presets, custom size, DPI, orientation, background and the
 * memory/size warnings an artist needs before committing to a huge canvas.
 */
@Composable
fun NewCanvasScreen(
    viewModel: MonaViewModel,
    settings: UiSettings,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit,
) {
    val colors = Mona.colors
    val spacing = Mona.spacing
    val scope = rememberCoroutineScope()
    val defaultPreset = CanvasSpec.presets.firstOrNull { it.id == settings.lastCanvasPreset } ?: CanvasSpec.presets.first()
    var spec by remember { mutableStateOf(CanvasSpec.fromPreset(defaultPreset, name = "Untitled")) }
    var creating by remember { mutableStateOf(false) }
    var customPresets by remember { mutableStateOf<List<CanvasPreset>>(emptyList()) }
    var widthText by remember(spec.width) { mutableStateOf(spec.width.toString()) }
    var heightText by remember(spec.height) { mutableStateOf(spec.height.toString()) }
    var dpiText by remember(spec.dpi) { mutableStateOf(spec.dpi.toString()) }

    val warnings = CanvasSpec.warnings(spec)
    val estimatedBytes = spec.estimatedLayerBytes

    Column(Modifier.fillMaxSize().background(colors.ground)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = spacing.gutter, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonaIconButton(Icons.Outlined.ArrowBack, "Back", onBack, tint = colors.textSecondary)
            Spacer(Modifier.width(spacing.sm))
            Column(Modifier.weight(1f)) {
                Text("New canvas", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Text("${spec.aspectLabel} · ${"%.1f".format(spec.megapixels)} MP", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            MonaButton(
                if (creating) "Creating…" else "Create",
                onClick = {
                    if (creating) return@MonaButton
                    creating = true
                    scope.launch {
                        val doc = viewModel.createDocument(spec)
                        viewModel.updateSettings { it.copy(lastCanvasPreset = spec.presetId ?: "custom") }
                        onCreated(doc.id)
                    }
                },
                icon = Icons.Outlined.Check,
                enabled = !creating && spec.width in 16..16384 && spec.height in 16..16384,
            )
        }

        Row(Modifier.weight(1f)) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.gutter),
            ) {
                // Canvas preview with the real aspect ratio
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(Mona.shapes.lg))
                        .background(colors.surface2)
                        .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.lg)),
                    contentAlignment = Alignment.Center,
                ) {
                    val ratio = spec.width.toFloat() / spec.height
                    val boxW = if (ratio >= 1f) 240.dp else (240 * ratio).dp
                    val boxH = if (ratio >= 1f) (240 / ratio).dp else 240.dp
                    Box(
                        Modifier
                            .size(width = boxW, height = boxH)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (spec.transparent) colors.surface3
                                else androidx.compose.ui.graphics.Color(spec.backgroundArgb),
                            )
                            .border(1.dp, colors.borderStrong),
                    )
                    Text(
                        "${spec.width} × ${spec.height}",
                        style = MonaType.numeric,
                        color = if (spec.transparent) colors.textSecondary else colors.textPrimary,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(6.dp),
                    )
                }

                Spacer(Modifier.height(spacing.md))
                SectionHeader("Presets", subtitle = "Print, screen, social and comic sizes")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CanvasSpec.presets.forEach { preset ->
                        MonaPill(preset.name, selected = spec.presetId == preset.id, onClick = {
                            spec = CanvasSpec.fromPreset(preset, name = spec.name)
                            widthText = preset.width.toString()
                            heightText = preset.height.toString()
                            dpiText = preset.dpi.toString()
                        })
                    }
                }
                if (customPresets.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.sm))
                    SectionHeader("Custom presets")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        customPresets.forEach { preset ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MonaPill(preset.name, selected = spec.presetId == preset.id, onClick = {
                                    spec = CanvasSpec.fromPreset(preset, name = spec.name)
                                    widthText = preset.width.toString()
                                    heightText = preset.height.toString()
                                    dpiText = preset.dpi.toString()
                                })
                                MonaIconButton(
                                    Icons.Outlined.Delete,
                                    "Delete ${preset.name}",
                                    onClick = {
                                        viewModel.deleteCanvasPreset(preset.id)
                                        customPresets = customPresets.filterNot { it.id == preset.id }
                                    },
                                    tint = Mona.colors.textTertiary,
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(spacing.md))
                SectionHeader("Custom size")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Width (px)", widthText, Modifier.weight(1f)) { text ->
                        widthText = text
                        text.toIntOrNull()?.let { if (it in 16..16384) spec = spec.copy(width = it, presetId = "custom") }
                    }
                    NumberField("Height (px)", heightText, Modifier.weight(1f)) { text ->
                        heightText = text
                        text.toIntOrNull()?.let { if (it in 16..16384) spec = spec.copy(height = it, presetId = "custom") }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    NumberField("DPI", dpiText, Modifier.weight(1f)) { text ->
                        dpiText = text
                        text.toIntOrNull()?.let { if (it in 36..1200) spec = spec.copy(dpi = it) }
                    }
                    MonaSegmented(
                        options = listOf("Portrait", "Landscape"),
                        selected = if (spec.height >= spec.width) "Portrait" else "Landscape",
                        onSelect = { orientation ->
                            val w = spec.width
                            val h = spec.height
                            val swap = (orientation == "Landscape" && h > w) || (orientation == "Portrait" && w > h)
                            if (swap) {
                                spec = spec.copy(width = h, height = w)
                                widthText = h.toString(); heightText = w.toString()
                            }
                        },
                        label = { it },
                        modifier = Modifier.weight(1.4f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonaPill("Swap W↔H", onClick = {
                        val w = spec.width
                        spec = spec.copy(width = spec.height, height = w, presetId = "custom")
                        widthText = spec.width.toString(); heightText = spec.height.toString()
                    })
                    MonaPill("Square", onClick = {
                        spec = spec.copy(height = spec.width, presetId = "custom")
                        heightText = spec.width.toString()
                    })
                }

                Spacer(Modifier.height(spacing.md))
                SectionHeader("Background")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    MonaPill(
                        text = "White",
                        selected = !spec.transparent && spec.backgroundArgb == 0xFFFFFFFF.toInt(),
                        onClick = { spec = spec.copy(transparent = false, backgroundArgb = 0xFFFFFFFF.toInt()) },
                    )
                    MonaPill(
                        text = "Transparent",
                        selected = spec.transparent,
                        onClick = { spec = spec.copy(transparent = true, backgroundArgb = 0) },
                    )
                    com.monaart.studio.core.design.MonaPalettes.artistPalettes.values.take(3).flatten().distinct().take(6).forEach { c ->
                        val argb = c.value.let { (it shr 32).toInt() }
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(c)
                                .border(1.dp, if (!spec.transparent && spec.backgroundArgb == argb) colors.accent else colors.border, RoundedCornerShape(8.dp))
                                .clickable { spec = spec.copy(transparent = false, backgroundArgb = argb) },
                        )
                    }
                }

                Spacer(Modifier.height(spacing.md))
                SectionHeader("Name and presets")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField("Project name", spec.name, Modifier.weight(1f), keyboardType = KeyboardType.Text) { spec = spec.copy(name = it) }
                    MonaPill("Save preset", onClick = {
                        val preset = CanvasPreset(
                            id = "custom-${System.currentTimeMillis()}",
                            name = "${spec.width}×${spec.height} @${spec.dpi}",
                            width = spec.width, height = spec.height, dpi = spec.dpi,
                            transparent = spec.transparent, backgroundArgb = spec.backgroundArgb, custom = true,
                        )
                        viewModel.saveCanvasPreset(preset)
                        customPresets = customPresets + preset
                    })
                }
                Spacer(Modifier.height(spacing.xl))
            }

            // Right column: size & memory facts
            Column(
                Modifier
                    .width(260.dp)
                    .padding(horizontal = spacing.gutter)
                    .verticalScroll(rememberScrollState()),
            ) {
                SectionHeader("Estimated cost")
                InfoCard {
                    InfoLine("Canvas", "${spec.width} × ${spec.height} px")
                    InfoLine("Pixels", "%.2f MP".format(spec.megapixels))
                    InfoLine("Physical size", "%.1f × %.1f cm".format(
                        spec.width / spec.dpi.toFloat() * 2.54f,
                        spec.height / spec.dpi.toFloat() * 2.54f,
                    ))
                    InfoLine("One full layer", formatBytesShort(estimatedBytes))
                    InfoLine("Memory model", "Tiled: only painted areas allocate")
                }
                if (warnings.isNotEmpty()) {
                    Spacer(Modifier.height(spacing.sm))
                    warnings.forEach { w ->
                        StatusBanner(w, tone = StatusTone.Warning)
                        Spacer(Modifier.height(6.dp))
                    }
                }
                Spacer(Modifier.height(spacing.md))
                SectionHeader("Tips")
                Text(
                    "Layers start empty. Painting 300 DPI for print or 72–144 DPI for screens is a good default. Transparent canvases export with alpha.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Mona.shapes.md))
            .background(Mona.colors.surface)
            .border(1.dp, Mona.colors.border, RoundedCornerShape(Mona.shapes.md))
            .padding(12.dp),
        content = content,
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Mona.colors.textSecondary, modifier = Modifier.weight(1f))
        Text(value, style = MonaType.numeric, color = Mona.colors.textPrimary)
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Number,
    onChange: (String) -> Unit,
) {
    val colors = Mona.colors
    Column(
        modifier
            .clip(RoundedCornerShape(Mona.shapes.sm))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.sm))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
        BasicTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
