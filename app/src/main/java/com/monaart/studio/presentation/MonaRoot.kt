package com.monaart.studio.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.monaart.studio.app.monaViewModelFactory
import com.monaart.studio.core.design.Mona
import com.monaart.studio.presentation.assets.AssetLibraryScreen
import com.monaart.studio.presentation.brushstudio.BrushStudioScreen
import com.monaart.studio.presentation.editor.WorkspaceRoute
import com.monaart.studio.presentation.gallery.GalleryScreen
import com.monaart.studio.presentation.home.HomeScreen
import com.monaart.studio.presentation.newcanvas.NewCanvasScreen
import com.monaart.studio.presentation.settings.SettingsScreen
import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. */
@Serializable object HomeRoute
@Serializable object GalleryRoute
@Serializable object NewCanvasRoute
@Serializable data class EditorRoute(val projectId: Long)
@Serializable object BrushStudioRoute
@Serializable object AssetLibraryRoute
@Serializable object SettingsRoute

@Composable
fun MonaRoot(
    viewModel: MonaViewModel,
    settings: com.monaart.studio.domain.model.UiSettings,
) {
    val nav = rememberNavController()
    val reduceMotion = settings.reduceMotion
    val duration = if (reduceMotion) 0 else 260

    Box(Modifier.fillMaxSize().background(Mona.colors.ground)) {
        NavHost(
            navController = nav,
            startDestination = HomeRoute,
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
            composable<HomeRoute> {
                val projects by viewModel.visibleProjects.collectAsStateWithLifecycle()
                HomeScreen(
                    viewModel = viewModel,
                    settings = settings,
                    projects = projects,
                    onNewCanvas = { nav.navigate(NewCanvasRoute) },
                    onOpenGallery = { nav.navigate(GalleryRoute) },
                    onOpenProject = { id -> nav.navigate(EditorRoute(id)) },
                    onBrushStudio = { nav.navigate(BrushStudioRoute) },
                    onAssets = { nav.navigate(AssetLibraryRoute) },
                    onSettings = { nav.navigate(SettingsRoute) },
                )
            }
            composable<GalleryRoute> {
                val projects by viewModel.visibleProjects.collectAsStateWithLifecycle()
                GalleryScreen(
                    viewModel = viewModel,
                    settings = settings,
                    projects = projects,
                    onBack = { nav.popBackStack() },
                    onOpenProject = { id -> nav.navigate(EditorRoute(id)) },
                    onNewCanvas = { nav.navigate(NewCanvasRoute) },
                )
            }
            composable<NewCanvasRoute> {
                NewCanvasScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                    onCreated = { id ->
                        nav.navigate(EditorRoute(id)) {
                            popUpTo(HomeRoute)
                        }
                    },
                )
            }
            composable<EditorRoute> { entry ->
                val route = entry.toRoute<EditorRoute>()
                WorkspaceRoute(
                    projectId = route.projectId,
                    settings = settings,
                    viewModel = viewModel,
                    onExit = { nav.popBackStack() },
                    onOpenSettings = { nav.navigate(SettingsRoute) },
                )
            }
            composable<BrushStudioRoute> {
                BrushStudioScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                )
            }
            composable<AssetLibraryRoute> {
                AssetLibraryScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                )
            }
            composable<SettingsRoute> {
                SettingsScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBack = { nav.popBackStack() },
                )
            }
        }
    }
}
