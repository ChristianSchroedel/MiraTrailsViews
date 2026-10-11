package org.example.miratrail

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.core.view.WindowInsetsCompat
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.example.miratrail.ui.TrailRoute
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

    private fun destination() = compose.activity.navigation.currentRoute

    private fun openLoadedCatalog() {
        button(R.string.catalog).performScrollTo().performClick()
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Am kleinen Fluss").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("5 passende Wege").assertIsDisplayed()
    }

    private fun openWalk(title: String) {
        compose.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(title))
        compose.onNodeWithText(title).performClick()
        compose.runOnIdle {
            assertEquals(
                TrailRoute.Detail(
                    (compose.activity.application as TrailApplication)
                        .repository.walks.value.first { it.title == title }.id
                ), destination()
            )
        }
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
            Triple(R.string.catalog, TrailRoute.Catalog, text(R.string.search_hint)),
            Triple(R.string.create, TrailRoute.Create, text(R.string.route_name)),
            Triple(R.string.favorites, TrailRoute.Favorites, text(R.string.favorites)),
            Triple(R.string.planned, TrailRoute.Planned, text(R.string.planned)),
            Triple(R.string.completed, TrailRoute.Completed, text(R.string.completed)),
            Triple(R.string.notes, TrailRoute.Notes, text(R.string.notes)),
            Triple(R.string.settings, TrailRoute.Settings, text(R.string.simulate_error))
        )
        destinations.forEach { (label, expectedDestination, evidence) ->
            button(label).performScrollTo().performClick()
            compose.onAllNodesWithText(evidence).onLast().assertIsDisplayed()
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
        compose.runOnIdle { assertEquals(TrailRoute.Edit(), destination()) }
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
        compose.runOnIdle {
            val created = (compose.activity.application as TrailApplication)
                .repository.walks.value.first { it.title == "Neue Runde" }
            assertEquals(TrailRoute.Detail(created.id), destination())
        }
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

    @Test
    fun editorCanBeScrolledAndSavedWithKeyboardOpen() {
        button(R.string.create).performScrollTo().performClick()
        compose.onNode(hasSetTextAction() and hasText(text(R.string.route_name)))
            .performTextReplacement("Tastaturrunde")
        dismissKeyboard()
        compose.onNode(hasSetTextAction() and hasText(text(R.string.route_area)))
            .performTextReplacement("Südpark")
        dismissKeyboard()
        val note = compose.onNode(hasSetTextAction() and hasText(text(R.string.route_note)))
        note.performScrollTo().performTextInput("Auch mit Tastatur erreichbar.")
        compose.waitUntil(10_000) {
            val insets = compose.activity.window.decorView.rootWindowInsets
            insets != null && WindowInsetsCompat.toWindowInsetsCompat(insets)
                .isVisible(WindowInsetsCompat.Type.ime())
        }
        note.performScrollTo().assertIsDisplayed().assertIsFocused()
        assertAboveKeyboard(note)
        button(R.string.save).performScrollTo().assertIsDisplayed()
        assertAboveKeyboard(button(R.string.save))
        button(R.string.save).performClick()
        compose.onNodeWithText("Tastaturrunde").assertIsDisplayed()
        compose.runOnIdle {
            val saved = (compose.activity.application as TrailApplication)
                .repository.walks.value.last()
            assertEquals("Auch mit Tastatur erreichbar.", saved.note)
            assertEquals(TrailRoute.Detail(saved.id), destination())
        }
    }

    private fun assertAboveKeyboard(node: SemanticsNodeInteraction) {
        val bounds = node.fetchSemanticsNode().boundsInWindow
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            val insets = WindowInsetsCompat.toWindowInsetsCompat(view.rootWindowInsets)
            val keyboardTop = view.height - insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            assertTrue(
                "Element verdeckt durch Tastatur: $bounds, Tastatur ab $keyboardTop",
                bounds.bottom <= keyboardTop
            )
        }
    }

    @Test
    fun editorDraftAndBackStackSurviveActivityRecreation() {
        openLoadedCatalog()
        openWalk("Am kleinen Fluss")
        button(R.string.edit).performScrollTo().performClick()
        compose.onNode(hasSetTextAction() and hasText("Am kleinen Fluss"))
            .performTextReplacement("Ungespeicherter Entwurf")
        dismissKeyboard()
        compose.activityRule.scenario.recreate()
        compose.onNode(hasSetTextAction() and hasText("Ungespeicherter Entwurf"))
            .assertIsDisplayed()
        compose.runOnIdle { assertEquals(TrailRoute.Edit(1), destination()) }
        compose.onNodeWithContentDescription(text(R.string.navigate_back)).performClick()
        compose.runOnIdle { assertEquals(TrailRoute.Detail(1), destination()) }
        compose.onNodeWithText("Am kleinen Fluss").performScrollTo().assertIsDisplayed()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.runOnIdle { assertEquals(TrailRoute.Catalog, destination()) }
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Am kleinen Fluss").fetchSemanticsNodes().isNotEmpty()
        }
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText(text(R.string.overview_heading)).assertIsDisplayed()
    }
}
