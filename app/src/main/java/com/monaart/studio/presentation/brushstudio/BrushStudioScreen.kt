package com.monaart.studio.presentation.brushstudio

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monaart.engine.brush.BrushEngine
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.BrushSpec
import com.monaart.engine.brush.StrokePoint
import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.LayerOps
import com.monaart.engine.paint.alphaOf
import com.monaart.studio.core.design.Mona
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.presentation.common.formatBytesShort
import com.monaart.studio.presentation.editor.BrushControls
import com.monaart.studio.presentation.editor.EditorViewModel
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.SectionHeader

/**
 * Brush studio. The preview strokes are rendered by the *real* engine
 * ([BrushEngine]) into an offscreen document, then blitted — so what you tune
 * here is exactly what the canvas does, including grain, taper and pressure.
 */
@Composable
fun BrushStudioScreen(
    viewModel: MonaViewModel,
    settings: UiSettings,
    onBack: () -> Unit,
) {
    val colors = Mona.colors
    val presets by viewModel.brushes.presets.collectAsStateWithLifecycle()
    val favorites by viewModel.brushes.favorites.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf(BrushPresets.default) }
    var working by remember { mutableStateOf(selected) }
    var category by remember { mutableStateOf<String?>(null) }
    var previewSeed by remember { mutableStateOf(0) }
    var nameDraft by remember { mutableStateOf(working.name) }

    fun select(preset: BrushSpec) {
        selected = preset
        working = preset
        nameDraft = preset.name
        previewSeed++
    }

    Column(Modifier.fillMaxSize().background(colors.ground)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonaIconButton(Icons.Outlined.ArrowBack, "Back", onBack, tint = colors.textSecondary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Brush studio", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Text(
                    "${presets.size} presets · live preview rendered by the painting engine",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            MonaButton("Save as new", {
                working = working.copy(name = nameDraft.ifBlank { working.name })
                viewModel.saveBrush(working) { saved -> select(saved) }
            }, icon = Icons.Outlined.Save, tonal = true)
        }

        Row(Modifier.weight(1f)) {
            // Preset browser
            Column(
                Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .background(colors.surface)
                    .padding(10.dp),
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MonaPill("All", selected = category == null, onClick = { category = null })
                    BrushPresets.categories.forEach { cat ->
                        MonaPill(cat, selected = category == cat, onClick = { category = cat })
                    }
                }
                Spacer(Modifier.height(8.dp))
                val visible = presets.filter { category == null || it.category == category }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(visible, key = { it.id }) { preset ->
                        val isSelected = preset.id == selected.id
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Mona.shapes.sm))
                                .background(if (isSelected) colors.accentMuted else colors.surface2)
                                .border(1.dp, if (isSelected) colors.accent else colors.border, RoundedCornerShape(Mona.shapes.sm))
                                .clickable { select(preset) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(preset.name, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary, maxLines = 1)
                                Text(
                                    "${preset.category} · ${preset.kind.name.lowercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                    maxLines = 1,
                                )
                            }
                            if (preset.id.startsWith("user.")) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete preset",
                                    tint = colors.textTertiary,
                                    modifier = Modifier.size(16.dp).clickable { viewModel.deleteBrush(preset.id) },
                                )
                                Spacer(Modifier.width(6.dp))
                            }
                            Icon(
                                if (favorites.contains(preset.id)) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favourite",
                                tint = if (favorites.contains(preset.id)) colors.accent else colors.textTertiary,
                                modifier = Modifier.size(16.dp).clickable { viewModel.toggleBrushFavorite(preset.id) },
                            )
                        }
                    }
                }
            }

            // Preview + properties
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                BrushPreview(working, previewSeed, Modifier.fillMaxWidth().height(200.dp))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonaPill("Re-render", onClick = { previewSeed++ })
                    MonaPill("Reset", onClick = { select(BrushPresets.byId(working.id) ?: working) })
                    MonaPill("Duplicate", onClick = {
                        select(working.copy(id = "user.${System.currentTimeMillis()}", name = "${working.name} copy"))
                    })
                }
                Spacer(Modifier.height(12.dp))
                SectionHeader("Preset name")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(Mona.shapes.sm))
                            .background(colors.surface)
                            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.sm))
                            .padding(10.dp),
                    ) {
                        androidx.compose.foundation.text.BasicTextField(
                            value = nameDraft,
                            onValueChange = { nameDraft = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                        )
                    }
                    MonaButton("Apply", { working = working.copy(name = nameDraft) }, tonal = true)
                }
                Spacer(Modifier.height(12.dp))
                BrushControls(StudioEditorBridge(working) { updated -> working = updated; previewSeed++ }, studioState(working))
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Lightweight adapter so the shared brush control surface can edit a local
 * `working` spec without needing a document. Keeps one implementation of the
 * controls for both the inspector and the studio.
 */
