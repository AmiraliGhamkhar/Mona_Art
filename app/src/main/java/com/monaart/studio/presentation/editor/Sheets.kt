package com.monaart.studio.presentation.editor

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monaart.engine.io.ExportFormat
import com.monaart.studio.core.design.Mona
import com.monaart.studio.core.design.MonaType
import com.monaart.studio.presentation.common.InfoRow
import com.monaart.studio.presentation.common.formatBytesShort
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.MonaSlider
import com.monaart.studio.ui.components.ParamSlider
import com.monaart.studio.ui.components.SectionHeader
import com.monaart.studio.ui.components.StatusBanner
import com.monaart.studio.ui.components.StatusTone

/** Dimmed scrim + bottom sheet container used by every editor panel. */
@Composable
private fun SheetScaffold(
    title: String,
    onDismiss: () -> Unit,
    maxHeightFraction: Float = 0.72f,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxSize(1f)
            .padding(top = 64.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = shapes.xl, topEnd = shapes.xl))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(topStart = shapes.xl, topEnd = shapes.xl))
                .heightIn(max = 560.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
                MonaIconButton(Icons.Outlined.Close, "Close", onDismiss, tint = colors.textSecondary)
            }
            Column(Modifier.verticalScroll(rememberScrollState())) { content() }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun LayersSheet(editor: EditorViewModel, state: EditorUiState, onDismiss: () -> Unit, expanded: Boolean) {
    SheetScaffold(title = "Layers", onDismiss = onDismiss) {
        LayerList(editor, state, compact = false)
    }
}

@Composable
fun BrushSheet(editor: EditorViewModel, state: EditorUiState, onDismiss: () -> Unit) {
    SheetScaffold(title = "Brush", onDismiss = onDismiss) {
        BrushControls(editor, state)
    }
}

@Composable
fun ColorSheet(editor: EditorViewModel, state: EditorUiState, onDismiss: () -> Unit) {
    SheetScaffold(title = "Colour", onDismiss = onDismiss) {
        ColorControls(editor, state)
    }
}

/** Filter/adjustment preview with live amount control. */
@Composable
fun FilterDialog(editor: EditorViewModel, state: EditorUiState) {
    val kind = state.filterPreview ?: return
    val colors = Mona.colors
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxSize(1f)
            .padding(top = 120.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(Mona.shapes.lg))
                .background(colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.lg))
                .padding(16.dp),
        ) {
            Text(kind.label, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(
                "Preview is live. Apply to commit to the layer.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(10.dp))
            if (kind.max > kind.min) {
                ParamSlider(
                    kind.paramLabel,
                    state.filterAmount,
                    { editor.updateFilterAmount(it) },
                    range = kind.min..kind.max,
                    format = { "%.2f".format(it) },
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonaButton("Apply", { editor.applyFilterPreview() })
                MonaButton("Cancel", { editor.cancelFilterPreview() }, tonal = true)
            }
        }
    }
}

/**
 * Export sheet: format, scale and quality. The chosen values travel unmodified
 * through [ExportRequest] to the actual exporter — the sheet collects exactly the
 * settings the export honours (scale applies to raster formats, quality to JPEG).
 */
@Composable
fun ExportSheet(state: EditorUiState, onDismiss: () -> Unit, onExport: (ExportRequest) -> Unit) {
    val colors = Mona.colors
    var format by remember { mutableStateOf(ExportFormat.PNG) }
    var scale by remember { mutableStateOf(1f) }
    var quality by remember { mutableStateOf(92f) }
    val doc = state.document
    SheetScaffold(title = "Export", onDismiss = onDismiss) {
        Text(
            "Rendered with the same compositor as the canvas, so exports match what you see.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(10.dp))
        SectionHeader("Format")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ExportFormat.entries.forEach { fmt ->
                MonaPill(fmt.label, selected = format == fmt, onClick = { format = fmt })
            }
        }
        Spacer(Modifier.height(10.dp))
        if (format == ExportFormat.PNG || format == ExportFormat.JPEG || format == ExportFormat.TIFF) {
            SectionHeader("Resolution")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.5f, 1f, 2f).forEach { s ->
                    MonaPill("${(s * 100).toInt()}%", selected = scale == s, onClick = { scale = s })
                }
            }
        }
        if (format == ExportFormat.JPEG) {
            Spacer(Modifier.height(8.dp))
            ParamSlider("Quality", quality, { quality = it }, range = 30f..100f, format = { "${it.toInt()}" })
        }
        Spacer(Modifier.height(10.dp))
        if (doc != null) {
            val w = (doc.width * scale).toInt()
            val h = (doc.height * scale).toInt()
            InfoRow("Output size", "$w × $h px")
            InfoRow("Layers", if (format.supportsLayers) "Preserved (${state.layers.size})" else "Flattened")
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MonaButton(
                "Export",
                { onExport(ExportRequest(format, scale, quality.toInt().coerceIn(1, 100))) },
                enabled = !state.exportInProgress,
            )
            MonaButton("Cancel", onDismiss, tonal = true)
        }
        if (state.exportInProgress) {
            Spacer(Modifier.height(8.dp))
            Text("Rendering export…", style = MaterialTheme.typography.bodySmall, color = colors.accent)
        }
    }
}

