package com.monaart.studio.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Style
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monaart.engine.brush.BrushPresets
import com.monaart.studio.core.design.Mona
import com.monaart.studio.domain.model.ProjectSummary
import com.monaart.studio.domain.model.ThemeMode
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.presentation.common.ProjectThumbnail
import com.monaart.studio.presentation.common.formatBytesShort
import com.monaart.studio.presentation.common.relativeTime
import com.monaart.studio.ui.components.BentoCard
import com.monaart.studio.ui.components.Metric
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.SectionHeader
import com.monaart.studio.ui.components.StudioMark
import com.monaart.studio.ui.components.TipCard

/**
 * Creative Home: a Bento dashboard that orients the artist in one glance —
 * continue drawing, start something new, browse the library, and see library
 * health. Deliberately not a drawing surface.
 */
@Composable
fun HomeScreen(
    viewModel: MonaViewModel,
    settings: UiSettings,
    projects: List<ProjectSummary>,
    onNewCanvas: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onBrushStudio: () -> Unit,
    onAssets: () -> Unit,
    onSettings: () -> Unit,
) {
    val colors = Mona.colors
    val spacing = Mona.spacing
    val recent = projects.take(6)
    val favorites = projects.filter { it.favorite }.take(4)
    var showTip by remember { mutableStateOf(settings.showOnboardingTips) }
    val storageBytes by viewModel.storageBytes.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.ground),
        contentPadding = PaddingValues(
            start = spacing.gutter, end = spacing.gutter,
            top = spacing.xl, bottom = spacing.xxl + 80.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        // ---------------- Header ----------------
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StudioMark(44.dp)
                Spacer(Modifier.width(spacing.md))
                Column(Modifier.weight(1f)) {
                    Text("Mona Art", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                    Text(
                        "Digital painting studio",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                    )
                }
                MonaIconButton(Icons.Outlined.Search, "Search projects", onOpenGallery, tint = colors.textSecondary)
                MonaIconButton(Icons.Outlined.Settings, "Settings", onSettings, tint = colors.textSecondary)
            }
        }

        // ---------------- Hero: continue or create ----------------
        item {
            BentoCard(
                modifier = Modifier.fillMaxWidth(),
                accent = colors.accent,
                contentPadding = spacing.cardPadding + 4.dp,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (recent.isEmpty()) "Your canvas is waiting" else "Welcome back",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (recent.isEmpty()) "Create a canvas, pick a brush, and start painting with pressure sensitivity."
                            else "Pick up “${recent.first().name}” or start a new piece.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                    }
                    Spacer(Modifier.width(spacing.md))
                    Box(
                        Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.linearGradient(listOf(colors.accent, colors.cyan.copy(alpha = 0.8f))),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Brush, contentDescription = null, tint = colors.accentOn)
                    }
                }
                Spacer(Modifier.height(spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    MonaButton("New canvas", onNewCanvas, icon = Icons.Outlined.Add)
                    MonaButton("Gallery", onOpenGallery, tonal = true, icon = Icons.Outlined.GridView)
                }
            }
        }

        // ---------------- Bento row: brushes + assets ----------------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    onClick = onBrushStudio,
                    contentPadding = spacing.md,
                ) {
                    Icon(Icons.Outlined.Brush, contentDescription = null, tint = colors.cyan, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(spacing.sm))
                    Text("Brush studio", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                    Text(
                        "${BrushPresets.all.size} presets · ${BrushPresets.categories.size} families",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                    )
                }
                BentoCard(
                    modifier = Modifier.weight(1f),
                    onClick = onAssets,
                    contentPadding = spacing.md,
                ) {
                    Icon(Icons.Outlined.Style, contentDescription = null, tint = colors.pink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.height(spacing.sm))
                    Text("Asset library", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                    Text(
                        "Grain, palettes, stamps",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                    )
                }
            }
        }

        // ---------------- Recent projects ----------------
        item {
            SectionHeader(
                title = "Recent work",
                subtitle = if (projects.isEmpty()) "Nothing yet — your first canvas is one tap away" else "${projects.size} projects",
                trailing = {
                    if (projects.isNotEmpty()) {
                        MonaPill("View all", onClick = onOpenGallery)
                    }
                },
            )
        }

        if (recent.isEmpty()) {
            item {
                BentoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.surface2),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = null, tint = colors.textTertiary)
                        }
                        Spacer(Modifier.width(spacing.md))
                        Column(Modifier.weight(1f)) {
                            Text("No projects yet", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                            Text(
                                "Canvases you create appear here with a live thumbnail.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary,
                            )
                        }
                    }
                }
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                    items(recent, key = { it.id }) { project ->
                        RecentProjectCard(project) { onOpenProject(project.id) }
                    }
                }
            }
        }

        // ---------------- Favourites ----------------
        if (favorites.isNotEmpty()) {
            item {
                SectionHeader(title = "Favourites", subtitle = "Pinned by you")
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    items(favorites, key = { it.id }) { project ->
                        Column(
                            Modifier
                                .width(120.dp)
                                .clip(RoundedCornerShape(Mona.shapes.md))
                                .clickable { onOpenProject(project.id) }
                                .background(colors.surface)
                                .padding(spacing.sm),
                        ) {
                            ProjectThumbnail(project, Modifier.fillMaxWidth().aspectRatio(1f))
                            Spacer(Modifier.height(6.dp))
                            Text(
                                project.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        // ---------------- Studio health / tip ----------------
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                BentoCard(modifier = Modifier.weight(1.2f), contentPadding = spacing.md) {
                    Text("Library", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Metric("Projects", projects.size.toString(), Modifier.weight(1f))
                        Metric(
                            "Storage",
                            formatBytesShort(storageBytes),
                            Modifier.weight(1f),
                            accent = colors.accent,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Everything is stored on this device. No account, no network.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                }
                BentoCard(modifier = Modifier.weight(1f), contentPadding = spacing.md) {
                    Text("Appearance", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ThemeChip("Dark", settings.themeMode == ThemeMode.Dark) {
                            viewModel.setTheme(ThemeMode.Dark)
                        }
                        ThemeChip("Light", settings.themeMode == ThemeMode.Light) {
                            viewModel.setTheme(ThemeMode.Light)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    ThemeChip("AMOLED", settings.themeMode == ThemeMode.Amoled) {
                        viewModel.setTheme(ThemeMode.Amoled)
                    }
                }
            }
        }

        if (showTip && settings.showOnboardingTips) {
            item {
                TipCard(
                    text = "Rest your palm on the screen: with the default “Stylus only” input mode, fingers pan and zoom but never paint.",
                    onDismiss = {
                        showTip = false
                        viewModel.updateSettings { it.copy(showOnboardingTips = false) }
                    },
                )
            }
        }

        item { Spacer(Modifier.height(40.dp)) }
    }
}

@Composable
private fun RecentProjectCard(project: ProjectSummary, onClick: () -> Unit) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    Column(
        Modifier
            .width(190.dp)
            .clip(RoundedCornerShape(shapes.lg))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(shapes.lg))
            .clickable(onClick = onClick)
            .padding(Mona.spacing.sm),
    ) {
        ProjectThumbnail(project, Modifier.fillMaxWidth().aspectRatio(1.25f))
        Spacer(Modifier.height(Mona.spacing.sm))
        Text(
            project.name,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${project.width}×${project.height} · ${relativeTime(project.modifiedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (project.favorite) {
                Icon(Icons.Outlined.Star, contentDescription = "Favourite", tint = colors.accent, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ThemeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = Mona.colors
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) colors.accent else colors.surface2)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) colors.accentOn else colors.textSecondary,
            fontWeight = FontWeight.Medium,
        )
    }
}

