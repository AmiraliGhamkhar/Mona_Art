package com.monaart.studio.presentation.brushstudio

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monaart.engine.brush.BrushEngine
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.BrushSpec
import com.monaart.engine.brush.StrokePoint
import com.monaart.engine.document.ArtDocument
import com.monaart.studio.core.design.Mona
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.presentation.editor.BrushControls
import com.monaart.studio.presentation.editor.EditorViewModel
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.SectionHeader
import kotlinx.coroutines.launch

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
    // The studio borrows a scratch editor solely for preset storage/status; the
    // document being drawn on (if any) is never touched.
    val editorVm: EditorViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }
    var importText by remember { mutableStateOf("") }
    var showImport by remember { mutableStateOf(false) }

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
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                MonaPill(if (showImport) "Cancel import" else "Import", onClick = { showImport = !showImport })
                MonaPill("Share JSON", onClick = {
                    val json = viewModel.brushes.exportPreset(working.copy(name = nameDraft.ifBlank { working.name }))
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(android.content.Intent.EXTRA_TEXT, json)
                        putExtra(android.content.Intent.EXTRA_SUBJECT, working.name)
                    }
                    context.startActivity(android.content.Intent.createChooser(send, "Share brush preset"))
                })
                MonaButton("Save as new", {
                    working = working.copy(name = nameDraft.ifBlank { working.name })
                    viewModel.saveBrush(working) { saved -> select(saved); status = "Saved \"${saved.name}\"" }
                }, icon = Icons.Outlined.Save, tonal = true)
            }
        }

        if (showImport) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    "Paste a preset exported from Mona Art (single object or an array).",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 120.dp)
                        .clip(RoundedCornerShape(Mona.shapes.sm))
                        .background(colors.surface2)
                        .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.sm))
                        .padding(8.dp),
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MonaButton("Import presets", {
                        val text = importText.trim()
                        if (text.isEmpty()) {
                            status = "Nothing to import"
                        } else {
                            scope.launch {
                                val added = viewModel.brushes.importPresets(text)
                                                                status = if (added.isEmpty(
                                    )) "No valid presets found" else "Imported ${added.size} preset(s)"
                                added.firstOrNull()?.let { select(it) }
                                importText = ""
                                showImport = false
                            }
                        }
                    }, tonal = true)
                    MonaPill("Clear", onClick = { importText = "" })
                }
            }
        }
        status?.let { message ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(colors.surface2)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                                Text(message, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary,
                    modifier = Modifier.weight(1f))
                MonaPill("Dismiss", onClick = { status = null })
            }
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
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement =
                    Arrangement.spacedBy(4.dp)) {
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
                                                                .border(1.dp, if (
                                    isSelected) colors.accent else colors.border, RoundedCornerShape(Mona.shapes.sm))
                                .clickable { select(preset) }
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                                                Text(preset.name, style =
                                    MaterialTheme.typography.labelLarge, color = colors.textPrimary, maxLines = 1)
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement =
                    Arrangement.spacedBy(8.dp)) {
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
                BrushControls(
                    editor = editorVm,
                    state = editorVm.brushStudioState(working),
                    onBrushChange = { updated ->
                        working = updated
                        previewSeed++
                    },
                    presets = presets,
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}



/**
 * Renders real engine strokes with [BrushEngine] onto a transparent document,
 * then rasterises the result at the preview's pixel size. Because it runs the
 * actual stroke pipeline, grain, taper, wetness and pressure response in the
 * preview are the same ones the canvas produces.
 */
internal fun renderPreviewBitmap(
    spec: BrushSpec,
    width: Int,
    height: Int,
): android.graphics.Bitmap? = runCatching {
    val doc = ArtDocument.create("preview", width, height, transparentBackground = true)
    val layer = doc.activeLayer() ?: return null
    val engine = BrushEngine()
    val color = 0xFFEEEAF6.toInt()
    val strokes = 3
    for (s in 0 until strokes) {
        val y = height * (0.30f + s * 0.20f)
        engine.begin(doc, layer, spec.copy(size = spec.size.coerceAtMost(height * 0.32f)), color, seed = 1000 + s)
        val steps = 160
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = 16f + t * (width - 32f)
            // Pressure ramp: light at the ends, full in the middle.
            val pressure = (0.12f + 0.88f * kotlin.math.sin(t * Math.PI).toFloat()).coerceIn(0.02f, 1f)
            val wobble = if (s == 2) kotlin.math.sin(t * 8f) * height * 0.06f else 0f
            engine.move(StrokePoint(x, y + wobble, pressure, timestampMillis = i * 6L))
            if (i % 8 == 0) engine.flush()
        }
        engine.end()
    }
    val out = com.monaart.engine.paint.Pixmap(width, height)
    com.monaart.engine.document.Compositor().render(
        doc,
        out,
        com.monaart.engine.paint.RectI(0, 0, width, height),
        includeBackground = false,
    )
    val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
    bmp.setPixels(out.straightPixels(), 0, width, 0, 0, width, height)
    bmp
}.getOrNull()

@Composable
private fun BrushPreview(spec: BrushSpec, seed: Int, modifier: Modifier = Modifier) {
    val colors = Mona.colors
    val density = LocalDensity.current
    val widthPx = with(density) { 900.dp.roundToPx() }.coerceIn(320, 1600)
    val heightPx = (widthPx * 0.22f).toInt().coerceIn(80, 400)
    val image = remember(spec, seed, widthPx, heightPx) {
        val bmp: android.graphics.Bitmap? = renderPreviewBitmap(spec, widthPx, heightPx)
        bmp?.asImageBitmap()
    }
    Box(
        modifier
            .clip(RoundedCornerShape(Mona.shapes.lg))
            .background(colors.surface2)
            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.lg)),
        contentAlignment = Alignment.BottomStart,
    ) {
        if (image != null) {
            androidx.compose.foundation.Image(
                bitmap = image,
                contentDescription = "Live preview of ${spec.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds,
            )
        } else {
            Text(
                "Preview unavailable",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .background(colors.ground.copy(alpha = 0.6f))
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
