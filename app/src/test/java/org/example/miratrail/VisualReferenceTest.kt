package org.example.miratrail

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.widget.FrameLayout
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.github.takahirom.roborazzi.*
import java.io.File
import java.util.concurrent.TimeUnit
import org.example.miratrail.data.Walk
import org.example.miratrail.ui.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Existing XML references and states, now driven through the real Nav-3 Compose destinations. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-rDE-w448dp-h997dp-notnight-xxhdpi")
@OptIn(ExperimentalRoborazziApi::class)
class VisualReferenceTest {
    @get:Rule(order = 0)
    val display = FixedScreenshotDisplay()

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    private val activity get() = compose.activity
    private val repository get() = (activity.application as TrailApplication).repository
    private val differences = mutableListOf<String>()
    private val model
        get() = ViewModelProvider(
            activity,
            WalkViewModelFactory(repository)
        )[WalkViewModel::class.java]

    @Before
    fun reset() = compose.runOnUiThread { repository.reset() }

    @After
    fun reportAllComparisons() {
        if (differences.isNotEmpty()) fail(differences.joinToString("\n\n"))
    }

    private fun settle() {
        compose.waitForIdle()
        shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)
        compose.mainClock.advanceTimeBy(500)
        compose.waitForIdle()
    }

    private fun show(destination: TrailRoute, id: Int = 1) {
        val route = when (destination) {
            is TrailRoute.Detail -> TrailRoute.Detail(id)
            is TrailRoute.Edit -> TrailRoute.Edit(id)
            else -> destination
        }
        compose.runOnUiThread { activity.navigation.navigate(route) }
        settle()
    }

    private fun scroll(delta: Float) {
        compose.onNode(hasScrollAction() and !hasSetTextAction())
            .performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, delta) }
        compose.waitForIdle()
    }

    @OptIn(ExperimentalRoborazziApi::class)
    private fun compare(path: String) {
        val root = activity.window.decorView
        assertEquals(1344, root.width)
        assertEquals(2992, root.height)
        root.captureRoboImage(
            "build/compose-review/${File(path).name}",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
        )
        try {
            root.captureRoboImage(path, roborazziOptions = screenScreenshotOptions())
        } catch (difference: AssertionError) {
            differences += "${File(path).name}: ${difference.message}"
        }
    }

    private fun captureScreen(name: String) {
        scroll(-1_000_000f)
        compare("src/test/snapshots/${name}_window.png")
        // Preserve the existing coverage: only capture a bottom when its XML reference exists.
        val bottom = "src/test/snapshots/${name}_bottom.png"
        if (File(bottom).exists()) {
            scroll(1_000_000f)
            compare(bottom)
            scroll(-1_000_000f)
        }
    }

    private fun screen(destination: TrailRoute, name: String, id: Int = 1) {
        show(destination, id)
        captureScreen(name)
    }

    private fun changeEditor(
        title: String,
        area: String,
        description: String = "",
        note: String = ""
    ) {
        // Direct SetText changes the fixture without requesting focus or opening the IME.
        listOf("Name" to title, "Ort" to area, "Beschreibung" to description, "Notiz" to note)
            .forEach { (label, value) ->
                compose.onNode(hasSetTextAction() and hasText(label))
                    .performSemanticsAction(SemanticsActions.SetText) { it(AnnotatedString(value)) }
            }
        settle()
    }

    private fun save() {
        compose.onNodeWithText("SPEICHERN").performScrollTo().performClick()
        settle()
    }

    @Test
    fun overview() {
        settle()
        compare("src/test/snapshots/org.example.miratrail.VisualReferenceTest.overview.png")
    }

    @Test
    fun catalogWithContent() {
        show(TrailRoute.Catalog)
        compose.onNodeWithText("5 passende Wege").assertIsDisplayed()
        compose.onNodeWithText("Am kleinen Fluss").assertIsDisplayed()
        assertEquals(5, model.state.value.walks.size)
        compare("src/test/snapshots/org.example.miratrail.VisualReferenceTest.catalogWithContent.png")
    }

    @Test
    fun catalogEmpty() {
        show(TrailRoute.Catalog)
        compose.runOnUiThread { model.setQuery("kein passender Weg") }
        settle()
        compose.onNodeWithText(activity.getString(R.string.empty_list)).assertIsDisplayed()
        compare("src/test/snapshots/org.example.miratrail.VisualReferenceTest.catalogEmpty.png")
    }

    @Test
    fun formValidation() {
        show(TrailRoute.Create)
        save()
        scroll(-1_000_000f)
        compose.onNodeWithText("Bitte einen Namen eingeben.").assertIsDisplayed()
        compare("src/test/snapshots/org.example.miratrail.VisualReferenceTest.formValidation.png")
    }

    @Test
    fun overviewCompleteContent() = screen(TrailRoute.Overview, "overview_full")

    @Test
    fun catalogCompleteContent() = screen(TrailRoute.Catalog, "catalog_full")

    @Test
    fun catalogLoading() {
        show(TrailRoute.Catalog)
        compose.mainClock.autoAdvance = false
        compose.runOnUiThread { model.load() }
        shadowOf(Looper.getMainLooper()).idle()
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        assertEquals(CatalogPhase.LOADING, model.state.value.phase)
        compose.onNodeWithContentDescription("Wird geladen").assertExists()
        compare("src/test/snapshots/catalog_loading_window.png")
        compose.mainClock.autoAdvance = true
    }

    @Test
    fun catalogError() {
        compose.runOnUiThread { model.setSimulateLoadError(true) }
        show(TrailRoute.Catalog)
        compose.onNodeWithText(activity.getString(R.string.load_error)).assertIsDisplayed()
        captureScreen("catalog_error")
    }

    @Test
    fun catalogFavoritesFilter() {
        show(TrailRoute.Catalog)
        compose.onNodeWithText(activity.getString(R.string.only_favorites)).performClick()
        settle()
        assertEquals(2, model.state.value.walks.size)
        captureScreen("catalog_favorites")
    }

    @Test
    fun catalogSearchMatch() {
        show(TrailRoute.Catalog)
        compose.runOnUiThread { model.setQuery("Nordufer") }
        settle()
        assertEquals(1, model.state.value.walks.size)
        captureScreen("catalog_search_match")
    }

    @Test
    fun catalogFavoritesSearchEmpty() {
        show(TrailRoute.Catalog)
        compose.runOnUiThread {
            model.setFavoritesOnly(true)
            model.setQuery("Südhöhe")
        }
        settle()
        compose.onNodeWithText(activity.getString(R.string.empty_list)).assertIsDisplayed()
        captureScreen("catalog_favorites_empty")
    }

    @Test
    fun catalogSelection() {
        show(TrailRoute.Catalog)
        compose.onNodeWithText("Am kleinen Fluss").performTouchInput { longClick() }
        settle()
        compose.onNodeWithText("Ausgewählt: Am kleinen Fluss. Antippen öffnet Details.")
            .assertIsDisplayed()
        captureScreen("catalog_selected")
    }

    private val collections = listOf(
        TrailRoute.Favorites to "favorites", TrailRoute.Planned to "planned",
        TrailRoute.Completed to "completed", TrailRoute.Notes to "notes"
    )

    @Test
    fun collectionsWithContent() {
        collections.forEach { (destination, name) -> screen(destination, "${name}_populated") }
    }

    @Test
    fun collectionsEmpty() {
        compose.runOnUiThread {
            repository.walks.value.forEach {
                repository.save(
                    it.copy(
                        favorite = false,
                        planned = false,
                        completedStages = 0,
                        note = ""
                    )
                )
            }
        }
        collections.forEach { (destination, name) -> screen(destination, "${name}_empty") }
    }

    @Test
    fun detailSeedWalks() {
        (1..5).forEach { id -> screen(TrailRoute.Detail(), "detail_walk_$id", id) }
    }

    @Test
    fun detailToggleCombinations() {
        listOf(false, true).forEach { favorite ->
            listOf(false, true).forEach { planned ->
                compose.runOnUiThread {
                    repository.save(
                        repository.find(1)!!
                            .copy(favorite = favorite, planned = planned)
                    )
                }
                screen(TrailRoute.Detail(), "detail_favorite_${favorite}_planned_$planned")
            }
        }
    }

    @Test
    fun detailProgressStates() {
        (0..3).forEach { completed ->
            compose.runOnUiThread {
                repository.save(
                    repository.find(1)!!
                        .copy(completedStages = completed)
                )
            }
            show(TrailRoute.Detail())
            compose.onNodeWithText("NÄCHSTE ETAPPE ABSCHLIESSEN").apply {
                if (completed < 3) assertIsEnabled() else assertIsNotEnabled()
            }
            captureScreen("detail_progress_$completed")
        }
    }

    @Test
    fun detailProgressUpdatesInPlace() {
        show(TrailRoute.Detail())
        (2..3).forEach { completed ->
            compose.onNodeWithText("NÄCHSTE ETAPPE ABSCHLIESSEN").performScrollTo().performClick()
            settle()
            compose.onNodeWithText("$completed von 3 Etappen abgeschlossen").assertExists()
            compose.onNodeWithContentDescription(
                "Etappen: $completed von 3 abgeschlossen. Alter Steg, Wiese, Baumallee."
            ).assertExists()
            compose.onNodeWithText("NÄCHSTE ETAPPE ABSCHLIESSEN").apply {
                if (completed < 3) assertIsEnabled() else assertIsNotEnabled()
            }
            captureScreen("detail_progress_$completed")
        }
    }

    @Test
    fun detailLongContentAndGenericArtwork() {
        var id = 0
        compose.runOnUiThread {
            id = repository.save(
                Walk(
                    0,
                    "Ein langer Spaziergang durch die Gärten und entlang des alten Flussufers",
                    "Gemeinschaftsgärten am südlichen Stadtrand",
                    "Über schmale Wege geht es durch die Gärten zum Ufer. ".repeat(12),
                    listOf("Gartentor", "Flussufer", "Heimweg"),
                    note = "Bei Regen ist der Weg rutschig.\nEine Pause am Fluss einplanen.\nAm Gartentor treffen."
                )
            ).id
        }
        screen(TrailRoute.Detail(), "detail_long_content", id)
        show(TrailRoute.Catalog)
        compose.runOnUiThread { model.setQuery(repository.find(id)!!.title) }
        settle()
        captureScreen("catalog_generic_artwork")
    }

    @Test
    fun createBlank() = screen(TrailRoute.Create, "create_blank")

    @Test
    fun createFilled() {
        show(TrailRoute.Create)
        changeEditor(
            "Neue Runde am Fluss",
            "Südpark",
            "Vom Park zum Ufer.\nÜber den Steg zurück.",
            "Wasser und eine Regenjacke mitnehmen."
        )
        captureScreen("create_filled")
        save()
        captureScreen("create_saved_detail")
    }

    @Test
    fun formErrorVariants() {
        listOf("" to "", "" to "Nordufer", "Ab" to "Nordufer", "Gültiger Name" to "")
            .forEachIndexed { index, (title, area) ->
                show(TrailRoute.Create)
                changeEditor(title, area)
                save()
                assertTrue(
                    compose.onAllNodes(
                        SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)
                    ).fetchSemanticsNodes().isNotEmpty()
                )
                captureScreen("create_validation_$index")
            }
    }

    @Test
    fun editPrefilled() = screen(TrailRoute.Edit(), "edit_prefilled")

    @Test
    fun editWithoutNote() = screen(TrailRoute.Edit(), "edit_without_note", 2)

    @Test
    fun editValidationAndCorrection() {
        show(TrailRoute.Edit())
        val old = repository.find(1)!!
        changeEditor("", "", old.description, old.note)
        save()
        captureScreen("edit_validation")
        changeEditor("Korrigierter Weg", "Südpark", old.description, old.note)
        save()
        captureScreen("edit_corrected_detail")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun settingsStatesAndResetDialog() {
        show(TrailRoute.Settings)
        captureScreen("settings_default")
        compose.onNodeWithText(activity.getString(R.string.simulate_error))
            .performClick()
            .assertIsOn()
        assertTrue(model.simulateLoadError)
        captureScreen("settings_error_enabled")
        compose.onNodeWithText("BEISPIELDATEN ZURÜCKSETZEN").performScrollTo().performClick()
        compose.onNodeWithText(activity.getString(R.string.reset_confirm)).assertIsDisplayed()
        captureScreenRoboImage(
            "build/compose-review/settings_reset_dialog.png",
            roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record)
        )
        try {
            captureScreenRoboImage(
                "src/test/snapshots/settings_reset_dialog.png",
                roborazziOptions = screenScreenshotOptions()
            )
        } catch (difference: AssertionError) {
            differences += "settings_reset_dialog.png: ${difference.message}"
        }
    }

    private fun stageProgressView(
        stages: List<String>,
        completed: Int,
        heightPixels: Int = 288
    ): ComposeView {
        val width = activity.resources.displayMetrics.widthPixels
        val view = ComposeView(activity).apply {
            setContent {
                StageProgress(
                    stages,
                    completed,
                    Modifier.height((heightPixels / resources.displayMetrics.density).dp)
                )
            }
        }
        activity.setContentView(view, FrameLayout.LayoutParams(width, heightPixels))
        settle()
        assertEquals(width, view.width)
        assertEquals(heightPixels, view.height)
        return view
    }

    @Test
    fun stageProgress() {
        val view = stageProgressView(listOf("Steg", "Wiese", "Allee"), 1)
        val bitmap = Bitmap.createBitmap(view.width, 288, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        bitmap.captureRoboImage(roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Verify))
    }

    @Test
    fun stageProgressVariants() {
        listOf(
            listOf("Steg", "Wiese", "Allee"), listOf("Start"), emptyList(),
            listOf("Tor", "Beete", "Brunnen", "Ufer", "Ziel"),
            listOf("Gemeinschaftsgarten", "Alter Aussichtspunkt", "Flussufer")
        ).forEachIndexed { index, stages ->
            (0..stages.size.coerceAtLeast(1)).forEach { completed ->
                val view = stageProgressView(stages, completed, heightPixels = 2992)
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                bitmap.captureRoboImage(
                    "src/test/snapshots/stages_${index}_$completed.png",
                    roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Verify)
                )
            }
        }
    }
}