/** Document/canvas settings, transform actions and storage info. */
@Composable
fun DocumentSettingsSheet(editor: EditorViewModel, state: EditorUiState, onDismiss: () -> Unit) {
    val colors = Mona.colors
    val doc = state.document
    SheetScaffold(title = "Document", onDismiss = onDismiss) {
        if (doc == null) return@SheetScaffold
        SectionHeader("Canvas")
        var name by remember(doc.id) { mutableStateOf(doc.name) }
        BasicTextField(
            value = name,
            onValueChange = { name = it; if (it.isNotBlank()) editor.renameDocument(it) },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(color = colors.textPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Mona.shapes.md))
                .background(colors.surface2)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        )
        Spacer(Modifier.height(6.dp))
        InfoRow("Size", "${doc.width} × ${doc.height} px")
        InfoRow("Resolution", "${doc.dpi} DPI")
        val inchesW = doc.width / doc.dpi.toFloat()
        val inchesH = doc.height / doc.dpi.toFloat()
        InfoRow("Physical", "%.2f × %.2f in".format(inchesW, inchesH))
        InfoRow("Background", if (doc.transparentBackground) "Transparent" else "#%06X".format(doc.backgroundColor and 0xFFFFFF))
        InfoRow("Layer pixel memory", formatBytesShort(state.memoryBytes))
        Spacer(Modifier.height(10.dp))
        SectionHeader("Transform")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MonaPill("Crop to selection", onClick = { editor.cropToSelection() })
            MonaPill("Rotate 90°", onClick = { editor.rotateCanvas(90) })
            MonaPill("Rotate 180°", onClick = { editor.rotateCanvas(180) })
            MonaPill("Flip H", onClick = { editor.flipCanvas(true) })
            MonaPill("Flip V", onClick = { editor.flipCanvas(false) })
        }
        Spacer(Modifier.height(10.dp))
        SectionHeader("Adjustments", subtitle = "Applied to the active layer, live preview")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            com.monaart.engine.filter.FilterKind.entries.forEach { kind ->
                MonaPill(kind.label, onClick = { onDismiss(); editor.beginFilterPreview(kind) })
            }
        }
        Spacer(Modifier.height(10.dp))
        SectionHeader("Selection")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            MonaPill("Select all", onClick = { editor.selectAll() })
            MonaPill("Invert", onClick = { editor.invertSelection() })
            MonaPill("Feather 4 px", onClick = { editor.featherSelection(4) })
            MonaPill("Grow 2 px", onClick = { editor.growSelection(2) })
            MonaPill("Shrink 2 px", onClick = { editor.shrinkSelection(2) })
            MonaPill("Deselect", onClick = { editor.clearSelection() })
        }
        Spacer(Modifier.height(10.dp))
        SectionHeader("Symmetry & guides")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MonaPill("Vertical symmetry", selected = state.symmetryVertical, onClick = { editor.toggleSymmetry(true) })
            MonaPill("Horizontal symmetry", selected = state.symmetryHorizontal, onClick = { editor.toggleSymmetry(false) })
        }
        Spacer(Modifier.height(12.dp))
        MonaButton("Save now", { editor.save() })
        Spacer(Modifier.height(24.dp))
    }
}

