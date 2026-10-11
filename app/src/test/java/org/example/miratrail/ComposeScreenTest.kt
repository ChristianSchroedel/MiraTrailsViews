package org.example.miratrail

import android.os.Looper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import java.util.concurrent.TimeUnit
import org.example.miratrail.ui.TrailRoute
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-rDE-w448dp-h997dp-notnight-xxhdpi")
class ComposeScreenTest {
    @get:Rule(order = 0)
    val display = FixedScreenshotDisplay()

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    private val repository get() = (compose.activity.application as TrailApplication).repository

    @Before
    fun reset() {
        compose.runOnUiThread {
            repository.reset()
        }
    }

    private fun show(destination: TrailRoute) {
        compose.runOnUiThread { compose.activity.navigation.navigate(destination) }
        compose.waitForIdle()
        shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)
        compose.waitForIdle()
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun review(name: String) {
        compose.activity.window.decorView.captureRoboImage(
            "build/compose-review/$name.png",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
        )
    }

    @Test
    fun catalogSelectionSearchAndOpen() {
        show(TrailRoute.Catalog)
        compose.onNodeWithText("5 passende Wege").assertIsDisplayed()
        compose.onNodeWithText("Am kleinen Fluss").assertIsDisplayed()
        review("catalog")
        compose.onNodeWithText("Am kleinen Fluss").performTouchInput { longClick() }
        compose.onNodeWithText("Ausgewählt: Am kleinen Fluss. Antippen öffnet Details.")
            .assertIsDisplayed()
        compose.onNodeWithText("Name oder Ort suchen").performTextInput("Nordufer")
        compose.onNodeWithText("1 passende Wege").assertIsDisplayed()
        compose.onNodeWithText("Nur gemerkte Wege").performClick()
        compose.onNodeWithText("1 passende Wege").assertIsDisplayed()
        compose.onNodeWithText("Am kleinen Fluss").performClick()
        compose.onNodeWithText("1 von 3 Etappen abgeschlossen")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun editorValidationAndSave() {
        show(TrailRoute.Create)
        review("editor")
        compose.onNodeWithText("SPEICHERN").performScrollTo().performClick()
        compose.onNodeWithText("Bitte einen Namen eingeben.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Bitte einen Ort eingeben.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Name").performScrollTo().performTextInput("Compose-Runde")
        compose.onNodeWithText("Ort").performScrollTo().performTextInput("Nordpark")
        compose.onNodeWithText("Beschreibung")
            .performScrollTo()
            .performTextInput("Zum Fluss und zurück.")
        compose.onNodeWithText("Notiz").performScrollTo().performTextInput("Wasser mitnehmen.")
        compose.onNodeWithText("SPEICHERN").performScrollTo().performClick()
        compose.onNodeWithText("Compose-Runde").assertIsDisplayed()
        assertEquals("Wasser mitnehmen.", repository.walks.value.last().note)
    }

    @Test
    fun detailProgressAndNavigation() {
        show(TrailRoute.Detail())
        review("detail")
        compose.onNodeWithText("NÄCHSTE ETAPPE ABSCHLIESSEN").performScrollTo().performClick()
        compose.onNodeWithText("2 von 3 Etappen abgeschlossen")
            .performScrollTo()
            .assertIsDisplayed()
        compose.onNodeWithText("NÄCHSTE ETAPPE ABSCHLIESSEN").performScrollTo().performClick()
        compose.onNodeWithText("NÄCHSTE ETAPPE ABSCHLIESSEN").assertIsNotEnabled()
        compose.onNodeWithText("WEG BEARBEITEN").performScrollTo().performClick()
        compose.onNodeWithText("Am kleinen Fluss").assertExists()
    }

    @Test
    fun settingsResetRequiresConfirmation() {
        repository.save(repository.find(1)!!.copy(title = "Geändert"))
        show(TrailRoute.Settings)
        review("settings")
        compose.onNodeWithText("Ladefehler simulieren").performClick().assertIsOn()
        compose.onNodeWithText("BEISPIELDATEN ZURÜCKSETZEN").performScrollTo().performClick()
        compose.onNodeWithText("ABBRECHEN").performClick()
        assertEquals("Geändert", repository.find(1)!!.title)
        compose.onNodeWithText("BEISPIELDATEN ZURÜCKSETZEN").performClick()
        review("reset-dialog")
        compose.onNode(
            hasText("BEISPIELDATEN ZURÜCKSETZEN") and hasClickAction() and hasAnyAncestor(
                isDialog()
            )
        ).performClick()
        compose.onNodeWithText("Ladefehler simulieren").assertIsOff()
        assertEquals("Am kleinen Fluss", repository.find(1)!!.title)
    }

    @Test
    fun overviewAndCollectionsRender() {
        review("overview")
        compose.onNodeWithText("ALLE WEGE").performClick()
        compose.waitForIdle()
        shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)
        compose.waitForIdle()
        compose.onNodeWithText("Am kleinen Fluss").assertExists()
        for ((destination, name) in listOf(
            TrailRoute.Favorites to "favorites",
            TrailRoute.Planned to "planned",
            TrailRoute.Completed to "completed",
            TrailRoute.Notes to "notes"
        )) {
            show(destination)
            review(name)
        }
    }

    @Test
    fun repeatedEditorEntriesKeepIndependentDrafts() {
        show(TrailRoute.Edit(1))
        compose.onNode(hasSetTextAction() and hasText("Am kleinen Fluss"))
            .performTextReplacement("Erster Entwurf")
        show(TrailRoute.Edit(1))
        compose.onNode(hasSetTextAction() and hasText("Am kleinen Fluss")).assertIsDisplayed()
        compose.onNode(hasSetTextAction() and hasText("Am kleinen Fluss"))
            .performTextReplacement("Zweiter Entwurf")
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNode(hasSetTextAction() and hasText("Erster Entwurf")).assertIsDisplayed()
        show(TrailRoute.Edit(1))
        compose.onNode(hasSetTextAction() and hasText("Am kleinen Fluss")).assertIsDisplayed()
    }
}
