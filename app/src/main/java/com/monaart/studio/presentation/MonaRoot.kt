package com.monaart.studio.presentation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.monaart.studio.core.design.Mona
import com.monaart.studio.domain.model.UiSettings
import com.monaart.studio.presentation.assets.AssetLibraryScreen
import com.monaart.studio.presentation.brushstudio.BrushStudioScreen
import com.monaart.studio.presentation.editor.WorkspaceRoute
import com.monaart.studio.presentation.gallery.GalleryScreen
import com.monaart.studio.presentation.home.HomeScreen
import com.monaart.studio.presentation.newcanvas.NewCanvasScreen
import com.monaart.studio.presentation.settings.SettingsScreen
import com.monaart.studio.ui.components.MonaPill

/**
 * Route table.
 *
 * String routes are used deliberately: type-safe navigation requires the Kotlin
 * serialization compiler plugin, which was removed together with the other
 * annotation-processing toolchains. The routes are centralised here so a future
 * plugin-enabled build only has to change this file.
 */
object Routes {
    const val HOME = "home"
    const val GALLERY = "gallery"
    const val NEW_CANVAS = "new"
    const val EDITOR = "editor/{projectId}"
    const val BRUSH_STUDIO = "brushes"
    const val ASSETS = "assets"
    const val SETTINGS = "settings"

    fun editor(projectId: Long): String = "editor/$projectId"
}

@Composable
fun MonaRoot(
    viewModel: MonaViewModel,
    settings: UiSettings,
) {
    val nav = rememberNavController()
    val reduceMotion = settings.reduceMotion
    val duration = if (reduceMotion) 0 else 260

    // A project imported from another app opens straight into the editor, and the
    // outcome (opened / failed) is reported rather than silently dropped.
    val importedId by viewModel.openProjectId.collectAsStateWithLifecycle()
    val intakeMessage by viewModel.intakeMessage.collectAsStateWithLifecycle()
    LaunchedEffect(importedId) {
        val id = importedId ?: return@LaunchedEffect
        nav.navigate(Routes.editor(id)) { popUpTo(Routes.HOME) }
        viewModel.consumeOpenProject()
    }

    Box(Modifier.fillMaxSize().background(Mona.colors.ground)) {
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            enterTransition = {
                if (reduceMotion) fadeIn(tween(0)) else slideInHorizontally(tween(duration)) { it / 8 } + fadeIn(tween(duration))
            },
            exitTransition = {
                if (reduceMotion) fadeOut(tween(0)) else fadeOut(tween(duration / 2))
            },
            popEnterTransition = {
                if (reduceMotion) fadeIn(tween(0)) else fadeIn(tween(duration))
            },
            popExitTransition = {
                if (reduceMotion) fadeOut(tween(0)) else slideOutHorizontally(tween(duration)) { it / 8 } + fadeOut(tween(duration))
            },
        ) {
            composable(Routes.HOME) {
                val projects by viewModel.visibleProjects.collectAsStateWithLifecycle()
                HomeScreen(
                    viewModel = viewModel,
                    settings = settings,
                    projects = projects,
                    onNewCanvas = { nav.navigate(Routes.NEW_CANVAS) },
                    onOpenGallery = { nav.navigate(Routes.GALLERY) },
                    onOpenProject = { id -> nav.navigate(Routes.editor(id)) },
                    onBrushStudio = { nav.navigate(Routes.BRUSH_STUDIO) },
                    onAssets = { nav.navigate(Routes.ASSETS) },
                    onSettings = { nav.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.GALLERY) {
                val projects by viewModel.visibleProjects.collectAsStateWithLifecycle()
                GalleryScreen(
                    viewModel = viewModel,
                    settings = settings,
                    projects = projects,
                    onBack = { nav.popBackStack() },
                    onOpenProject = { id -> nav.navigate(Routes.editor(id)) },
                    onNewCanvas = { nav.navigate(Routes.NEW_CANVAS) },
                )
            }
            composable(Routes.NEW_CANVAS) {
                NewCanvasScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                    onCreated = { id ->
                        nav.navigate(Routes.editor(id)) { popUpTo(Routes.HOME) }
                    },
                )
            }
            composable(
                route = Routes.EDITOR,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType }),
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId") ?: -1L
                WorkspaceRoute(
                    projectId = projectId,
                    settings = settings,
                    viewModel = viewModel,
                    onExit = { nav.popBackStack() },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.BRUSH_STUDIO) {
                BrushStudioScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.ASSETS) {
                AssetLibraryScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                )
            }
        }

        intakeMessage?.let { message ->
            IntakeBanner(message, onDismiss = { viewModel.consumeIntakeMessage() })
        }
    }
}

/**
 * Reports the result of opening a file from another app. Deliberately persistent
 * until dismissed: an import that fails while the user is looking elsewhere should
 * still be visible when they come back.
 */
@Composable
private fun BoxScope.IntakeBanner(message: String, onDismiss: () -> Unit) {
    val colors = Mona.colors
    Row(
        Modifier
            .align(Alignment.TopCenter)
            .padding(16.dp)
            .clip(RoundedCornerShape(Mona.shapes.lg))
            .background(colors.surface2)
            .border(1.dp, colors.border, RoundedCornerShape(Mona.shapes.lg))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(12.dp))
        MonaPill("Dismiss", onClick = onDismiss)
    }
}
