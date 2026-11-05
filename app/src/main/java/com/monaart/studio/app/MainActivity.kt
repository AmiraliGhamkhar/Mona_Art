package com.monaart.studio.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monaart.studio.core.design.MonaTheme
import com.monaart.studio.presentation.MonaRoot
import com.monaart.studio.presentation.MonaViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { MonaApp() }
    }
}

@Composable
private fun MonaApp(
    viewModel: MonaViewModel = viewModel(factory = monaViewModelFactory()),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    MonaTheme(settings = settings) {
        MonaRoot(viewModel = viewModel, settings = settings)
    }
}

/** Factory that hands the manual dependency container to the root ViewModel. */
@Suppress("UNCHECKED_CAST")
fun monaViewModelFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras,
    ): T {
        val app = MonaArtApp.instance
        return MonaViewModel(app.settings, app.projects, app.brushes) as T
    }
}
