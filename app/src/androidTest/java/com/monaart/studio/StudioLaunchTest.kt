package com.monaart.studio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onRoot
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monaart.studio.app.MainActivity
import com.monaart.studio.app.MonaArtApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Application-level launch test.
 *
 * [EditorSmokeTest] drives the engine and the repository directly while an
 * Activity happens to be open; that proves the paint core works on a device but
 * says nothing about whether the real UI comes up. This one launches the actual
 * [MainActivity] through its real [MonaArtApp] container and asserts against the
 * Compose tree the user sees.
 *
 * Scope, stated plainly: this covers process start, container wiring and first
 * composition. Driving strokes, crop and filter dialogs through injected touch
 * needs an emulator or device, and none is available in CI here, so those
 * scenarios are not claimed as covered.
 */
@RunWith(AndroidJUnit4::class)
class StudioLaunchTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun studioShellRendersAfterLaunch() {
        compose.waitForIdle()
        compose.onRoot(useUnmergedTree = true).assertExists()
        // onAllNodesWithText(...).onFirst() rather than onNodeWithText: the header
        // text may also appear inside a merged accessibility node, and an
        // ambiguous match would fail the test for the wrong reason.
        compose.onAllNodesWithText("Mona Art").onFirst().assertIsDisplayed()
    }

    @Test
    fun activityReachesResumedOnTheRealContainer() {
        compose.activityRule.scenario.onActivity { activity ->
            assertEquals(Lifecycle.State.RESUMED, activity.lifecycle.currentState)
        }
    }

    /**
     * The manual dependency container has to be fully initialised before the
     * first composition, because the root ViewModel reads all four singletons
     * straight out of it in its factory.
     */
    @Test
    fun dependencyContainerIsReadyBeforeComposition() {
        compose.activityRule.scenario.onActivity { activity ->
            val app = activity.application as MonaArtApp
            assertNotNull(app.index)
            assertNotNull(app.settings)
            assertNotNull(app.projects)
            assertNotNull(app.brushes)
        }
    }
}
