package com.monaart.studio.presentation.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monaart.studio.core.design.Mona
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.MonaViewModel
import com.monaart.studio.ui.components.MonaButton

/**
 * Route wrapper for the drawing workspace. Owns the editor ViewModel lifetime
 * and the export/document file pickers; the workspace itself is stateless with
 * respect to persistence.
 */
@Composable
fun WorkspaceRoute(
    projectId: Long,
    settings: UiSettings,
    viewModel: MonaViewModel,
    onExit: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val editor: EditorViewModel = viewModel()
    val state by editor.state.collectAsStateWithLifecycle()
    val pendingExport = remember { mutableStateOf<ExportRequest?>(null) }
    var showExportSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showLayers by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        viewModel.projects.markOpened(projectId)
        editor.bindSettings(settings)
        editor.open(projectId)
    }
    LaunchedEffect(settings) { editor.bindSettings(settings) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("*/*"),
    ) { uri: Uri? ->
        val request = pendingExport.value
        if (uri != null && request != null) {
            // The sheet's actual choices are passed through — scale and quality
            // used to be hardcoded here, which silently disabled those controls.
            editor.exportToUri(uri, request.format, scale = request.scale, quality = request.quality)
        }
        pendingExport.value = null
    }

    Box(Modifier.fillMaxSize().background(Mona.colors.ground)) {
        val doc = state.document
        if (doc == null) {
                        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment =
                Alignment.CenterHorizontally) {
                Text(
                    state.statusMessage ?: "Preparing canvas…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mona.colors.textSecondary,
                )
                Spacer(Modifier.height(16.dp))
                MonaButton("Back to studio", onExit, tonal = true)
            }
        } else {
            DrawingWorkspace(
                editor = editor,
                state = state,
                settings = settings,
                onExit = { editor.closeEditor(onExit) },
                onExport = { showExportSheet = true },
                onDocumentSettings = { showSettingsSheet = true },
                onOpenSettings = onOpenSettings,
                onShowLayers = { showLayers = true },
            )
        }

        if (showExportSheet) {
            ExportSheet(
                state = state,
                onDismiss = { showExportSheet = false },
                onExport = { request ->
                    pendingExport.value = request
                    showExportSheet = false
                    exportLauncher.launch("${state.document?.name ?: "artwork"}.${request.format.extension}")
                },
            )
        }
        if (showSettingsSheet) {
            DocumentSettingsSheet(
                editor = editor,
                state = state,
                onDismiss = { showSettingsSheet = false },
            )
        }
        // Transient status toasts
        state.statusMessage?.let { message ->
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2200)
                editor.consumeStatus()
            }
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 96.dp),
            ) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Mona.colors.surface2)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(message, style = MaterialTheme.typography.labelMedium, color = Mona.colors.textPrimary)
                }
            }
        }
    }
}
