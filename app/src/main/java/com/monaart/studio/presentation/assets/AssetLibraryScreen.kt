package com.monaart.studio.presentation.assets

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
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.FileUpload
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monaart.engine.brush.Patterns
import com.monaart.studio.core.design.Mona
import com.monaart.studio.core.design.MonaPalettes
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.presentation.common.formatBytesShort
import com.monaart.studio.ui.components.ColorSwatch
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.SectionHeader

/**
 * Asset library: brushes, grain textures and colour palettes. Textures are
 * rendered from the same procedural generators the brush engine samples, so the
 * preview is the actual grain (no duplicated thumbnails that could drift).
 */
@Composable
fun AssetLibraryScreen(
    viewModel: MonaViewModel,
    settings: UiSettings,
    onBack: () -> Unit,
) {
    val colors = Mona.colors
    val presets by viewModel.brushes.presets.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf("Brushes") }
    var importText by remember { mutableStateOf<String?>(null) }
    // Palette swatches copy into the editor's primary colour, so the library is a
    // usable source of colours rather than a display of them.
    val context = androidx.compose.ui.platform.LocalContext.current
    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
    var copied by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(colors.ground)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonaIconButton(Icons.Outlined.ArrowBack, "Back", onBack, tint = colors.textSecondary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Asset library", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Text(
                    "Brushes, grain and palettes — all stored on device",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            MonaButton("Export brushes", {
                importText = viewModel.brushes.exportAll()
            }, icon = Icons.Outlined.FileUpload, tonal = true)
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Brushes", "Grain", "Palettes").forEach { label ->
                MonaPill(label, selected = tab == label, onClick = { tab = label })
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.weight(1f)) {
            when (tab) {
                "Brushes" -> LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(presets, key = { it.id }) { preset ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(Mona.shapes.md))
                                .background(colors.surface)
                                .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.md))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                                                        Icon(Icons.Outlined.Brush, contentDescription = null, tint =
                                colors.accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                                                Text(preset.name, style =
                                    MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text(
                                    "${preset.category} · ${preset.kind.name.lowercase()} · ${preset.size.toInt()}px " +
                                        "· ${(preset.opacity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                )
                                Text(
                                    buildString {
                                        append("spacing ${"%.2f".format(preset.spacing)}")
                                                                                if (preset.texture != null) append(
                                            " · grain ${preset.texture!!.removePrefix("grain.")}")
                                        if (preset.wetness > 0f) append(" · wet ${(preset.wetness * 100).toInt()}%")
                                                                                if (preset.taperMode.name !=
                                            "NONE") append(" · taper ${preset.taperMode.name.lowercase()}")
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                )
                            }
                            if (!preset.id.startsWith("user.")) {
                                MonaPill("Built-in")
                            } else {
                                MonaPill("Custom", color = colors.cyan)
                            }
                        }
                    }
                }
                "Grain" -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                ) {
                    SectionHeader("Procedural grain", subtitle = "Sampled per dab by textured brushes")
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Patterns.GRAIN_NAMES.forEach { name ->
                            Column(Modifier.width(150.dp)) {
                                GrainTile(name, Modifier.fillMaxWidth().height(150.dp))
                                Spacer(Modifier.height(4.dp))
                                                                Text(name.removePrefix("grain."), style =
                                    MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
                else -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                ) {
                    SectionHeader("Artist palettes", subtitle = "Tap a swatch to copy its hex colour")
                    MonaPalettes.artistPalettes.forEach { (name, swatches) ->
                                                Text(name, style = MaterialTheme.typography.titleSmall, color =
                            colors.textPrimary, modifier = Modifier.padding(top = 10.dp))
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier =
                            Modifier.padding(top = 6.dp)) {
                            swatches.forEach { c ->
                                val hex = "#%06X".format((c.value shr 32).toInt() and 0xFFFFFF)
                                ColorSwatch(c, size = 36.dp, onClick = {
                                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(hex))
                                    copied = "$name · $hex copied"
                                })
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    copied?.let { message ->
        Box(
            Modifier
                .fillMaxWidth()
                .background(colors.surface2)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(message, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary,
                    modifier = Modifier.weight(1f))
                MonaPill("Dismiss", onClick = { copied = null })
            }
        }
    }

    importText?.let { text ->
        // Export preview: the brush JSON is the same format the engine parses.
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { importText = null },
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier
                    .width(520.dp)
                    .clip(RoundedCornerShape(Mona.shapes.lg))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.lg))
                    .padding(16.dp),
            ) {
                Text("Brush presets (JSON)", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                Text(
                    "${presets.size} presets · ${formatBytesShort(text.length.toLong())} of JSON",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text.take(1400),
                                        style = MaterialTheme.typography.labelSmall.copy(fontFamily =
                        com.monaart.studio.core.design.MonaType.numeric.fontFamily),
                    color = colors.textSecondary,
                )
                Spacer(Modifier.height(10.dp))
                MonaButton("Close", { importText = null }, tonal = true)
            }
        }
    }
}

/** Renders a grain pattern tile by sampling it exactly like the brush does. */
@Composable
private fun GrainTile(name: String, modifier: Modifier = Modifier) {
    val colors = Mona.colors
    val tile = remember(name) { Patterns.grain(name) }
    Canvas(
        modifier
            .clip(RoundedCornerShape(Mona.shapes.md))
            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.md)),
    ) {
        drawRect(colors.surface2)
        val cells = 48
        val cw = size.width / cells
        val ch = size.height / cells
        for (y in 0 until cells) {
            for (x in 0 until cells) {
                val sx = (x * Patterns.TILE / cells)
                val sy = (y * Patterns.TILE / cells)
                val v = Patterns.sample(tile, sx, sy) / 255f
                drawRect(
                    color = Color(v, v, v, 1f),
                    topLeft = Offset(x * cw, y * ch),
                    size = Size(cw + 0.6f, ch + 0.6f),
                )
            }
        }
    }
}
