package com.monaart.studio.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monaart.studio.domain.model.CornerStyle
import com.monaart.studio.domain.model.Density
import com.monaart.studio.domain.model.GestureAction
import com.monaart.studio.domain.model.InputMode
import com.monaart.studio.domain.model.ThemeMode
import com.monaart.studio.domain.model.UiSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mona_settings")

/**
 * Persists [UiSettings] with DataStore. Every setter is a targeted write so
 * changing one preference never rewrites the whole settings blob (and cannot
 * race with another screen's write).
 */
class SettingsStore(private val context: Context) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val accent = intPreferencesKey("accent")
        val corners = stringPreferencesKey("corners")
        val density = stringPreferencesKey("density")
        val fontScale = floatPreferencesKey("font_scale")
        val reduceMotion = booleanPreferencesKey("reduce_motion")
        val highContrast = booleanPreferencesKey("high_contrast")
        val haptics = booleanPreferencesKey("haptics")
        val tips = booleanPreferencesKey("tips")
        val inputMode = stringPreferencesKey("input_mode")
        val palm = booleanPreferencesKey("palm_rejection")
        val stylusButton = stringPreferencesKey("stylus_button")
        val twoFinger = stringPreferencesKey("two_finger")
        val threeFinger = stringPreferencesKey("three_finger")
        val pressureSmoothing = floatPreferencesKey("pressure_smoothing")
        val stabilizer = floatPreferencesKey("stabilizer")
        val doubleTapZoom = booleanPreferencesKey("double_tap_zoom")
        val rotation = booleanPreferencesKey("rotation")
        val rail = booleanPreferencesKey("rail")
        val inspector = booleanPreferencesKey("inspector")
        val inspectorTab = stringPreferencesKey("inspector_tab")
        val quickBar = booleanPreferencesKey("quick_bar")
        val autosave = intPreferencesKey("autosave")
        val historySteps = intPreferencesKey("history_steps")
        val backups = intPreferencesKey("backups")
        val thumbQuality = intPreferencesKey("thumb_quality")
        val lastBrush = stringPreferencesKey("last_brush")
        val lastColor = intPreferencesKey("last_color")
        val onboarding = booleanPreferencesKey("onboarding")
        val lastPreset = stringPreferencesKey("last_preset")
    }

    val settings: Flow<UiSettings> = context.dataStore.data.map { p -> fromPreferences(p) }

    private fun fromPreferences(p: Preferences): UiSettings {
        val d = UiSettings()
        return UiSettings(
            themeMode = p[Keys.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: d.themeMode,
            accentArgb = p[Keys.accent] ?: d.accentArgb,
            cornerStyle = p[Keys.corners]?.let { runCatching { CornerStyle.valueOf(it) }.getOrNull() } ?: d.cornerStyle,
            density = p[Keys.density]?.let { runCatching { Density.valueOf(it) }.getOrNull() } ?: d.density,
            fontScale = p[Keys.fontScale] ?: d.fontScale,
            reduceMotion = p[Keys.reduceMotion] ?: d.reduceMotion,
            highContrast = p[Keys.highContrast] ?: d.highContrast,
            haptics = p[Keys.haptics] ?: d.haptics,
            showOnboardingTips = p[Keys.tips] ?: d.showOnboardingTips,
            inputMode = p[Keys.inputMode]?.let { runCatching { InputMode.valueOf(it) }.getOrNull() } ?: d.inputMode,
            palmRejection = p[Keys.palm] ?: d.palmRejection,
            stylusButtonAction = p[Keys.stylusButton]?.let {
                runCatching {
                    GestureAction.valueOf(
                        it,
                    )
                }.getOrNull()
            } ?: d.stylusButtonAction,
            twoFingerTap = p[Keys.twoFinger]?.let {
                runCatching { GestureAction.valueOf(it) }.getOrNull()
            } ?: d.twoFingerTap,
            threeFingerTap = p[Keys.threeFinger]?.let {
                runCatching {
                    GestureAction.valueOf(
                        it,
                    )
                }.getOrNull()
            } ?: d.threeFingerTap,
            pressureSmoothing = p[Keys.pressureSmoothing] ?: d.pressureSmoothing,
            stabilizerPreset = p[Keys.stabilizer] ?: d.stabilizerPreset,
            doubleTapZoom = p[Keys.doubleTapZoom] ?: d.doubleTapZoom,
            canvasRotationEnabled = p[Keys.rotation] ?: d.canvasRotationEnabled,
            leftRailVisible = p[Keys.rail] ?: d.leftRailVisible,
            rightInspectorVisible = p[Keys.inspector] ?: d.rightInspectorVisible,
            inspectorTab = p[Keys.inspectorTab] ?: d.inspectorTab,
            showQuickBrushBar = p[Keys.quickBar] ?: d.showQuickBrushBar,
            autosaveSeconds = p[Keys.autosave] ?: d.autosaveSeconds,
            maxHistorySteps = p[Keys.historySteps] ?: d.maxHistorySteps,
            keepRecoveryBackups = p[Keys.backups] ?: d.keepRecoveryBackups,
            thumbnailQuality = p[Keys.thumbQuality] ?: d.thumbnailQuality,
            lastBrushId = p[Keys.lastBrush] ?: d.lastBrushId,
            lastColorArgb = p[Keys.lastColor] ?: d.lastColorArgb,
            onboardingComplete = p[Keys.onboarding] ?: d.onboardingComplete,
            lastCanvasPreset = p[Keys.lastPreset] ?: d.lastCanvasPreset,
        )
    }

    /**
     * Applies [transform] to the current settings inside the DataStore
     * transaction, so concurrent updates from different screens cannot clobber
     * each other (a full rewrite would lose whichever edit landed first).
     */
    suspend fun update(transform: (UiSettings) -> UiSettings) {
        context.dataStore.edit { p ->
            val s = transform(fromPreferences(p))
            p[Keys.theme] = s.themeMode.name
            p[Keys.accent] = s.accentArgb
            p[Keys.corners] = s.cornerStyle.name
            p[Keys.density] = s.density.name
            p[Keys.fontScale] = s.fontScale
            p[Keys.reduceMotion] = s.reduceMotion
            p[Keys.highContrast] = s.highContrast
            p[Keys.haptics] = s.haptics
            p[Keys.tips] = s.showOnboardingTips
            p[Keys.inputMode] = s.inputMode.name
            p[Keys.palm] = s.palmRejection
            p[Keys.stylusButton] = s.stylusButtonAction.name
            p[Keys.twoFinger] = s.twoFingerTap.name
            p[Keys.threeFinger] = s.threeFingerTap.name
            p[Keys.pressureSmoothing] = s.pressureSmoothing
            p[Keys.stabilizer] = s.stabilizerPreset
            p[Keys.doubleTapZoom] = s.doubleTapZoom
            p[Keys.rotation] = s.canvasRotationEnabled
            p[Keys.rail] = s.leftRailVisible
            p[Keys.inspector] = s.rightInspectorVisible
            p[Keys.inspectorTab] = s.inspectorTab
            p[Keys.quickBar] = s.showQuickBrushBar
            p[Keys.autosave] = s.autosaveSeconds
            p[Keys.historySteps] = s.maxHistorySteps
            p[Keys.backups] = s.keepRecoveryBackups
            p[Keys.thumbQuality] = s.thumbnailQuality
            p[Keys.lastBrush] = s.lastBrushId
            p[Keys.lastColor] = s.lastColorArgb
            p[Keys.onboarding] = s.onboardingComplete
            p[Keys.lastPreset] = s.lastCanvasPreset
        }
    }
}
