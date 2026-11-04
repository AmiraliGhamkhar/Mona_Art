package com.monaart.studio.domain.model

/** Theme selection. */
enum class ThemeMode { System, Dark, Light, Amoled }

/** How round the interface should feel. */
enum class CornerStyle(val factor: Float) {
    Sharp(0.45f),
    Soft(1f),
    Round(1.5f),
}

/** Interface density multiplier for spacing tokens. */
enum class Density(val factor: Float) {
    Compact(0.8f),
    Comfortable(1f),
    Spacious(1.25f),
}

/** Where stylus input is trusted for painting. */
enum class InputMode(val label: String, val description: String) {
    StylusOnly("Stylus only", "Fingers pan and zoom; only a stylus paints. Best against palm touches."),
    StylusPreferFinger("Stylus + finger", "A stylus always paints; fingers paint when no stylus has been used recently."),
    AnyInput("Any input", "Fingers and styluses both paint. Gestures then need two fingers."),
}

/** Two/three-finger gesture shortcuts. */
enum class GestureAction(val label: String) {
    Undo("Undo"),
    Redo("Redo"),
    ToggleLayers("Toggle layers panel"),
    ToggleBrush("Toggle brush settings"),
    Nothing("Nothing"),
}

/** The user's persisted UI and input preferences. */
data class UiSettings(
    val themeMode: ThemeMode = ThemeMode.Dark,
    val accentArgb: Int = 0,
    val cornerStyle: CornerStyle = CornerStyle.Soft,
    val density: Density = Density.Comfortable,
    val fontScale: Float = 1f,
    val reduceMotion: Boolean = false,
    val highContrast: Boolean = false,
    val haptics: Boolean = true,
    val showOnboardingTips: Boolean = true,
    // Input
    val inputMode: InputMode = InputMode.StylusOnly,
    val palmRejection: Boolean = true,
    val stylusButtonAction: GestureAction = GestureAction.Undo,
    val twoFingerTap: GestureAction = GestureAction.Undo,
    val threeFingerTap: GestureAction = GestureAction.Redo,
    val pressureSmoothing: Float = 0.35f,
    val stabilizerPreset: Float = 0.35f,
    val doubleTapZoom: Boolean = true,
    val canvasRotationEnabled: Boolean = true,
    // Workspace
    val leftRailVisible: Boolean = true,
    val rightInspectorVisible: Boolean = true,
    val inspectorTab: String = "brush",
    val showQuickBrushBar: Boolean = true,
    // Performance / storage
    val autosaveSeconds: Int = 20,
    val maxHistorySteps: Int = 120,
    val keepRecoveryBackups: Int = 3,
    val thumbnailQuality: Int = 1024,
    // Persistence for the last session
    val lastBrushId: String = "ink.g-pen",
    val lastColorArgb: Int = 0xFF17131F.toInt(),
    val onboardingComplete: Boolean = false,
    val lastCanvasPreset: String = "square-2048",
)

/** Immutable snapshot of an editable workspace layout. */
data class WorkspaceLayout(
    val railVisible: Boolean = true,
    val inspectorVisible: Boolean = true,
    val inspectorWidth: Float = 300f,
    val quickBrushBarVisible: Boolean = true,
) {
    fun toggledRail() = copy(railVisible = !railVisible)
    fun toggledInspector() = copy(inspectorVisible = !inspectorVisible)
}
