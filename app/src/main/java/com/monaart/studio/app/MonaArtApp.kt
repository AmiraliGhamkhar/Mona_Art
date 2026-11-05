package com.monaart.studio.app

import android.app.Application
import android.content.Context
import com.monaart.studio.data.db.ProjectIndex
import com.monaart.studio.data.repository.BrushLibraryRepository
import com.monaart.studio.data.repository.ProjectRepository
import com.monaart.studio.data.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point and manual dependency container.
 *
 * The app deliberately uses a small hand-written container instead of a DI
 * framework: there are eight singletons, their lifetimes are obvious, and the
 * graph is trivial to read in one file.
 */
class MonaArtApp : Application() {
    lateinit var index: ProjectIndex
        private set
    lateinit var settings: SettingsStore
        private set
    lateinit var projects: ProjectRepository
        private set
    lateinit var brushes: BrushLibraryRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        index = ProjectIndex(java.io.File(filesDir, "index"))
        settings = SettingsStore(this)
        projects = ProjectRepository(this, index)
        brushes = BrushLibraryRepository(this)
        appScope.launch {
            projects.sync()
            brushes.ensureBuiltIns()
        }
    }

    companion object {
        lateinit var instance: MonaArtApp
            private set

        fun container(context: Context): MonaArtApp =
            context.applicationContext as MonaArtApp
    }
}
