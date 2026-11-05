package com.monaart.studio.presentation.settings

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monaart.studio.core.design.Mona
import com.monaart.studio.core.design.MonaPalettes
import com.monaart.studio.domain.model.CornerStyle
import com.monaart.studio.domain.model.Density
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import com.monaart.studio.domain.model.ThemeMode
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.presentation.common.formatBytesShort
import com.monaart.studio.ui.components.ColorSwatch
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.MonaSegmented
import com.monaart.studio.ui.components.ParamSlider
import com.monaart.studio.ui.components.SectionHeader
import com.monaart.studio.ui.components.StatusBanner
import com.monaart.studio.ui.components.StatusTone

/**
 * Application settings: theme, accessibility, stylus/gesture behaviour,
 * workspace defaults and storage. Every control writes through the ViewModel to
 * DataStore, so settings survive process death.
 */
@Composable
fun SettingsScreen(
    viewModel: MonaViewModel,
    settings: UiSettings,
    onBack: () -> Unit,
) {
    val colors = Mona.colors
    val storage by viewModel.storageBytes.collectAsStateWithLifecycle()
    var section by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().background(colors.ground)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonaIconButton(Icons.Outlined.ArrowBack, "Back", onBack, tint = colors.textSecondary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Settings", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Text("Mona Art 1.0.0 · offline, local-first", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
        }
        MonaSegmented(
            options = listOf(0, 1, 2, 3),
            selected = section,
            onSelect = { section = it },
            label = { listOf("Appearance", "Stylus", "Workspace", "Storage")[it] },
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        Spacer(Modifier.height(10.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            when (section) {
                0 -> AppearanceSection(viewModel, settings)
                1 -> StylusSection(viewModel, settings)
                2 -> WorkspaceSection(viewModel, settings)
                else -> StorageSection(viewModel, settings, storage)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AppearanceSection(viewModel: MonaViewModel, settings: UiSettings) {
    val colors = Mona.colors
    SectionHeader("Theme", subtitle = "AMOLED mode removes dark glow for OLED panels")
    MonaSegmented(
        options = ThemeMode.entries,
        selected = settings.themeMode,
        onSelect = viewModel::setTheme,
        label = { it.name },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    SectionHeader("Accent")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        MonaPalettes.accentChoices.forEach { c ->
            val argb = (c.value shr 32).toInt()
            ColorSwatch(c, size = 32.dp, selected = settings.accentArgb == argb, onClick = { viewModel.setAccent(argb) })
        }
        MonaPill("Default", selected = settings.accentArgb == 0, onClick = { viewModel.setAccent(0) })
    }
    Spacer(Modifier.height(10.dp))
    SectionHeader("Shape & density")
    MonaSegmented(
        options = CornerStyle.entries,
        selected = settings.cornerStyle,
        onSelect = viewModel::setCornerStyle,
        label = { it.name },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(6.dp))
    MonaSegmented(
        options = Density.entries,
        selected = settings.density,
        onSelect = viewModel::setDensity,
        label = { it.name },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    SectionHeader("Accessibility")
    ParamSlider("Font scale", settings.fontScale, { viewModel.setFontScale(it) }, range = 0.85f..1.35f, format = { "%.2f×".format(it) })
    ToggleRow("Reduce motion", "Replaces springs and slides with instant changes", settings.reduceMotion) {
        viewModel.setReduceMotion(it)
    }
    ToggleRow("Haptic feedback", "Confirms sliders and destructive actions", settings.haptics) {
        viewModel.setHaptics(it)
    }
    ToggleRow("Show tips", "Occasional guidance cards on the studio screen", settings.showOnboardingTips) {
        viewModel.updateSettings { s -> s.copy(showOnboardingTips = it) }
    }
    StatusBanner(
        "All interactive controls are 48dp or larger, use labels for screen readers, and never rely on colour alone to show state.",
        tone = StatusTone.Info,
    )
}

@Composable
private fun StylusSection(viewModel: MonaViewModel, settings: UiSettings) {
    val colors = Mona.colors
    SectionHeader("Input priority", subtitle = "How the app distinguishes a stylus from fingers and palms")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        InputMode.entries.forEach { mode ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Mona.shapes.md))
                    .background(if (settings.inputMode == mode) colors.accentMuted else colors.surface)
                    .border(1.dp, if (settings.inputMode == mode) colors.accent else colors.border, RoundedCornerShape(Mona.shapes.md))
                    .clickable { viewModel.setInputMode(mode) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(mode.label, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                    Text(mode.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                }
                if (settings.inputMode == mode) {
                    Text("Selected", style = MaterialTheme.typography.labelSmall, color = colors.accent)
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    ToggleRow("Palm rejection", "Ignore touches while the stylus is near or just lifted", settings.palmRejection) {
        viewModel.setPalmRejection(it)
    }
    ToggleRow("Canvas rotation", "Allow two-finger rotation", settings.canvasRotationEnabled) {
        viewModel.updateSettings { s -> s.copy(canvasRotationEnabled = it) }
    }
    ToggleRow("Double-tap to zoom", "Double tap the canvas to fit or zoom in", settings.doubleTapZoom) {
        viewModel.updateSettings { s -> s.copy(doubleTapZoom = it) }
    }
    Spacer(Modifier.height(10.dp))
    SectionHeader("Gestures")
    GesturePicker("Stylus button", settings.stylusButtonAction) { viewModel.setStylusButton(it) }
    GesturePicker("Two-finger tap", settings.twoFingerTap) { viewModel.setTwoFinger(it) }
    GesturePicker("Three-finger tap", settings.threeFingerTap) { viewModel.setThreeFinger(it) }
    Spacer(Modifier.height(10.dp))
    SectionHeader("Stroke engine")
    ParamSlider("Default stabilisation", settings.stabilizerPreset, { viewModel.setStabilizer(it) }, format = { "${(it * 100).toInt()}%" })
    ParamSlider("Pressure smoothing", settings.pressureSmoothing, { viewModel.updateSettings { s -> s.copy(pressureSmoothing = it) } }, format = { "${(it * 100).toInt()}%" })
    StatusBanner(
        "Tilt and hover depend on the stylus hardware. Devices that do not report tilt simply keep tilt-driven brushes perceptually perpendicular.",
        tone = StatusTone.Info,
    )
}

@Composable
private fun GesturePicker(label: String, current: GestureAction, onSelect: (GestureAction) -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Mona.colors.textSecondary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            GestureAction.entries.forEach { action ->
                MonaPill(action.label, selected = current == action, onClick = { onSelect(action) })
            }
        }
    }
}

@Composable
private fun WorkspaceSection(viewModel: MonaViewModel, settings: UiSettings) {
    SectionHeader("Default workspace")
    ToggleRow("Left tool rail", "Show the tool rail when opening a project", settings.leftRailVisible) {
        viewModel.updateSettings { s -> s.copy(leftRailVisible = it) }
    }
    ToggleRow("Right inspector", "Show brush/colour/layers inspector on tablets", settings.rightInspectorVisible) {
        viewModel.updateSettings { s -> s.copy(rightInspectorVisible = it) }
    }
    ToggleRow("Quick brush bar", "Bottom strip with size and opacity", settings.showQuickBrushBar) {
        viewModel.updateSettings { s -> s.copy(showQuickBrushBar = it) }
    }
    Spacer(Modifier.height(10.dp))
    SectionHeader("Saving")
    ParamSlider(
        "Autosave interval",
        settings.autosaveSeconds.toFloat(),
        { viewModel.setAutosave(it.toInt()) },
        range = 5f..120f,
        format = { "${it.toInt()} s" },
    )
    ParamSlider(
        "Undo history depth",
        settings.maxHistorySteps.toFloat(),
        { viewModel.setHistorySteps(it.toInt()) },
        range = 10f..400f,
        format = { "${it.toInt()} steps" },
    )
    Text(
        "History is measured in steps and bytes: deep histories on large canvases release the oldest snapshots first.",
        style = MaterialTheme.typography.bodySmall,
        color = Mona.colors.textTertiary,
    )
}

@Composable
private fun StorageSection(viewModel: MonaViewModel, settings: UiSettings, storage: Long) {
    val colors = Mona.colors
    SectionHeader("Storage", subtitle = "Projects live in app-private storage and never leave the device")
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.weight(1f)) {
            Text(formatBytesShort(storage), style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
            Text("used by project files and thumbnails", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        MonaPill("Refresh", onClick = { viewModel.refreshStorage() })
    }
    Spacer(Modifier.height(10.dp))
    SectionHeader("Thumbnail size", subtitle = "Longest edge of gallery and project-picker previews")
    MonaSegmented(
        options = listOf(256, 512, 1024, 2048),
        selected = settings.thumbnailQuality,
        onSelect = viewModel::setThumbnailQuality,
        label = { "$it px" },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(10.dp))
    SectionHeader("Project format")
    Text(
        "Native projects (.mona) are ZIP archives containing a versioned JSON manifest, DEFLATE-compressed tile data, a thumbnail and a SHA-256 checksum. Saves are atomic (temp file + rename), with the previous version kept as .bak and a recovery snapshot written by autosave.",
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary,
    )
    Spacer(Modifier.height(10.dp))
    SectionHeader("About")
    Text(
        "Mona Art 1.0.0 (format v3). Rendering engine, brush engine, compositor, filters and exporters are implemented in the app's own Kotlin engine module; no third-party paint engine or network service is used.",
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        "Bundled font: Inter (SIL OFL 1.1).",
        style = MaterialTheme.typography.labelSmall,
        color = colors.textTertiary,
    )
}

@Composable
private fun ToggleRow(label: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val colors = Mona.colors
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

