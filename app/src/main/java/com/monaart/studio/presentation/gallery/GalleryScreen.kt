package com.monaart.studio.presentation.gallery

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monaart.studio.core.design.Mona
import com.monaart.studio.domain.model.ProjectFilter
import com.monaart.studio.domain.model.ProjectSort
import com.monaart.studio.domain.model.ProjectSummary
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.presentation.common.ProjectThumbnail
import com.monaart.studio.presentation.common.relativeTime
import com.monaart.studio.ui.components.BentoCard
import com.monaart.studio.ui.components.EmptyState
import com.monaart.studio.ui.components.MonaButton
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaPill
import com.monaart.studio.ui.components.SectionHeader

/**
 * Project gallery: search, sort, filters, favourites and destructive actions
 * with confirmation. Thumbnails are read from disk and cached per version.
 */
@Composable
fun GalleryScreen(
    viewModel: MonaViewModel,
    settings: UiSettings,
    projects: List<ProjectSummary>,
    onBack: () -> Unit,
    onOpenProject: (Long) -> Unit,
    onNewCanvas: () -> Unit,
) {
    val colors = Mona.colors
    val spacing = Mona.spacing
    val query by viewModel.query.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val favoritesOnly by viewModel.favoritesOnly.collectAsStateWithLifecycle()
    val storage by viewModel.storageBytes.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<ProjectSummary?>(null) }
    var sortOpen by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(colors.ground)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = spacing.gutter, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonaIconButton(Icons.Outlined.Close, "Close gallery", onBack, tint = colors.textSecondary)
            Spacer(Modifier.width(spacing.sm))
            Column(Modifier.weight(1f)) {
                Text("Gallery", style = MaterialTheme.typography.headlineMedium, color = colors.textPrimary)
                Text(
                    "${projects.size} projects · ${com.monaart.studio.presentation.common.formatBytesShort(storage)} used",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            MonaButton("New", onNewCanvas, icon = Icons.Outlined.Add)
        }

        // Search + filters
        Row(
            Modifier.fillMaxWidth().padding(horizontal = spacing.gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.surface)
                    .border(1.dp, colors.border, RoundedCornerShape(999.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text("Search projects", style = MaterialTheme.typography.bodyMedium, color = colors.textTertiary)
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = viewModel::setQuery,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = "Clear search",
                        tint = colors.textTertiary,
                        modifier = Modifier.size(16.dp).clickable { viewModel.setQuery("") },
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Box {
                MonaPill(sort.label, onClick = { sortOpen = !sortOpen })
                if (sortOpen) {
                    Column(
                        Modifier
                            .padding(top = 34.dp)
                            .width(180.dp)
                            .clip(RoundedCornerShape(Mona.shapes.md))
                            .background(colors.surface2)
                            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.md))
                            .padding(4.dp),
                    ) {
                        ProjectSort.entries.forEach { option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(Mona.shapes.sm))
                                    .background(if (sort == option) colors.accentMuted else colors.surface2)
                                    .clickable { viewModel.setSort(option); sortOpen = false }
                                    .padding(10.dp),
                            ) {
                                Text(option.label, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(spacing.sm))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = spacing.gutter),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ProjectFilter.entries.forEach { option ->
                MonaPill(option.label, selected = filter == option, onClick = { viewModel.setFilter(option) })
            }
            MonaPill("★", selected = favoritesOnly, onClick = { viewModel.toggleFavoritesOnly() })
        }
        Spacer(Modifier.height(spacing.sm))

        if (projects.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.FolderOpen,
                title = if (query.isBlank()) "No projects yet" else "Nothing matches “$query”",
                message = if (query.isBlank()) {
                    "Create your first canvas and it will appear here with a live preview."
                } else {
                    "Try a different search term or clear the filters."
                },
                actionLabel = if (query.isBlank()) "New canvas" else "Clear search",
                onAction = { if (query.isBlank()) onNewCanvas() else viewModel.setQuery("") },
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 190.dp),
                contentPadding = PaddingValues(spacing.gutter),
                horizontalArrangement = Arrangement.spacedBy(spacing.md),
                verticalArrangement = Arrangement.spacedBy(spacing.md),
            ) {
                items(projects, key = { it.id }) { project ->
                    ProjectCard(
                        project = project,
                        onOpen = { onOpenProject(project.id) },
                        onFavorite = { viewModel.toggleFavorite(project) },
                        onDelete = { pendingDelete = project },
                    )
                }
            }
        }
    }

    pendingDelete?.let { project ->
        ConfirmDelete(project, onDismiss = { pendingDelete = null }, onConfirm = {
            viewModel.trash(project)
            pendingDelete = null
        })
    }
}

@Composable
private fun ProjectCard(
    project: ProjectSummary,
    onOpen: () -> Unit,
    onFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    Column(
        Modifier
            .clip(RoundedCornerShape(shapes.lg))
            .background(colors.surface)
            .border(1.dp, colors.border, RoundedCornerShape(shapes.lg))
            .clickable(onClick = onOpen)
            .padding(Mona.spacing.sm),
    ) {
        Box {
            ProjectThumbnail(project, Modifier.fillMaxWidth().aspectRatio(1.15f))
            Row(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(CircleShape)
                    .background(colors.ground.copy(alpha = 0.65f)),
            ) {
                Icon(
                    if (project.favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                    contentDescription = if (project.favorite) "Remove favourite" else "Add favourite",
                    tint = if (project.favorite) colors.accent else colors.textSecondary,
                    modifier = Modifier.size(26.dp).padding(4.dp).clickable(onClick = onFavorite),
                )
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete project",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(26.dp).padding(4.dp).clickable(onClick = onDelete),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            project.name,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${project.width}×${project.height} · ${project.layerCount} layers",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            maxLines = 1,
        )
        Text(
            relativeTime(project.modifiedAt),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            maxLines = 1,
        )
    }
}

@Composable
private fun ConfirmDelete(project: ProjectSummary, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val colors = Mona.colors
    Box(
        Modifier
            .fillMaxSize()
            .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
    )
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BentoCard(Modifier.width(380.dp)) {
            Text("Move to trash?", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                "“${project.name}” will be removed from the gallery. Files stay on disk until storage is cleaned up.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonaButton("Move to trash", onConfirm)
                MonaButton("Cancel", onDismiss, tonal = true)
            }
        }
    }
}
