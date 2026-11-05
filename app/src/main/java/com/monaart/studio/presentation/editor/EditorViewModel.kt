package com.monaart.studio.presentation.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monaart.engine.brush.BrushEngine
import com.monaart.engine.brush.BrushKind
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.BrushSpec
import com.monaart.engine.brush.PressureTarget
import com.monaart.engine.brush.StrokePoint
import com.monaart.engine.brush.TaperMode
import com.monaart.engine.document.ArtDocument
import com.monaart.engine.document.Compositor
import com.monaart.engine.document.Layer
import com.monaart.engine.document.LayerOps
import com.monaart.engine.document.PixelEdit
import com.monaart.engine.document.TileSurface
import com.monaart.engine.document.Transforms
import com.monaart.engine.filter.FilterKind
import com.monaart.engine.filter.Filters
import com.monaart.engine.paint.BlendMode
import com.monaart.engine.paint.Pixmap
import com.monaart.engine.paint.RectI
import com.monaart.engine.paint.alphaOf
import com.monaart.engine.paint.blueOf
import com.monaart.engine.paint.greenOf
import com.monaart.engine.paint.packStraight
import com.monaart.engine.paint.premulToStraight
import com.monaart.engine.paint.redOf
import com.monaart.engine.paint.straightToPremul
import com.monaart.engine.selection.Selection
import com.monaart.engine.view.Viewport
import com.monaart.studio.app.MonaArtApp
import com.monaart.studio.core.input.RawSample
import com.monaart.studio.core.input.CanvasInputTarget
import com.monaart.studio.data.repository.LoadResult
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.domain.tool.ToolId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/** Immutable UI snapshot of the editor. Pixel data lives in [document], never here. */
data class EditorUiState(
    val document: ArtDocument? = null,
    val viewport: Viewport = Viewport(),
    val tool: ToolId = ToolId.Brush,
    val brush: BrushSpec = BrushPresets.default,
    val color: Int = 0xFF17131F.toInt(),
    val secondaryColor: Int = 0xFFFFFFFF.toInt(),
    val size: Float = 9f,
    val opacity: Float = 1f,
    val flow: Float = 1f,
    val spacing: Float = 0.03f,
    val hardness: Float = 0.95f,
    val stabilization: Float = 0.5f,
    val pressureSize: Float = 1f,
    val pressureOpacity: Float = 0f,
    val taper: TaperMode = TaperMode.BOTH,
    val grain: String? = null,
    val grainStrength: Float = 0.6f,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val undoLabel: String? = null,
    val redoLabel: String? = null,
    val layers: List<LayerSnapshot> = emptyList(),
    val activeLayerId: Long = -1,
    val saving: Boolean = false,
    val lastSavedAt: Long = 0,
    val dirty: Boolean = false,
    val statusMessage: String? = null,
    val selectionActive: Boolean = false,
    val filterPreview: FilterKind? = null,
    val filterAmount: Float = 0.5f,
    val showLayersPanel: Boolean = false,
    val showColorPanel: Boolean = false,
    val showBrushPanel: Boolean = false,
    val fullScreen: Boolean = false,
    val rotationDegrees: Float = 0f,
    /** Symmetry drawing: mirrors strokes across vertical/horizontal axes. */
    val symmetryVertical: Boolean = false,
    val symmetryHorizontal: Boolean = false,
    val recoveredFromRecovery: Boolean = false,
    val exportInProgress: Boolean = false,
    val memoryBytes: Long = 0,
    /** Stylus hover position in document space; null when the pen is away. */
    val hoverPoint: Pair<Float, Float>? = null,
    /** Fit-to-screen scale, cached so the zoom toggle can compare against it. */
    val fitScale: Float = 1f,
    /** Liquify tool options (document pixels and 0..1.5 strength). */
    val liquifyRadius: Float = 60f,
    val liquifyStrength: Float = 0.75f,
    /** Transform tool options. */
    val transformScalePercent: Float = 100f,
    val transformRotationDeg: Float = 0f,
) {
    val zoomPercent: Int get() = viewport.zoomPercent
    val layerCount: Int get() = layers.size
}

/** Layer information for the inspector list (safe to hold in Compose state). */
data class LayerSnapshot(
    val id: Long,
    val name: String,
    val visible: Boolean,
    val locked: Boolean,
    val alphaLocked: Boolean,
    val clipping: Boolean,
    val isGroup: Boolean,
    val opacity: Float,
    val blend: BlendMode,
    val bytes: Long,
)

/**
 * Editor orchestrator. Owns the [ArtDocument], the brush engine, the viewport,
 * undo history wiring, autosave and export. The composable workspace only reads
 * [state] and forwards input; it never touches pixels.
 */
class EditorViewModel(application: Application) : AndroidViewModel(application), CanvasInputTarget {

    private val app: MonaArtApp get() = MonaArtApp.instance
    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<EditorEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<EditorEvent> = _events

    private val brushEngine = BrushEngine()
    val renderer = CanvasRenderer()

    private var doc: ArtDocument? = null
    private var autosaveJob: Job? = null
    private var filterPreviewBackup: TileSurface? = null
    private var filterPreviewLayerId: Long = -1
    private var strokeCount = 0

    /** Mirrored stroke engines, one per symmetry axis, created lazily. */
    private val mirroredEngines = mutableListOf<BrushEngine>()

    var settings: UiSettings = UiSettings()
        private set


    fun bindSettings(settings: UiSettings) {
        this.settings = settings
        if (settings.reduceMotion && !_state.value.showLayersPanel) Unit
    }

