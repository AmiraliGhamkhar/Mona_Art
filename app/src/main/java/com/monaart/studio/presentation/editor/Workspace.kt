package com.monaart.studio.presentation.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Flip
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.RotateLeft
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monaart.studio.core.design.Mona
import com.monaart.studio.core.design.MonaType
import com.monaart.studio.core.input.CanvasInputHandler
import com.monaart.studio.core.input.CanvasInputOverlay
import com.monaart.studio.core.input.sanitizeZoom
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.domain.tool.ToolId
import com.monaart.studio.domain.tool.icon
import com.monaart.studio.domain.tool.primaryRailTools
import com.monaart.studio.domain.tool.secondaryRailTools
import com.monaart.studio.ui.components.MonaIconButton
import com.monaart.studio.ui.components.MonaToolButton
import com.monaart.studio.presentation.common.formatBytesShort

/**
 * The drawing workspace: canvas centred, tool rail on the left, actions on top,
 * contextual inspector on the right. On narrow screens the inspector becomes a
 * sheet so the canvas keeps its area.
 */
@Composable
fun DrawingWorkspace(
    editor: EditorViewModel,
    state: EditorUiState,
    settings: UiSettings,
    onExit: () -> Unit,
    onExport: () -> Unit,
    onDocumentSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowLayers: () -> Unit,
) {
    val colors = Mona.colors
    BoxWithConstraints(Modifier.fillMaxSize().background(colors.canvasBackdrop)) {
        val wide = maxWidth >= 840.dp
        val showRail = settings.leftRailVisible && !state.fullScreen
        val showInspector = wide && settings.rightInspectorVisible && !state.fullScreen && !state.showLayersPanel

        Column(Modifier.fillMaxSize()) {
            if (!state.fullScreen) {
                TopBar(
                    state = state,
                    editor = editor,
                    onExit = onExit,
                    onExport = onExport,
                    onDocumentSettings = onDocumentSettings,
                    onOpenSettings = onOpenSettings,
                    onShowLayers = onShowLayers,
                )
            }
            Row(Modifier.weight(1f)) {
                if (showRail) {
                    ToolRail(state = state, onSelect = editor::selectTool, compact = !wide)
                }
                CanvasHost(
                    editor = editor,
                    state = state,
                    settings = settings,
                    modifier = Modifier.weight(1f),
                )
                if (showInspector) {
                    InspectorPanel(
                        editor = editor,
                        state = state,
                        modifier = Modifier.width(300.dp).fillMaxHeight(),
                    )
                }
            }
            if (settings.showQuickBrushBar && !state.fullScreen) {
                QuickBrushBar(editor = editor, state = state)
            }
        }

        if (state.fullScreen) {
            // Minimal floating exit affordance in full-screen drawing mode.
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(colors.surface.copy(alpha = 0.85f))
                    .clickable { editor.setFullScreen(false) }
                    .padding(12.dp),
            ) {
                Icon(Icons.Outlined.Close, contentDescription = "Exit full screen", tint = colors.textSecondary)
            }
        }

        // Panels (sheets) for narrow layouts and layer management
        if (state.showLayersPanel) {
            LayersSheet(editor = editor, state = state, onDismiss = editor::toggleLayersPanel, expanded = true)
        }
        if (state.showBrushPanel) {
            BrushSheet(editor = editor, state = state, onDismiss = editor::toggleBrushPanel)
        }
        if (state.showColorPanel) {
            ColorSheet(editor = editor, state = state, onDismiss = editor::toggleColorPanel)
        }
        if (state.filterPreview != null) {
            FilterDialog(editor = editor, state = state)
        }
    }
}

// ---------------------------------------------------------------------------
// Top bar
// ---------------------------------------------------------------------------

