package com.monaart.studio.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monaart.studio.core.design.MonaTheme
import com.monaart.studio.presentation.MonaRoot
import com.monaart.studio.presentation.MonaViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    /** A file handed to us by another app (VIEW intent), consumed once by the UI. */
    private val pendingFile = MutableStateFlow<PendingFile?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent { MonaApp(pendingFile) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * The manifest advertises `.mona` projects and images for ACTION_VIEW, so the
     * intent has to reach the repository. Without this the app claimed to open
     * files and silently did nothing.
     */
    private fun handleIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val uri = intent.data ?: return
        pendingFile.value = PendingFile(uri, intent.resolveDisplayName())
    }

    private fun Intent.resolveDisplayName(): String? {
        val resolver = contentResolver
        return runCatching {
            resolver.query(data ?: return null, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
        }.getOrNull()
    }
}

/** A file the user asked Mona Art to open from outside the app. */
data class PendingFile(val uri: Uri, val displayName: String?)

@Composable
private fun MonaApp(
    pendingFile: MutableStateFlow<PendingFile?>,
    viewModel: MonaViewModel = viewModel(factory = monaViewModelFactory()),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val incoming by pendingFile.collectAsStateWithLifecycle()
    LaunchedEffect(incoming) {
        val file = incoming ?: return@LaunchedEffect
        viewModel.handleIncomingFile(file.uri, file.displayName)
        pendingFile.value = null
    }
    MonaTheme(settings = settings) {
        MonaRoot(viewModel = viewModel, settings = settings)
    }
}

/**
 * Factory that hands the manual dependency container to the root ViewModel.
 *
 * The Application comes from [ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
 * (the activity's real application); the static instance is only a fallback for
 * test environments that construct the VM without an activity. Constructing
 * MonaViewModel with a detached `Application()` is exactly what previously made
 * every incoming-file import crash on `contentResolver`.
 */
@Suppress("UNCHECKED_CAST")
fun monaViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            ?: MonaArtApp.instance
        return MonaViewModel(app, app.settings, app.projects, app.brushes) as T
    }
}