@Composable
private fun StudioEditorBridge(
    working: BrushSpec,
    onChange: (BrushSpec) -> Unit,
): EditorViewModel {
    // Reuse the real view model so saved presets and status messages behave
    // identically; the studio edits a scratch copy of its brush.
    val vm: EditorViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = com.monaart.studio.app.monaViewModelFactory(),
    )
    androidx.compose.runtime.LaunchedEffect(working) { vm.adoptStudioBrush(working) }
    return vm
}

private fun studioState(working: BrushSpec) = com.monaart.studio.presentation.editor.EditorUiState(
    brush = working,
    size = working.size,
    opacity = working.opacity,
    flow = working.flow,
    spacing = working.spacing,
    hardness = working.hardness,
    stabilization = working.stabilization,
    pressureSize = working.pressureSize,
    pressureOpacity = working.pressureOpacity,
    taper = working.taperMode,
    grain = working.texture,
    grainStrength = working.grainStrength,
)

/**
 * Renders real strokes with [BrushEngine] into an offscreen transparent canvas
 * and averages their coverage into the preview. Pressure ramps from 15% to 100%
 * so the pressure response is visible.
 */
@Composable
private fun BrushPreview(spec: BrushSpec, seed: Int, modifier: Modifier = Modifier) {
    val colors = Mona.colors
    val preview = remember(spec, seed) { renderBrushPreview(spec) }
    Box(
        modifier
            .clip(RoundedCornerShape(Mona.shapes.lg))
            .background(colors.surface2)
            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.lg)),
        contentAlignment = Alignment.BottomStart,
    ) {
        val image = remember(preview) {
            preview?.let {
                val bmp = android.graphics.Bitmap.createBitmap(it.width, it.height, android.graphics.Bitmap.Config.ARGB_8888)
                bmp.setPixels(it.straightPixels(), 0, it.width, 0, 0, it.width, it.height)
                androidx.compose.ui.graphics.asImageBitmap(bmp)
            }
        }
        if (image != null) {
            androidx.compose.foundation.Image(
                bitmap = image,
                contentDescription = "Brush preview stroke",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.ground.copy(alpha = 0.55f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(spec.name, style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
            Text(
                "${spec.size.toInt()} px · ${(spec.opacity * 100).toInt()}% · spacing ${"%.2f".format(spec.spacing)}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
            )
        }
    }
}

/** Runs the engine on a scratch document sized to the preview. */
internal fun renderBrushPreview(spec: BrushSpec, width: Int = 900, height: Int = 200): com.monaart.engine.paint.Pixmap? {
    return runCatching {
        val doc = ArtDocument.create("preview", width, height, transparentBackground = true)
        val layer = doc.activeLayer() ?: return null
        val engine = BrushEngine()
        val color = 0xFFEEEAF6.toInt()
        val strokes = 3
        for (s in 0 until strokes) {
            val y = height * (0.28f + s * 0.22f)
            engine.begin(doc, layer, spec.copy(size = spec.size.coerceAtMost(height * 0.35f)), color, seed = 1000 + s)
            val steps = 120
            for (i in 0..steps) {
                val t = i.toFloat() / steps
                val x = 20f + t * (width - 40f)
                // Pressure ramp: light at the start, full in the middle, tapered end.
                val pressure = (0.15f + 0.85f * kotlin.math.sin(t * Math.PI).toFloat()).coerceIn(0.02f, 1f)
                engine.move(StrokePoint(x, y + (if (s == 2) kotlin.math.sin(t * 8f) * 12f else 0f), pressure, timestampMillis = i * 8L))
                if (i % 6 == 0) engine.flush()
            }
            engine.end()
        }
        val out = com.monaart.engine.paint.Pixmap(width, height)
        com.monaart.engine.document.Compositor().render(doc, out, com.monaart.engine.paint.RectI.wh(width, height), includeBackground = false)
        out
    }.getOrNull()
}