@Composable
private fun TopBar(
    state: EditorUiState,
    editor: EditorViewModel,
    onExit: () -> Unit,
    onExport: () -> Unit,
    onDocumentSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onShowLayers: () -> Unit,
) {
    val colors = Mona.colors
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonaIconButton(Icons.Outlined.ArrowBack, "Back to studio", onExit, tint = colors.textSecondary)
        Column(Modifier.weight(1f).padding(horizontal = 6.dp)) {
            Text(
                state.document?.name ?: "Untitled",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val status = when {
                state.saving -> "Saving…"
                state.dirty -> "Unsaved changes · autosaved"
                else -> "Saved · ${state.document?.width}×${state.document?.height}"
            }
            Text(status, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, maxLines = 1)
        }
        MonaIconButton(Icons.Outlined.Undo, state.undoLabel ?: "Undo", editor::undo, enabled = state.canUndo, tint = colors.textSecondary)
        MonaIconButton(Icons.Outlined.Redo, state.redoLabel ?: "Redo", editor::redo, enabled = state.canRedo, tint = colors.textSecondary)
        MonaIconButton(Icons.Outlined.Layers, "Layers", onShowLayers, tint = colors.textSecondary)
        MonaIconButton(Icons.Outlined.FileDownload, "Export", onExport, tint = colors.accent)
        Box {
            MonaIconButton(Icons.Outlined.MoreVert, "More", { menuOpen = !menuOpen }, tint = colors.textSecondary)
            if (menuOpen) {
                MoreMenu(
                    state = state,
                    editor = editor,
                    onDismiss = { menuOpen = false },
                    onDocumentSettings = onDocumentSettings,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
    }
}

@Composable
private fun MoreMenu(
    state: EditorUiState,
    editor: EditorViewModel,
    onDismiss: () -> Unit,
    onDocumentSettings: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val colors = Mona.colors
    val shapes = Mona.shapes
    Column(
        Modifier
            .padding(top = 56.dp, end = 8.dp)
            .width(250.dp)
            .clip(RoundedCornerShape(shapes.lg))
            .background(colors.surface2)
            .border(1.dp, colors.border, RoundedCornerShape(shapes.lg))
            .padding(6.dp),
    ) {
        MenuItem("Canvas & document", Icons.Outlined.Settings) { onDismiss(); onDocumentSettings() }
        MenuItem("Full screen", Icons.Outlined.Fullscreen) { onDismiss(); editor.setFullScreen(true) }
        MenuItem("Fit canvas", Icons.Outlined.CenterFocusStrong) { onDismiss(); editor.fitCanvas() }
        MenuItem("Rotate canvas 90°", Icons.Outlined.RotateLeft) { onDismiss(); editor.rotateCanvas(90) }
        MenuItem("Flip horizontal", Icons.Outlined.Flip) { onDismiss(); editor.flipCanvas(true) }
        MenuItem(if (state.symmetryVertical) "Symmetry: on" else "Symmetry: off", Icons.Outlined.Tune) {
            editor.toggleSymmetry(vertical = true)
        }
        MenuItem("Clear layer", Icons.Outlined.Close) { onDismiss(); editor.clearActiveLayer() }
        MenuItem("App settings", Icons.Outlined.Settings) { onDismiss(); onOpenSettings() }
    }
}

@Composable
private fun MenuItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val colors = Mona.colors
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Mona.shapes.sm))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
    }
}

// ---------------------------------------------------------------------------
// Tool rail
// ---------------------------------------------------------------------------

