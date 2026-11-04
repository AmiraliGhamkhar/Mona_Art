package com.monaart.studio.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monaart.engine.brush.BrushSpec
import com.monaart.studio.app.MonaArtApp
import com.monaart.studio.data.repository.BrushLibraryRepository
import com.monaart.studio.data.repository.ProjectRepository
import com.monaart.studio.data.settings.SettingsStore
import com.monaart.studio.domain.model.CanvasPreset
import com.monaart.studio.domain.model.CanvasSpec
import com.monaart.studio.domain.model.CornerStyle
import com.monaart.studio.domain.model.Density
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import com.monaart.studio.domain.model.ProjectFilter
import com.monaart.studio.domain.model.ProjectSort
import com.monaart.studio.domain.model.ProjectSummary
import com.monaart.studio.domain.model.ThemeMode
import com.monaart.studio.domain.model.UiSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Root ViewModel for the application shell: owns settings, the gallery query,
 * brush library and the active editing session. Screens are stateless and read
 * from here.
 */
class MonaViewModel(
    private val settingsStore: SettingsStore,
    val projects: ProjectRepository,
    val brushes: BrushLibraryRepository,
) : AndroidViewModel(Application()) {

    val settings: StateFlow<UiSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UiSettings())

    private val _projects = MutableStateFlow<List<ProjectSummary>>(emptyList())
    val galleryProjects: StateFlow<List<ProjectSummary>> = _projects.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _sort = MutableStateFlow(ProjectSort.RecentlyEdited)
    val sort: StateFlow<ProjectSort> = _sort.asStateFlow()

    private val _filter = MutableStateFlow(ProjectFilter.All)
    val filter: StateFlow<ProjectFilter> = _filter.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _storageBytes = MutableStateFlow(0L)
    val storageBytes: StateFlow<Long> = _storageBytes.asStateFlow()

    /** Filtered + sorted list for the gallery. */
    val visibleProjects: StateFlow<List<ProjectSummary>> =
        combine(_projects, _query, _sort, _filter, _favoritesOnly) { all, q, sortBy, filterBy, favOnly ->
            var list = all
            if (q.isNotBlank()) {
                list = list.filter { it.name.contains(q, ignoreCase = true) }
            }
            if (favOnly) list = list.filter { it.favorite }
            list = when (filterBy) {
                ProjectFilter.All -> list
                ProjectFilter.Favorites -> list.filter { it.favorite }
                ProjectFilter.Recent -> list.filter {
                    System.currentTimeMillis() - it.modifiedAt < 7L * 24 * 3600 * 1000
                }
                ProjectFilter.Large -> list.filter { it.width.toLong() * it.height >= 4_000_000L }
            }
            when (sortBy) {
                ProjectSort.RecentlyEdited -> list.sortedByDescending { it.modifiedAt }
                ProjectSort.RecentlyCreated -> list.sortedByDescending { it.createdAt }
                ProjectSort.Name -> list.sortedBy { it.name.lowercase() }
                ProjectSort.Size -> list.sortedByDescending { it.width.toLong() * it.height }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val app: MonaArtApp get() = MonaArtApp.instance

    init {
        viewModelScope.launch {
            projects.summaries.collect { list ->
                _projects.value = list
                _loading.value = false
            }
        }
        viewModelScope.launch { _storageBytes.value = projects.storageBytes() }
        viewModelScope.launch { brushes.ensureBuiltIns() }
    }

    // ---------------- Settings ----------------

    fun updateSettings(transform: (UiSettings) -> UiSettings) {
        viewModelScope.launch { settingsStore.update(transform) }
    }

    fun setTheme(mode: ThemeMode) = updateSettings { it.copy(themeMode = mode) }
    fun setAccent(argb: Int) = updateSettings { it.copy(accentArgb = argb) }
    fun setCornerStyle(style: CornerStyle) = updateSettings { it.copy(cornerStyle = style) }
    fun setDensity(density: Density) = updateSettings { it.copy(density = density) }
    fun setFontScale(scale: Float) = updateSettings { it.copy(fontScale = scale.coerceIn(0.85f, 1.35f)) }
    fun setReduceMotion(value: Boolean) = updateSettings { it.copy(reduceMotion = value) }
    fun setHaptics(value: Boolean) = updateSettings { it.copy(haptics = value) }
    fun setInputMode(mode: InputMode) = updateSettings { it.copy(inputMode = mode) }
    fun setPalmRejection(value: Boolean) = updateSettings { it.copy(palmRejection = value) }
    fun setStylusButton(action: GestureAction) = updateSettings { it.copy(stylusButtonAction = action) }
    fun setTwoFinger(action: GestureAction) = updateSettings { it.copy(twoFingerTap = action) }
    fun setThreeFinger(action: GestureAction) = updateSettings { it.copy(threeFingerTap = action) }
    fun setStabilizer(value: Float) = updateSettings { it.copy(stabilizerPreset = value.coerceIn(0f, 0.95f)) }
    fun setAutosave(seconds: Int) = updateSettings { it.copy(autosaveSeconds = seconds.coerceIn(5, 600)) }
    fun setHistorySteps(steps: Int) = updateSettings { it.copy(maxHistorySteps = steps.coerceIn(10, 400)) }
    fun setThumbnailQuality(px: Int) = updateSettings { it.copy(thumbnailQuality = px) }

    // ---------------- Gallery ----------------

    fun setQuery(q: String) { _query.value = q }
    fun setSort(s: ProjectSort) { _sort.value = s }
    fun setFilter(f: ProjectFilter) { _filter.value = f }
    fun toggleFavoritesOnly() { _favoritesOnly.value = !_favoritesOnly.value }

    fun toggleFavorite(project: ProjectSummary) {
        viewModelScope.launch { projects.setFavorite(project.id, !project.favorite) }
    }

    fun trash(project: ProjectSummary) {
        viewModelScope.launch {
            projects.trash(project.id)
            _storageBytes.value = projects.storageBytes()
        }
    }

    fun refreshStorage() {
        viewModelScope.launch { _storageBytes.value = projects.storageBytes() }
    }

    // ---------------- Documents ----------------

    suspend fun createDocument(spec: CanvasSpec): com.monaart.engine.document.ArtDocument =
        projects.create(spec)

    fun customPresets(): StateFlow<List<CanvasPreset>> = customPresetsState
    private val customPresetsState = MutableStateFlow<List<CanvasPreset>>(emptyList())

    fun saveCanvasPreset(preset: CanvasPreset) {
        viewModelScope.launch {
            app.database.projectDao().upsertPreset(
                com.monaart.studio.data.db.CanvasPresetEntity(
                    id = preset.id, name = preset.name, width = preset.width, height = preset.height,
                    dpi = preset.dpi, transparent = preset.transparent,
                    backgroundArgb = preset.backgroundArgb, createdAt = System.currentTimeMillis(),
                ),
            )
            customPresetsState.value = customPresetsState.value + preset
        }
    }

    fun deleteCanvasPreset(id: String) {
        viewModelScope.launch {
            app.database.projectDao().deletePreset(id)
            customPresetsState.value = customPresetsState.value.filterNot { it.id == id }
        }
    }

    // ---------------- Brushes ----------------

    fun saveBrush(spec: BrushSpec, onSaved: (BrushSpec) -> Unit = {}) {
        viewModelScope.launch { onSaved(brushes.save(spec)) }
    }

    fun deleteBrush(id: String) {
        viewModelScope.launch { brushes.delete(id) }
    }

    fun toggleBrushFavorite(id: String) {
        viewModelScope.launch { brushes.toggleFavorite(id) }
    }
}