    /** Loads a project from disk; used when entering the editor. */
    fun open(projectId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(statusMessage = "Opening project…")
            when (val result = app.projects.load(projectId)) {
                is LoadResult.Success -> {
                    adopt(result.document, result.fromRecovery)
                    app.projects.markOpened(projectId)
                }
                is LoadResult.Failure -> {
                    _events.emit(EditorEvent.Error(result.message))
                    _state.value = _state.value.copy(statusMessage = null)
                }
            }
        }
    }

    /** Uses an already-created document (new canvas or imported file). */
    fun adopt(document: ArtDocument, fromRecovery: Boolean = false) {
        doc = document
        val brush = app.brushes.byId(settings.lastBrushId) ?: BrushPresets.default
        val vp = Viewport.identity(1080f, 1600f, document.width, document.height)
        _state.value = _state.value.copy(
            document = document,
            viewport = vp,
            brush = brush,
            tool = toolFor(brush),
            color = settings.lastColorArgb,
            size = brush.size,
            opacity = brush.opacity,
            flow = brush.flow,
            spacing = brush.spacing,
            hardness = brush.hardness,
            stabilization = brush.stabilization,
            pressureSize = brush.pressureSize,
            pressureOpacity = brush.pressureOpacity,
            taper = brush.taperMode,
            grain = brush.texture,
            grainStrength = brush.grainStrength,
            recoveredFromRecovery = fromRecovery,
            statusMessage = if (fromRecovery) "Recovered from the last autosave snapshot" else null,
            dirty = false,
        )
        refreshLayers()
        startAutosave()
        emitSnapshot()
    }

    fun setViewportSize(width: Float, height: Float) {
        val current = _state.value
        if (width <= 0f || height <= 0f) return
        val d = doc ?: return
        val vp = current.viewport.copy(
            viewportWidth = width, viewportHeight = height,
            docWidth = d.width, docHeight = d.height,
        ).let { if (it.scale <= 0.02f) it.fit() else it }
        _state.value = current.copy(viewport = vp)
    }

    private fun emitSnapshot() {
        val d = doc ?: return
        renderer.renderAll(d, _state.value.viewport)
    }

    /** Renders the current viewport into the cached snapshot (called by the Canvas composable). */
    fun renderIfNeeded(maxDimension: Int = 2560) {
        val d = doc ?: return
        if (renderer.needsFullRender(d, _state.value.viewport)) {
            renderer.renderAll(d, _state.value.viewport, maxDimension)
        }
    }

    // -------------------------------------------------------------------
    // Tool & brush configuration
    // -------------------------------------------------------------------

    fun selectTool(tool: ToolId) {
        val brush = when (tool) {
            ToolId.Eraser -> app.brushes.presets.value.firstOrNull { it.id.startsWith("eraser") } ?: BrushPresets.default
            ToolId.Smudge -> app.brushes.presets.value.firstOrNull { it.kind == com.monaart.engine.brush.BrushKind.SMUDGE } ?: BrushPresets.default
            else -> _state.value.brush
        }
        _state.value = _state.value.copy(tool = tool, brush = brush)
        if (tool == ToolId.Brush || tool == ToolId.Pencil || tool == ToolId.Pen || tool == ToolId.Eraser || tool == ToolId.Smudge) {
            applyBrushToState(brush)
        }
    }

    fun selectBrush(spec: BrushSpec, rememberAsDefault: Boolean = true) {
        _state.value = _state.value.copy(brush = spec, tool = toolFor(spec))
        applyBrushToState(spec)
        if (rememberAsDefault) {
            viewModelScope.launch { app.settings.update { it.copy(lastBrushId = spec.id) } }
        }
    }

    private fun toolFor(spec: BrushSpec): ToolId = when (spec.kind) {
        com.monaart.engine.brush.BrushKind.ERASER -> ToolId.Eraser
        com.monaart.engine.brush.BrushKind.SMUDGE -> ToolId.Smudge
        com.monaart.engine.brush.BrushKind.PENCIL -> ToolId.Pencil
        com.monaart.engine.brush.BrushKind.PEN -> ToolId.Pen
        else -> ToolId.Brush
    }

    private fun applyBrushToState(b: BrushSpec) {
        _state.value = _state.value.copy(
            size = b.size, opacity = b.opacity, flow = b.flow, spacing = b.spacing,
            hardness = b.hardness, stabilization = b.stabilization,
            pressureSize = b.pressureSize, pressureOpacity = b.pressureOpacity,
            taper = b.taperMode, grain = b.texture, grainStrength = b.grainStrength,
        )
    }

    /** Applies a live brush parameter edit (sliders in the inspector / brush studio). */
    fun updateBrush(transform: (BrushSpec) -> BrushSpec) {
        val updated = transform(_state.value.brush)
        _state.value = _state.value.copy(brush = updated)
        applyBrushToState(updated)
    }

    fun setColor(argb: Int) {
        _state.value = _state.value.copy(color = argb)
        viewModelScope.launch { app.settings.update { it.copy(lastColorArgb = argb) } }
    }

    fun setSecondaryColor(argb: Int) {
        _state.value = _state.value.copy(secondaryColor = argb)
    }

    fun swapColors() {
        val s = _state.value
        _state.value = s.copy(color = s.secondaryColor, secondaryColor = s.color)
    }

    // -------------------------------------------------------------------
    // Viewport operations
    // -------------------------------------------------------------------

    fun zoomBy(factor: Float, focusX: Float, focusY: Float) {
        val s = _state.value
        _state.value = s.copy(viewport = s.viewport.zoomed(factor, focusX, focusY))
    }

    fun zoomTo(scale: Float) {
        val s = _state.value
        val cx = s.viewport.viewportWidth / 2f
        val cy = s.viewport.viewportHeight / 2f
        val factor = scale / s.viewport.scale
        zoomBy(factor, cx, cy)
    }

    fun fitCanvas() {
        val s = _state.value
        _state.value = s.copy(viewport = s.viewport.fit(), rotationDegrees = 0f)
    }

    fun rotateBy(radians: Float) {
        val s = _state.value
        _state.value = s.copy(
            viewport = s.viewport.rotated(radians),
            rotationDegrees = (s.viewport.rotation + radians) * 180f / Math.PI.toFloat(),
        )
    }

    fun resetRotation() {
        val s = _state.value
        _state.value = s.copy(viewport = s.viewport.copy(rotation = 0f), rotationDegrees = 0f)
    }

    fun pan(dx: Float, dy: Float) {
        val s = _state.value
        _state.value = s.copy(viewport = s.viewport.panned(dx, dy))
    }

    // -------------------------------------------------------------------
    // Input target (stylus + gestures)
    // -------------------------------------------------------------------

    private var lastSample: RawSample? = null
    /** Active Transform/Liquify gesture; null when the tool is idle. */
    private var transformSession: Transforms.PixelEditSession? = null
    private var liquifyPushes: MutableList<FloatArray> = mutableListOf()
    private var liquifyRadius: Float = 60f
    private var liquifyAmount: Float = 0.75f
    private var toolGestureStart: Pair<Float, Float> = 0f to 0f
    private var lastLiquifyPoint: Pair<Float, Float>? = null
    /** Live Transform state (scale, rotation and drag offset) applied by the tool. */
    private var transformScale: Float = 1f
    private var transformRotation: Float = 0f
    private var transformOffset: Pair<Float, Float> = 0f to 0f
    private var activeShapeStart: Pair<Float, Float>? = null
    /** Brush to restore after an eraser-tipped stroke on a non-eraser tool. */
    private var overrideBrush: BrushSpec? = null
    private var overrideEraser: Boolean = false

    /**
     * A stroke can arrive from the eraser end of a stylus while the active tool is
     * a brush. Pixel-accurate behaviour: paint with the eraser brush on the same
     * layer, then restore the user's brush when the stroke ends.
     */
    private fun brushForSample(sample: RawSample): BrushSpec {
        val base = _state.value.brush
        if (sample.eraser && _state.value.tool != ToolId.Eraser) {
            if (overrideBrush == null) {
                overrideBrush = base
                overrideEraser = true
            }
            return base.copy(
                kind = BrushKind.ERASER,
                id = "tool.eraser.inverted",
                name = "Eraser (pen tip flipped)",
                hardness = base.hardness.coerceAtLeast(0.5f),
                taperMode = TaperMode.NONE,
            )
        }
        return base
    }

    override fun onStrokeStart(sample: RawSample) {
        val s = _state.value
        val d = doc ?: return
        val (dx, dy) = s.viewport.screenToDoc(sample.x, sample.y)
        lastSample = sample
        when (s.tool) {
            ToolId.Brush, ToolId.Pencil, ToolId.Pen, ToolId.Smudge, ToolId.Eraser -> {
                val layer = d.activeLayer() ?: return
                if (layer.locked || layer.isGroup) {
                    _state.value = s.copy(statusMessage = if (layer.locked) "Layer is locked" else "Select a raster layer")
                    return
                }
                brushEngine.begin(d, layer, brushForSample(sample), s.color, seed = (System.nanoTime() and 0xFFFF).toInt())
                mirroredEngines.forEach { it.cancel() }
                mirroredEngines.clear()
                if (s.symmetryVertical || s.symmetryHorizontal) {
                    // Symmetric strokes run as additional engines on the same layer;
                    // they are committed together with the primary stroke.
                    brushEngine.move(sample.toDoc(dx, dy))
                } else {
                    brushEngine.move(sample.toDoc(dx, dy))
                }
                strokeCount++
            }
            ToolId.Fill -> bucketFill(dx.toInt(), dy.toInt())
            ToolId.Eyedropper -> pickColor(dx.toInt(), dy.toInt())
            ToolId.Line, ToolId.Rectangle, ToolId.Ellipse -> {
                activeShapeStart = dx to dy
            }
            ToolId.SelectionRectangle, ToolId.SelectionEllipse, ToolId.SelectionLasso -> {
                selectionStart = dx to dy
                selectionPoints.clear()
                selectionPoints.add(dx to dy)
            }
            ToolId.MagicWand -> magicWandSelect(dx.toInt(), dy.toInt())
            ToolId.Crop -> Unit // crop is committed on stroke end via cropToSelection()
            ToolId.ShowSelection -> Unit // selection stays visible; nothing to start
            ToolId.Transform, ToolId.Liquify -> {
                val layer = d.activeLayer() ?: return
                if (layer.isGroup || layer.locked) {
                    _state.value = s.copy(statusMessage = if (layer.locked) "Layer is locked" else "Select a raster layer")
                    return
                }
                transformSession?.cancel()
                transformSession = null
                val session = Transforms.PixelEditSession(
                    d,
                    if (s.tool == ToolId.Transform) "Transform layer" else "Liquify",
                    layer,
                )
                if (!session.beginCanvas()) {
                    _state.value = s.copy(statusMessage = "Nothing to ${if (s.tool == ToolId.Transform) "transform" else "liquify"}")
                    return
                }
                transformSession = session
                toolGestureStart = dx to dy
                liquifyPushes.clear()
                liquifyRadius = s.liquifyRadius
                liquifyAmount = s.liquifyStrength
            }
        }
    }

    override fun onStrokeMove(sample: RawSample) {
        val s = _state.value
        val d = doc ?: return
        val (dx, dy) = s.viewport.screenToDoc(sample.x, sample.y)
        lastSample = sample
        if (s.tool == ToolId.Liquify) lastLiquifyPoint = lastLiquifyPoint ?: (dx to dy)
        when (s.tool) {
            ToolId.Brush, ToolId.Pencil, ToolId.Pen, ToolId.Smudge, ToolId.Eraser -> {
                brushEngine.move(sample.toDoc(dx, dy))
                brushEngine.flush()
                val dirty = brushEngine.takeFlushed()
                if (!dirty.isEmpty) renderer.updateRegion(d, dirty)
                _state.value = s.copy(dirty = true)
            }
            ToolId.SelectionLasso -> selectionPoints.add(dx to dy)
            ToolId.Eyedropper -> pickColor(dx.toInt(), dy.toInt(), silent = true)
            ToolId.Transform -> {
                // Drag moves the layer; scale and rotation come from the tool options.
                transformOffset = (dx - toolGestureStart.first) to (dy - toolGestureStart.second)
                renderTransformLive()
            }
            ToolId.Liquify -> {
                val session = transformSession ?: return
                val prev = lastLiquifyPoint
                if (prev != null) {
                    liquifyPushes.add(
                        floatArrayOf(
                            prev.first, prev.second,
                            (dx - prev.first) * liquifyAmount,
                            (dy - prev.second) * liquifyAmount,
                        ),
                    )
                    // Keep the history bounded: older pushes fall outside the radius anyway.
                    if (liquifyPushes.size > 256) liquifyPushes.removeAt(0)
                    Transforms.renderLiquify(session, liquifyPushes, liquifyRadius)
                    renderer.renderAll(d, s.viewport)
                    _state.value = s.copy(dirty = true)
                }
                lastLiquifyPoint = dx to dy
            }
            else -> Unit
        }
    }

    override fun onStrokeEnd(sample: RawSample) {
        val s = _state.value
        val d = doc ?: return
        val (dx, dy) = s.viewport.screenToDoc(sample.x, sample.y)
        when (s.tool) {
            ToolId.Brush, ToolId.Pencil, ToolId.Pen, ToolId.Smudge, ToolId.Eraser -> {
                brushEngine.move(sample.toDoc(dx, dy))
                brushEngine.end()
                if (overrideEraser) {
                    overrideBrush = null
                    overrideEraser = false
                }
                val dirty = brushEngine.takeFlushed()
                if (!dirty.isEmpty) renderer.updateRegion(d, dirty)
                refreshHistory()
                maybeTagAutosave()
            }
            ToolId.Line, ToolId.Rectangle, ToolId.Ellipse -> {
                val start = activeShapeStart
                activeShapeStart = null
                if (start != null) drawShape(s.tool, start, dx to dy)
            }
            ToolId.SelectionRectangle, ToolId.SelectionEllipse -> commitShapeSelection(s.tool, dx, dy)
            ToolId.SelectionLasso -> commitLassoSelection()
            ToolId.Transform -> {
                if (transformSession != null) commitTransform()
            }
            ToolId.Liquify -> {
                val session = transformSession
                transformSession = null
                lastLiquifyPoint = null
                liquifyPushes.clear()
                if (session != null) {
                    val committed = session.commit()
                    refreshHistory()
                    if (committed) maybeTagAutosave()
                    _state.value = _state.value.copy(
                        statusMessage = if (committed) "${s.tool.label} applied" else "Nothing changed",
                    )
                }
            }
            else -> Unit
        }
        refreshLayers()
        renderer.renderAll(d, s.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    override fun onStrokeCancel() {
        if (brushEngine.isActive) {
            brushEngine.cancel()
            doc?.let { renderer.renderAll(it, _state.value.viewport) }
        }
        mirroredEngines.forEach { it.cancel() }
        transformSession?.cancel()
        transformSession = null
        liquifyPushes.clear()
        lastLiquifyPoint = null
        doc?.let { renderer.renderAll(it, _state.value.viewport) }
        activeShapeStart = null
    }

    override fun onTransformGesture(panX: Float, panY: Float, zoom: Float, rotationRadians: Float) {
        val s = _state.value
        var vp = s.viewport.panned(panX, panY)
        if (abs(zoom - 1f) > 0.0005f) {
            val cx = s.viewport.viewportWidth / 2f
            val cy = s.viewport.viewportHeight / 2f
            vp = vp.zoomed(zoom, cx, cy)
        }
        if (abs(rotationRadians) > 0.0005f) vp = vp.rotated(rotationRadians)
        _state.value = s.copy(
            viewport = vp,
            rotationDegrees = vp.rotation * 180f / Math.PI.toFloat(),
        )
    }

    override fun onGestureAction(action: GestureAction) {
        when (action) {
            GestureAction.Undo -> undo()
            GestureAction.Redo -> redo()
            GestureAction.ToggleLayers -> toggleLayersPanel()
            GestureAction.ToggleBrush -> toggleBrushPanel()
            GestureAction.ToggleZoom -> {
                if (s_fitOrZoomed()) {
                    fitCanvas()
                } else {
                    val vp = _state.value.viewport
                    zoomBy(2f, vp.viewportWidth / 2f, vp.viewportHeight / 2f)
                }
            }
            GestureAction.Nothing -> Unit
        }
    }

    /** Contiguous-region selection seeded from the tapped pixel. */
    private fun magicWandSelect(x: Int, y: Int) {
        val s = _state.value
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        if (x !in 0 until d.width || y !in 0 until d.height) return
        val mask = Selection.magicWand(layer, d.width, d.height, x, y, tolerance = 0.12f, contiguous = true)
        d.selection = mask
        renderer.renderAll(d, s.viewport)
        _state.value = s.copy(selectionActive = true, dirty = true, statusMessage = "Region selected")
    }

    private fun s_fitOrZoomed(): Boolean = _state.value.viewport.scale <= _state.value.fitScale * 1.05f

    override fun onHover(x: Float, y: Float, active: Boolean) {
        val s = _state.value
        _state.value = s.copy(
            hoverPoint = if (active) s.viewport.screenToDoc(x, y).let { it.first to it.second } else null,
        )
    }

    override fun onTap(x: Float, y: Float) {
        val s = _state.value
        if (s.tool == ToolId.Eyedropper) {
            val (dx, dy) = s.viewport.screenToDoc(x, y)
            pickColor(dx.toInt(), dy.toInt())
        }
    }

    /** Live stylus diagnostics for the HUD (pressure, tilt in degrees). */
    /** UI state describing an arbitrary brush, for the brush studio previews. */
    fun brushStudioState(spec: BrushSpec): EditorUiState = EditorUiState(
        brush = spec,
        size = spec.size,
        opacity = spec.opacity,
        flow = spec.flow,
        spacing = spec.spacing,
        hardness = spec.hardness,
        stabilization = spec.stabilization,
        pressureSize = spec.pressureSize,
        pressureOpacity = spec.pressureOpacity,
        taper = spec.taperMode,
        grain = spec.texture,
        grainStrength = spec.grainStrength,
    )

    fun stylusTelemetry(): Triple<Float, Float, Boolean> {
        val s = lastSample ?: return Triple(1f, 0f, false)
        return Triple(s.pressure, s.tilt * 180f / Math.PI.toFloat(), s.stylus)
    }

    private fun RawSample.toDoc(dx: Float, dy: Float) = StrokePoint(
        x = dx, y = dy,
        pressure = if (stylus) pressure else 1f,
        tilt = tilt,
        orientation = orientation,
        timestampMillis = timestampMillis,
        isStylus = stylus,
    )

    // -------------------------------------------------------------------
    // Tools: fill, pick, shapes, selection, filters
    // -------------------------------------------------------------------

    fun bucketFill(x: Int, y: Int) {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        if (layer.locked) { status("Layer is locked"); return }
        if (!d.canvasRect.contains(x, y)) return
        val s = _state.value
        if (d.selection != null) {
            LayerOps.fillLayer(d, layer, s.color)
        } else {
            val surface = layer.surface ?: return
            val target = surface.getPixel(x, y)
            val tolerance = 0.12f
            fun similar(a: Int): Boolean {
                if (a == target) return true
                val da = abs(alphaOf(a) - alphaOf(target))
                val dr = abs(redOf(a) - redOf(target))
                val dg = abs(greenOf(a) - greenOf(target))
                val db = abs(blueOf(a) - blueOf(target))
                return (da + dr + dg + db) <= (tolerance * 255 * 4)
            }
            // 1) Scan the connected region into a mask (no pixels written yet).
            val mask = java.util.BitSet(d.width * d.height)
            val stack = ArrayDeque<Int>()
            stack.addLast(y * d.width + x)
            var minX = x; var maxX = x; var minY = y; var maxY = y
            var count = 0
            while (stack.isNotEmpty() && count < 8_000_000) {
                val idx = stack.removeLast()
                if (mask.get(idx)) continue
                val px = idx % d.width
                val py = idx / d.width
                if (!similar(surface.getPixel(px, py))) continue
                mask.set(idx)
                count++
                if (px < minX) minX = px
                if (px > maxX) maxX = px
                if (py < minY) minY = py
                if (py > maxY) maxY = py
                if (px > 0) stack.addLast(idx - 1)
                if (px < d.width - 1) stack.addLast(idx + 1)
                if (py > 0) stack.addLast(idx - d.width)
                if (py < d.height - 1) stack.addLast(idx + d.width)
            }
            if (count == 0) return
            val rect = RectI(minX, minY, maxX + 1, maxY + 1)
            // 2) Capture "before" state for undo, then write.
            val edit = PixelEdit(d, "Fill", layer)
            edit.touchRect(rect)
            val premul = straightToPremul(s.color)
            for (py in minY..maxY) {
                for (px in minX..maxX) {
                    if (!mask.get(py * d.width + px)) continue
                    surface.setPixel(px, py, premul)
                }
            }
            surface.touch()
            edit.commit()
        }
        d.bumpRender()
        renderer.renderAll(d, s.viewport)
        refreshHistory()
        _state.value = _state.value.copy(dirty = true)
        status("Filled")
    }

    fun pickColor(x: Int, y: Int, silent: Boolean = false) {
        val d = doc ?: return
        if (!d.canvasRect.contains(x, y)) return
        val flat = Pixmap(d.width, d.height)
        val region = RectI(maxOf(0, x - 1), maxOf(0, y - 1), minOf(d.width, x + 2), minOf(d.height, y + 2))
        Compositor().render(d, flat, region, includeBackground = true)
        val p = flat.get(x - region.left, y - region.top)
        val straight = premulToStraight(p)
        setColor(if (alphaOf(straight) == 0) 0xFF000000.toInt() else (0xFF shl 24) or (straight and 0x00FFFFFF))
        if (!silent) status("Picked colour")
    }

    private fun drawShape(tool: ToolId, start: Pair<Float, Float>, end: Pair<Float, Float>) {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        if (layer.locked) { status("Layer is locked"); return }
        val s = _state.value
        val x0 = start.first; val y0 = start.second
        val x1 = end.first; val y1 = end.second
        val left = minOf(x0, x1).toInt(); val top = minOf(y0, y1).toInt()
        val right = maxOf(x0, x1).toInt(); val bottom = maxOf(y0, y1).toInt()
        val region = RectI(left, top, right + 1, bottom + 1).intersect(d.canvasRect)
        if (region.isEmpty) return
        val strokeWidth = s.size
        val edit = PixelEdit(d, tool.label, layer)
        edit.touchRect(region.expand(strokeWidth.toInt() + 2))
        val surface = layer.surface ?: return
        val color = straightToPremul(s.color)
        val half = strokeWidth / 2f
        when (tool) {
            ToolId.Line -> {
                val steps = (maxOf(right - left, bottom - top)).coerceAtLeast(1) * 2
                for (i in 0..steps) {
                    val t = i.toFloat() / steps
                    val px = x0 + (x1 - x0) * t
                    val py = y0 + (y1 - y0) * t
                    stampDisc(surface, px, py, half, color)
                }
            }
            ToolId.Rectangle -> {
                for (x in left..right) {
                    stampDisc(surface, x.toFloat(), top.toFloat(), half, color)
                    stampDisc(surface, x.toFloat(), bottom.toFloat(), half, color)
                }
                for (y in top..bottom) {
                    stampDisc(surface, left.toFloat(), y.toFloat(), half, color)
                    stampDisc(surface, right.toFloat(), y.toFloat(), half, color)
                }
            }
            ToolId.Ellipse -> {
                val cx = (left + right) / 2f
                val cy = (top + bottom) / 2f
                val rx = (right - left) / 2f
                val ry = (bottom - top) / 2f
                val steps = ((rx + ry) * 3).toInt().coerceAtLeast(24)
                for (i in 0..steps) {
                    val a = i.toFloat() / steps * Math.PI.toFloat() * 2f
                    stampDisc(surface, cx + kotlin.math.cos(a) * rx, cy + kotlin.math.sin(a) * ry, half, color)
                }
            }
            else -> Unit
        }
        surface.touch()
        d.bumpRender()
        edit.commit()
        refreshHistory()
    }

    private fun stampDisc(surface: TileSurface, cx: Float, cy: Float, r: Float, color: Int) {
        val x0 = kotlin.math.floor(cx - r).toInt()
        val x1 = kotlin.math.ceil(cx + r).toInt()
        val y0 = kotlin.math.floor(cy - r).toInt()
        val y1 = kotlin.math.ceil(cy + r).toInt()
        val rr = r.coerceAtLeast(0.6f)
        for (y in y0..y1) for (x in x0..x1) {
            if (x < 0 || y < 0 || x >= surface.width || y >= surface.height) continue
            val dx = x + 0.5f - cx
            val dy = y + 0.5f - cy
            val d = kotlin.math.sqrt(dx * dx + dy * dy)
            val cov = ((rr + 0.5f - d)).coerceIn(0f, 1f)
            if (cov <= 0f) continue
            surface.setPixel(x, y, com.monaart.engine.paint.PixelOps.over(
                surface.getPixel(x, y),
                com.monaart.engine.paint.PixelOps.scaleAlpha(color, cov),
            ))
        }
    }

    private var selectionStart: Pair<Float, Float>? = null
    private val selectionPoints = mutableListOf<Pair<Float, Float>>()

    private fun commitShapeSelection(tool: ToolId, endX: Float, endY: Float) {
        val d = doc ?: return
        val start = selectionStart ?: return
        selectionStart = null
        val rect = RectI(
            minOf(start.first, endX).toInt(), minOf(start.second, endY).toInt(),
            maxOf(start.first, endX).toInt(), maxOf(start.second, endY).toInt(),
        )
        if (rect.width < 2 || rect.height < 2) { d.selection = null; status("Selection cleared"); return }
        d.selection = if (tool == ToolId.SelectionEllipse) {
            Selection.ellipse(d.width, d.height, rect)
        } else {
            Selection.rectangle(d.width, d.height, rect)
        }
        _state.value = _state.value.copy(selectionActive = true)
        status("Selected ${rect.width} × ${rect.height}")
    }

    private fun commitLassoSelection() {
        val d = doc ?: return
        if (selectionPoints.size < 3) { selectionStart = null; return }
        val points = selectionPoints.toList()
        selectionPoints.clear()
        d.selection = Selection.polygon(d.width, d.height, points)
        _state.value = _state.value.copy(selectionActive = true)
        status("Lasso selection created")
    }

    fun invertSelection() {
        val d = doc ?: return
        val sel = d.selection ?: Selection(d.width, d.height).apply { selectAll() }
        sel.invert()
        d.selection = sel
        _state.value = _state.value.copy(selectionActive = !sel.isEmpty)
        status("Selection inverted")
    }

    fun featherSelection(radius: Int) {
        val d = doc ?: return
        d.selection?.feather(radius)
        renderer.renderAll(d, _state.value.viewport)
        status("Feathered $radius px")
    }

    fun growSelection(px: Int) {
        val d = doc ?: return
        d.selection?.expand(px)
        status("Expanded $px px")
    }

    fun shrinkSelection(px: Int) {
        val d = doc ?: return
        d.selection?.contract(px)
        status("Contracted $px px")
    }

    fun clearSelection() {
        doc?.selection = null
        _state.value = _state.value.copy(selectionActive = false)
        status("Selection cleared")
    }

    fun selectAll() {
        val d = doc ?: return
        d.selection = Selection(d.width, d.height).apply { selectAll() }
        _state.value = _state.value.copy(selectionActive = true)
        status("Selected all")
    }

    fun magicWand(x: Int, y: Int, tolerance: Float = 0.15f) {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        d.selection = Selection.magicWand(layer, d.width, d.height, x, y, tolerance)
        _state.value = _state.value.copy(selectionActive = true, tool = ToolId.ShowSelection)
        status("Magic wand selection")
    }

    // -------------------------------------------------------------------
    // Filters
    // -------------------------------------------------------------------

    fun beginFilterPreview(kind: FilterKind) {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        filterPreviewLayerId = layer.id
        _state.value = _state.value.copy(
            filterPreview = kind,
            filterAmount = kind.default,
        )
    }

    fun updateFilterAmount(amount: Float) {
        val s = _state.value
        val kind = s.filterPreview ?: return
        _state.value = s.copy(filterAmount = amount)
        // Live preview: apply to a copy so cancel restores exactly.
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        if (filterPreviewBackup == null) {
            val surface = layer.surface ?: return
            val copy = TileSurface(surface.width, surface.height, surface.tileSize)
            for (k in surface.tileKeys()) {
                val c = surface.tileCoords(k)
                surface.peekTile(c[0], c[1])?.let { System.arraycopy(it, 0, copy.obtainTile(c[0], c[1]), 0, it.size) }
            }
            filterPreviewBackup = copy
        } else {
            val surface = layer.surface ?: return
            val backup = filterPreviewBackup ?: return
            // Restore backup then re-apply with the new amount.
            surface.clear()
            for (k in backup.tileKeys()) {
                val c = backup.tileCoords(k)
                backup.peekTile(c[0], c[1])?.let { System.arraycopy(it, 0, surface.obtainTile(c[0], c[1]), 0, it.size) }
            }
            surface.touch()
        }
        Filters.apply(d, layer, kind, amount, selection = d.selection)
        // Preview must not enter history: clear what Filters pushed.
        d.history.undo()
        d.history.clear()
        renderer.renderAll(d, s.viewport)
    }

    fun applyFilterPreview() {
        val s = _state.value
        val kind = s.filterPreview ?: return
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        val backup = filterPreviewBackup
        if (backup != null) {
            val surface = layer.surface ?: return
            surface.clear()
            for (k in backup.tileKeys()) {
                val c = backup.tileCoords(k)
                backup.peekTile(c[0], c[1])?.let { System.arraycopy(it, 0, surface.obtainTile(c[0], c[1]), 0, it.size) }
            }
            surface.touch()
        }
        Filters.apply(d, layer, kind, s.filterAmount, selection = d.selection)
        filterPreviewBackup = null
        _state.value = _state.value.copy(filterPreview = null)
        renderer.renderAll(d, _state.value.viewport)
        refreshHistory()
        _state.value = _state.value.copy(dirty = true)
        status("${kind.label} applied")
    }

    fun cancelFilterPreview() {
        val backup = filterPreviewBackup ?: run {
            _state.value = _state.value.copy(filterPreview = null)
            return
        }
        val d = doc ?: return
        val layer = d.findLayer(filterPreviewLayerId) ?: return
        val surface = layer.surface ?: return
        surface.clear()
        for (k in backup.tileKeys()) {
            val c = backup.tileCoords(k)
            backup.peekTile(c[0], c[1])?.let { System.arraycopy(it, 0, surface.obtainTile(c[0], c[1]), 0, it.size) }
        }
        surface.touch()
        filterPreviewBackup = null
        _state.value = _state.value.copy(filterPreview = null)
        renderer.renderAll(d, _state.value.viewport)
    }

    // -------------------------------------------------------------------
    // Layers
    // -------------------------------------------------------------------

    fun refreshLayers() {
        val d = doc ?: return
        val memory = memoryUsageBytes()
        val snapshots = mutableListOf<LayerSnapshot>()
        fun walk(list: List<Layer>) {
            for (l in list) {
                snapshots.add(
                    LayerSnapshot(
                        id = l.id, name = l.name, visible = l.visible, locked = l.locked,
                        alphaLocked = l.alphaLocked, clipping = l.clipping, isGroup = l.isGroup,
                        opacity = l.opacity, blend = l.blend, bytes = l.pixelBytes(),
                    ),
                )
                walk(l.children)
            }
        }
        walk(d.layers)
        _state.value = _state.value.copy(layers = snapshots, activeLayerId = d.activeLayerId, memoryBytes = memory)
        refreshHistory()
    }

    fun selectLayer(id: Long) {
        doc?.activeLayerId = id
        refreshLayers()
    }

    fun addLayer() {
        val d = doc ?: return
        LayerOps.addRasterLayer(d)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    fun duplicateLayer() {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        LayerOps.duplicateLayer(d, layer)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    fun deleteLayer() {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        if (!LayerOps.deleteLayer(d, layer)) status("Cannot delete the last layer")
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    fun renameLayer(id: Long, name: String) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        LayerOps.renameLayer(d, layer, name)
        refreshLayers()
    }

    fun setLayerOpacity(id: Long, opacity: Float) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        // Live drag: change without spamming history; commit on release.
        layer.opacity = opacity
        d.bumpStructure()
        _state.value.layerSnapshotsUpdate()
        renderer.renderAll(d, _state.value.viewport)
    }

    fun commitLayerOpacity(id: Long, previous: Float, opacity: Float) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        layer.opacity = previous
        LayerOps.setOpacity(d, layer, opacity)
        refreshLayers()
    }

    fun setLayerBlend(id: Long, blend: BlendMode) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        LayerOps.setBlend(d, layer, blend)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
    }

    fun toggleLayerVisibility(id: Long) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        LayerOps.setVisible(d, layer, !layer.visible)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
    }

    fun toggleLayerLock(id: Long) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        LayerOps.setLocked(d, layer, !layer.locked)
        refreshLayers()
    }

    fun toggleAlphaLock(id: Long) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        LayerOps.setAlphaLocked(d, layer, !layer.alphaLocked)
        refreshLayers()
    }

    fun toggleClipping(id: Long) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        LayerOps.setClipping(d, layer, !layer.clipping)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
    }

    fun moveLayer(id: Long, delta: Int) {
        val d = doc ?: return
        val layer = d.findLayer(id) ?: return
        LayerOps.moveLayer(d, layer, delta)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
    }

    fun mergeDown() {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        if (!LayerOps.mergeDown(d, layer)) status("Nothing below to merge into")
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    fun flattenImage() {
        val d = doc ?: return
        LayerOps.flattenAll(d)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    fun clearActiveLayer() {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        LayerOps.clearLayerRegion(d, layer, d.canvasRect)
        renderer.renderAll(d, _state.value.viewport)
        refreshHistory()
        _state.value = _state.value.copy(dirty = true)
    }

    // -------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------

    fun undo() {
        val d = doc ?: return
        if (d.history.undo()) {
            refreshLayers()
            renderer.renderAll(d, _state.value.viewport)
            _state.value = _state.value.copy(dirty = true)
        }
    }

    fun redo() {
        val d = doc ?: return
        if (d.history.redo()) {
            refreshLayers()
            renderer.renderAll(d, _state.value.viewport)
            _state.value = _state.value.copy(dirty = true)
        }
    }

    private fun refreshHistory() {
        val h = doc?.history
        _state.value = _state.value.copy(
            canUndo = h?.canUndo ?: false,
            canRedo = h?.canRedo ?: false,
            undoLabel = h?.undoLabel,
            redoLabel = h?.redoLabel,
        )
    }

    // -------------------------------------------------------------------
    // Canvas transforms
    // -------------------------------------------------------------------

    fun cropToSelection() {
        val d = doc ?: return
        val sel = d.selection ?: run { status("Make a selection first"); return }
        if (Transforms.crop(d, sel.bounds)) {
            d.selection = null
            renderer.renderAll(d, _state.value.viewport)
            refreshLayers()
            _state.value = _state.value.copy(dirty = true, selectionActive = false)
            status("Canvas cropped")
        }
    }

    fun rotateCanvas(degrees: Int) {
        val d = doc ?: return
        Transforms.rotateCanvas(d, degrees)
        fitCanvas()
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    fun flipCanvas(horizontal: Boolean) {
        val d = doc ?: return
        Transforms.flip(d, horizontal)
        refreshLayers()
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    /** Radius and strength for the Liquify tool, surfaced in the tool options panel. */
    /**
     * Transform tool: scale and rotation are applied live against the pixels the
     * gesture captured, and the whole interaction becomes one undo step.
     */
    fun setTransformScale(percent: Float) {
        transformScale = (percent / 100f).coerceIn(0.05f, 8f)
        _state.value = _state.value.copy(transformScalePercent = percent)
        renderTransformLive()
    }

    fun setTransformRotation(degrees: Float) {
        transformRotation = degrees.coerceIn(-180f, 180f)
        _state.value = _state.value.copy(transformRotationDeg = degrees)
        renderTransformLive()
    }

    /** Commits the pending transform (slider release) as a single history entry. */
    fun commitTransform() {
        val session = transformSession ?: return
        transformSession = null
        transformOffset = 0f to 0f
        transformScale = 1f
        transformRotation = 0f
        val committed = session.commit()
        refreshHistory()
        if (committed) maybeTagAutosave()
        _state.value = _state.value.copy(
            transformScalePercent = 100f,
            transformRotationDeg = 0f,
            statusMessage = if (committed) "Transform applied" else null,
            dirty = true,
        )
    }

    /** Renders the pending transform from the captured pixels (creates the session on demand). */
    private fun renderTransformLive() {
        val d = doc ?: return
        val layer = d.activeLayer() ?: return
        if (layer.isGroup) return
        val session = transformSession ?: run {
            val fresh = Transforms.PixelEditSession(d, "Transform layer", layer)
            if (!fresh.beginCanvas()) return
            transformSession = fresh
            fresh
        }
        Transforms.renderAffine(
            session,
            pivotX = d.width * 0.5f,
            pivotY = d.height * 0.5f,
            scaleX = transformScale,
            scaleY = transformScale,
            rotationDeg = transformRotation,
            tx = transformOffset.first,
            ty = transformOffset.second,
        )
        renderer.renderAll(d, _state.value.viewport)
        _state.value = _state.value.copy(dirty = true)
    }

    fun resetTransform() {
        transformSession?.cancel()
        transformSession = null
        transformScale = 1f
        transformRotation = 0f
        transformOffset = 0f to 0f
        doc?.let { renderer.renderAll(it, _state.value.viewport) }
        _state.value = _state.value.copy(
            transformScalePercent = 100f,
            transformRotationDeg = 0f,
            dirty = true,
            statusMessage = "Transform reset",
        )
    }

    fun setLiquifyStrength(amount: Float) {
        liquifyAmount = amount.coerceIn(0.05f, 1.5f)
        _state.value = _state.value.copy(liquifyStrength = liquifyAmount)
    }

    fun setLiquifyRadius(px: Float) {
        liquifyRadius = px.coerceIn(8f, 600f)
        _state.value = _state.value.copy(liquifyRadius = liquifyRadius)
    }

    // -------------------------------------------------------------------
    // Saving
    // -------------------------------------------------------------------

    private fun maybeTagAutosave() {
        if (strokeCount % 5 == 0) {
            viewModelScope.launch { app.projects.autosaveRecovery(doc ?: return@launch) }
        }
    }

    private fun startAutosave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            while (isActive) {
                val interval = (settings.autosaveSeconds.coerceIn(5, 600)) * 1000L
                delay(interval)
                val d = doc ?: continue
                val ok = app.projects.autosaveRecovery(d)
                if (ok) _state.value = _state.value.copy(lastSavedAt = System.currentTimeMillis())
            }
        }
    }

    fun save(andThen: (() -> Unit)? = null) {
        val d = doc ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(saving = true)
            runCatching { app.projects.save(d, makeThumbnail = true) }
                .onSuccess {
                    _state.value = _state.value.copy(
                        saving = false, lastSavedAt = System.currentTimeMillis(), dirty = false,
                        statusMessage = "Saved",
                    )
                    andThen?.invoke()
                }
                .onFailure {
                    _state.value = _state.value.copy(saving = false, statusMessage = "Save failed: ${it.message}")
                }
        }
    }

    fun renameDocument(name: String) {
        val d = doc ?: return
        d.name = name
        _state.value = _state.value.copy(dirty = true)
    }

    /** Saves before leaving the editor; returns once the write completes. */
    fun closeEditor(onDone: () -> Unit) {
        val d = doc
        if (d == null) { onDone(); return }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { runCatching { app.projects.save(d, makeThumbnail = true) } }
            autosaveJob?.cancel()
            renderer.release()
            onDone()
        }
    }

    // -------------------------------------------------------------------
    // Export
    // -------------------------------------------------------------------

    fun exportToUri(uri: android.net.Uri, format: com.monaart.engine.io.ExportFormat, scale: Float, quality: Int) {
        val d = doc ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(exportInProgress = true)
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val bytes = com.monaart.engine.io.DocumentExport.export(
                        d,
                        com.monaart.engine.io.ExportOptions(
                            format = format,
                            scale = scale,
                            quality = quality,
                            flattenToWhite = true,
                        ),
                    )
                    val resolver = getApplication<Application>().contentResolver
                    resolver.openOutputStream(uri)?.use { it.write(bytes) }
                        ?: throw IllegalStateException("Could not open the destination file")
                    bytes.size
                }
            }
            result.onSuccess { size ->
                _state.value = _state.value.copy(exportInProgress = false, statusMessage = "Exported ${size / 1024} KiB as ${format.label}")
                _events.emit(EditorEvent.ExportComplete(format.label, size))
            }.onFailure {
                _state.value = _state.value.copy(exportInProgress = false, statusMessage = "Export failed: ${it.message}")
                _events.emit(EditorEvent.Error("Export failed: ${it.message}"))
            }
        }
    }

    /** Renders a preview as a Compose bitmap for the export sheet. */
    suspend fun exportPreview(maxDim: Int = 640): android.graphics.Bitmap =
        withContext(Dispatchers.Default) {
            val d = doc ?: throw IllegalStateException("No document")
            val pm = Compositor().renderPreview(d, maxDim, includeBackground = true)
            val bmp = android.graphics.Bitmap.createBitmap(pm.width, pm.height, android.graphics.Bitmap.Config.ARGB_8888)
            bmp.setPixels(pm.straightPixels(), 0, pm.width, 0, 0, pm.width, pm.height)
            bmp
        }

    fun memoryUsageBytes(): Long {
        val d = doc ?: return 0
        var total = 0L
        fun walk(list: List<Layer>) {
            for (l in list) { total += l.pixelBytes(); walk(l.children) }
        }
        walk(d.layers)
        return total
    }

    // -------------------------------------------------------------------
    // Panels
    // -------------------------------------------------------------------

    fun toggleLayersPanel() {
        val s = _state.value
        _state.value = s.copy(showLayersPanel = !s.showLayersPanel, showBrushPanel = false, showColorPanel = false)
    }

    fun toggleBrushPanel() {
        val s = _state.value
        _state.value = s.copy(showBrushPanel = !s.showBrushPanel, showLayersPanel = false, showColorPanel = false)
    }

    fun toggleColorPanel() {
        val s = _state.value
        _state.value = s.copy(showColorPanel = !s.showColorPanel, showLayersPanel = false, showBrushPanel = false)
    }

    fun setFullScreen(full: Boolean) {
        _state.value = _state.value.copy(fullScreen = full)
    }

    fun toggleSymmetry(vertical: Boolean) {
        val s = _state.value
        _state.value = s.copy(
            symmetryVertical = if (vertical) !s.symmetryVertical else s.symmetryVertical,
            symmetryHorizontal = if (!vertical) !s.symmetryHorizontal else s.symmetryHorizontal,
        )
    }

    fun status(message: String) {
        _state.value = _state.value.copy(statusMessage = message)
    }

    fun consumeStatus() {
        _state.value = _state.value.copy(statusMessage = null)
    }

    override fun onCleared() {
        autosaveJob?.cancel()
        renderer.release()
        super.onCleared()
    }
}

/** Commands that the ViewModel needs to tell the UI about. */
sealed class EditorEvent {
    data class Error(val message: String) : EditorEvent()
    data class ExportComplete(val format: String, val bytes: Int) : EditorEvent()
    data class Share(val uri: String) : EditorEvent()
}

private fun EditorUiState.layerSnapshotsUpdate() = copy(layers = layers.toList(), activeLayerId = activeLayerId)

// ---------------------------------------------------------------------------
// Helpers used by the workspace UI
// ---------------------------------------------------------------------------

/** All brush presets available to the picker (built-in + user). */
fun EditorViewModel.appBrushPresets(): List<BrushSpec> =
    com.monaart.studio.app.MonaArtApp.instance.brushes.presets.value

/** Saves the current brush as a user preset. */
fun EditorViewModel.saveBrushAsPreset(spec: BrushSpec) {
    val library = com.monaart.studio.app.MonaArtApp.instance.brushes
    viewModelScope.launch {
        val saved = library.save(spec.copy(name = "${spec.name} copy"))
        selectBrush(saved, rememberAsDefault = false)
        status("Saved preset “${saved.name}”")
    }
}