@Composable
private fun ToolRail(state: EditorUiState, onSelect: (ToolId) -> Unit, compact: Boolean) {
    val colors = Mona.colors
    var expanded by remember { mutableStateOf(false) }
    Column(
        Modifier
            .width(if (compact) 56.dp else 64.dp)
            .fillMaxHeight()
            .background(colors.surface)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        primaryRailTools.forEach { tool ->
            MonaToolButton(
                icon = tool.icon,
                label = tool.label,
                onClick = { onSelect(tool) },
                selected = state.tool == tool,
            )
        }
        MonaToolButton(
            icon = Icons.Outlined.MoreVert,
            label = if (expanded) "Hide more tools" else "More tools",
            onClick = { expanded = !expanded },
            selected = expanded,
        )
        if (expanded) {
            secondaryRailTools.forEach { tool ->
                MonaToolButton(
                    icon = tool.icon,
                    label = tool.label,
                    onClick = { onSelect(tool) },
                    selected = state.tool == tool,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        // Colour wells under the rail
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(state.color))
                .border(2.dp, colors.surface3, RoundedCornerShape(12.dp))
                .clickable { onSelect(ToolId.Fill) }
                .semantics { contentDescription = "Primary colour" },
        )
    }
}

// ---------------------------------------------------------------------------
// Canvas host
// ---------------------------------------------------------------------------

@Composable
private fun CanvasHost(
    editor: EditorViewModel,
    state: EditorUiState,
    settings: UiSettings,
    modifier: Modifier = Modifier,
) {
    val colors = Mona.colors
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }

    val inputHandler = remember(editor, settings) {
        CanvasInputHandler(
            target = editor,
            config = {
                CanvasInputHandler.InputConfig(
                    mode = settings.inputMode,
                    palmRejection = settings.palmRejection,
                    rotationEnabled = settings.canvasRotationEnabled,
                    doubleTapZoom = settings.doubleTapZoom,
                    twoFingerTap = settings.twoFingerTap,
                    threeFingerTap = settings.threeFingerTap,
                    stylusButtonAction = settings.stylusButtonAction,
                )
            },
        )
    }

    Box(
        modifier
            .fillMaxSize()
            .background(colors.canvasBackdrop)
            .onSizeChanged { size ->
                canvasSize = size
                if (size.width > 0 && size.height > 0) {
                    editor.setViewportSize(size.width.toFloat(), size.height.toFloat())
                    editor.renderIfNeeded(maxDimension = maxOf(size.width, size.height).coerceAtLeast(1024))
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Input sits above the bitmap but below overlays so HUD buttons stay tappable.
        CanvasInputOverlay(inputHandler, Modifier.fillMaxSize())
        val image = editor.renderer.currentImage
        val vp = state.viewport
        val doc = state.document

        if (image != null && doc != null) {
            val rect = editor.renderer.renderedRect
            val renderScale = editor.renderer.renderScale
            // Place the rendered visible-region snapshot on screen.
            val (screenX, screenY) = vp.docToScreen(rect.left.toFloat(), rect.top.toFloat())
            val displayW = rect.width * vp.scale
            val displayH = rect.height * vp.scale
            val rotationDeg = vp.rotation * 180f / Math.PI.toFloat()
            Box(
                Modifier
                    .graphicsLayer {
                        // Rotation around the viewport centre, matching Viewport math.
                        translationX = screenX - (canvasSize.width / 2f) + displayW / 2f
                        translationY = screenY - (canvasSize.height / 2f) + displayH / 2f
                        rotationZ = rotationDeg
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0.5f)
                    }
                    .width(with(density) { displayW.toDp() })
                    .height(with(density) { displayH.toDp() }),
            ) {
                androidx.compose.foundation.Image(
                    bitmap = image,
                    contentDescription = "Canvas",
                    contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            // Canvas frame + guides drawn in screen space.
            Canvas(Modifier.fillMaxSize()) {
                drawDocFrame(vp, doc.width, doc.height, colors.borderStrong)
                if (state.symmetryVertical) {
                    val (sx, sy) = vp.docToScreen(doc.width / 2f, doc.height / 2f)
                    drawLine(colors.accent.copy(alpha = 0.5f), Offset(sx, 0f), Offset(sx, size.height), 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f)))
                }
                if (state.symmetryHorizontal) {
                    val (sx, sy) = vp.docToScreen(doc.width / 2f, doc.height / 2f)
                    drawLine(colors.accent.copy(alpha = 0.5f), Offset(0f, sy), Offset(size.width, sy), 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 12f)))
                }
                state.document?.selection?.let { sel ->
                    val b = sel.bounds
                    if (!b.isEmpty) {
                        val (x0, y0) = vp.docToScreen(b.left.toFloat(), b.top.toFloat())
                        val (x1, y1) = vp.docToScreen(b.right.toFloat(), b.bottom.toFloat())
                        drawRect(
                            color = colors.selection,
                            topLeft = Offset(minOf(x0, x1), minOf(y0, y1)),
                            size = androidx.compose.ui.geometry.Size(kotlin.math.abs(x1 - x0), kotlin.math.abs(y1 - y0)),
                            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))),
                        )
                    }
                }
                // Live crop drag: the region that will end up as the new canvas.
                state.cropRect?.let { crop ->
                    val (x0, y0) = vp.docToScreen(crop.left.toFloat(), crop.top.toFloat())
                    val (x1, y1) = vp.docToScreen(crop.right.toFloat(), crop.bottom.toFloat())
                    drawRect(
                        color = colors.accent,
                        topLeft = Offset(minOf(x0, x1), minOf(y0, y1)),
                        size = androidx.compose.ui.geometry.Size(kotlin.math.abs(x1 - x0), kotlin.math.abs(y1 - y0)),
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                    )
                }
            }
        } else {
            Text("Rendering…", color = colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
        }

        // Zoom / rotation HUD
        Row(
            Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.surface.copy(alpha = 0.9f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ZoomOut, "Zoom out", tint = colors.textSecondary,
                modifier = Modifier.size(16.dp).clickable {
                    editor.zoomBy(0.8f, canvasSize.width / 2f, canvasSize.height / 2f)
                })
            Text(
                "${state.zoomPercent}%",
                style = MonaType.numeric,
                color = colors.textPrimary,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Icon(Icons.Outlined.ZoomIn, "Zoom in", tint = colors.textSecondary,
                modifier = Modifier.size(16.dp).clickable {
                    editor.zoomBy(1.25f, canvasSize.width / 2f, canvasSize.height / 2f)
                })
            Spacer(Modifier.width(10.dp))
            Text(
                "${state.rotationDegrees.toInt()}°",
                style = MonaType.numeric,
                color = colors.textTertiary,
                modifier = Modifier.clickable { editor.resetRotation() },
            )
            Spacer(Modifier.width(10.dp))
            Icon(Icons.Outlined.CenterFocusStrong, "Fit canvas", tint = colors.textSecondary,
                modifier = Modifier.size(16.dp).clickable { editor.fitCanvas() })
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDocFrame(
    vp: com.monaart.engine.view.Viewport,
    docW: Int,
    docH: Int,
    color: Color,
) {
    val corners = listOf(
        vp.docToScreen(0f, 0f),
        vp.docToScreen(docW.toFloat(), 0f),
        vp.docToScreen(docW.toFloat(), docH.toFloat()),
        vp.docToScreen(0f, docH.toFloat()),
    )
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(corners[0].first, corners[0].second)
        lineTo(corners[1].first, corners[1].second)
        lineTo(corners[2].first, corners[2].second)
        lineTo(corners[3].first, corners[3].second)
        close()
    }
    drawPath(path, color.copy(alpha = 0.7f), style = Stroke(width = 1.2f))
}
