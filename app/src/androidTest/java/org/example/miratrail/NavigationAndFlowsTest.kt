package org.example.miratrail

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.fragment.NavHostFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

/** The six original Espresso journeys, exercised through Compose semantics. */
@RunWith(AndroidJUnit4::class)
class NavigationAndFlowsTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetExamples() {
        compose.runOnUiThread {
            (compose.activity.application as TrailApplication).repository.reset()
        }
    }

    private fun text(resource: Int) = compose.activity.getString(resource)
    private fun button(resource: Int) =
        compose.onNodeWithText(text(resource).uppercase(Locale.getDefault()))

    private fun destination() =
        (compose.activity.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment)
            .navController.currentDestination!!.id

    private fun openLoadedCatalog() {
        button(R.string.catalog).performScrollTo().performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Am kleinen Fluss").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("5 passende Wege").assertIsDisplayed()
    }

    private fun openWalk(title: String) {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(title))
        compose.onNodeWithText(title).performClick()
        compose.runOnIdle { assertEquals(R.id.detailFragment, destination()) }
    }

    private fun dismissKeyboard() {
        compose.runOnUiThread {
            val manager =
                compose.activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.hideSoftInputFromWindow(compose.activity.window.decorView.windowToken, 0)
        }
        compose.waitForIdle()
    }

    @Test
    fun allOverviewDestinationsOpen() {
        val destinations = listOf(
            Triple(R.string.catalog, R.id.catalogFragment, text(R.string.search_hint)),
            Triple(R.string.create, R.id.createFragment, text(R.string.route_name)),
            Triple(R.string.favorites, R.id.favoritesFragment, text(R.string.favorites)),
            Triple(R.string.planned, R.id.plannedFragment, text(R.string.planned)),
            Triple(R.string.completed, R.id.completedFragment, text(R.string.completed)),
            Triple(R.string.notes, R.id.notesFragment, text(R.string.notes)),
            Triple(R.string.settings, R.id.settingsFragment, text(R.string.simulate_error))
        )
        destinations.forEach { (label, expectedDestination, evidence) ->
            button(label).performScrollTo().performClick()
            compose.onNodeWithText(evidence).assertIsDisplayed()
            compose.runOnIdle { assertEquals(expectedDestination, destination()) }
            compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
            compose.onNodeWithText(text(R.string.overview_heading)).assertIsDisplayed()
        }
    }

    @Test
    fun catalogOpensDetailsAndEditor() {
        openLoadedCatalog()
        openWalk("Am kleinen Fluss")
        compose.onNodeWithContentDescription(
            "Etappen: 1 von 3 abgeschlossen. Alter Steg, Wiese, Baumallee."
        ).performScrollTo().assertIsDisplayed()
        button(R.string.edit).performScrollTo().performClick()
        compose.onNode(hasSetTextAction() and hasText("Am kleinen Fluss")).assertIsDisplayed()
        compose.runOnIdle { assertEquals(R.id.editFragment, destination()) }
    }

    @Test
    fun validationIsVisibleAndValidWalkCanBeSaved() {
        button(R.string.create).performScrollTo().performClick()
        button(R.string.save).performScrollTo().performClick()
        compose.onNodeWithText("Bitte einen Namen eingeben.").performScrollTo().assertIsDisplayed()
        compose.onNode(hasSetTextAction() and hasText(text(R.string.route_name)))
            .performScrollTo().performTextReplacement("Neue Runde")
        dismissKeyboard()
        compose.onNode(hasSetTextAction() and hasText(text(R.string.route_area)))
            .performScrollTo().performTextReplacement("Südpark")
        dismissKeyboard()
        button(R.string.save).performScrollTo().performClick()
        compose.onNodeWithText("Neue Runde").assertIsDisplayed()
        compose.runOnIdle { assertEquals(R.id.detailFragment, destination()) }
    }

    @Test
    fun completedWalkDisablesNextStage() {
        openLoadedCatalog()
        openWalk("Markt und Mauer")
        button(R.string.next_stage).performScrollTo().assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun searchCanShowEmptyState() {
        openLoadedCatalog()
        compose.onNode(hasSetTextAction() and hasText(text(R.string.search_hint)))
            .performTextReplacement("unbekannter Ort")
        dismissKeyboard()
        compose.onNodeWithText(text(R.string.empty_list)).assertIsDisplayed()
        compose.onNodeWithText("0 passende Wege").assertIsDisplayed()
        compose.onNodeWithText("Am kleinen Fluss").assertDoesNotExist()
    }

    @Test
    fun stageViewHasAccessibleSummary() {
        openLoadedCatalog()
        openWalk("Am kleinen Fluss")
        compose.onNodeWithContentDescription(
            "Etappen: 1 von 3 abgeschlossen. Alter Steg, Wiese, Baumallee."
        ).performScrollTo().assertIsDisplayed()
    }
}
