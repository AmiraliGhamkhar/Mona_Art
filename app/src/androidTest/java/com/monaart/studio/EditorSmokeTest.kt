package com.monaart.studio

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monaart.engine.brush.BrushEngine
import com.monaart.engine.brush.BrushPresets
import com.monaart.engine.brush.StrokePoint
import com.monaart.engine.document.ArtDocument
import com.monaart.engine.paint.alphaOf
import com.monaart.studio.app.MainActivity
import com.monaart.studio.data.db.ProjectIndex
import com.monaart.studio.data.repository.LoadResult
import com.monaart.studio.data.repository.ProjectRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Phase 0 instrumented smoke test.
 *
 * It deliberately avoids Compose UI testing APIs: the app launches, then the
 * test drives the engine + repository directly to prove that a drawn stroke
 * survives a save/load round trip on a real Android process. Phase 5 expands
 * this into the six-scenario device matrix.
 */
@RunWith(AndroidJUnit4::class)
class EditorSmokeTest {

    @Test
    fun launchCreateStrokeSaveReload_paintsPixel() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            val index = ProjectIndex(File(context.filesDir, "index"))
            val repository = ProjectRepository(context, index)
            val document = ArtDocument.create(
                name = "Phase 0 smoke",
                width = 256,
                height = 256,
                transparentBackground = true,
            )
            val layer = requireNotNull(document.activeLayer())

            val engine = BrushEngine()
            engine.begin(
                document,
                layer,
                BrushPresets.default.copy(size = 16f, spacing = 0.08f),
                0xFF112233.toInt(),
                seed = 11,
            )
            for (i in 0..20) {
                engine.move(StrokePoint(20f + i * 10f, 128f, pressure = 1f))
            }
            engine.end()

            val before = layer.requireSurface().getPixel(128, 128)
            assertTrue("stroke must paint before save", alphaOf(before) > 0)

            repository.save(document, makeThumbnail = false)
            when (val result = repository.load(document.id)) {
                is LoadResult.Success -> {
                    val reloaded = result.document.activeLayer()?.requireSurface()?.getPixel(128, 128)
                    assertNotNull("reloaded document must have an active layer", reloaded)
                    assertTrue("stroke pixel must survive reload", alphaOf(reloaded!!) > 0)
                }
                is LoadResult.Failure -> error("load failed: ${result.message}")
            }
        } finally {
            scenario.close()
        }
    }
}
