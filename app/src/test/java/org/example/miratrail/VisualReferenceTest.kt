package org.example.miratrail

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.captureScreenRoboImage
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputLayout
import java.util.concurrent.TimeUnit
import org.example.miratrail.data.Walk
import org.example.miratrail.ui.StageProgress
import org.example.miratrail.ui.WalkViewModel
import org.example.miratrail.ui.WalkViewModelFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowDisplay

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-rDE-w448dp-h997dp-notnight-xxhdpi")
class VisualReferenceTest {
    private val controllers = mutableListOf<ActivityController<MainActivity>>()

    @After
    fun destroyActivities() {
        controllers.asReversed().forEach { it.pause().stop().destroy() }
        controllers.clear()
    }

    @Before
    fun configurePixel9ProXlDisplay() {
        // 997 dp at 480 dpi rounds to 2991 px; the demo device has 2992 px.
        (RuntimeEnvironment.getApplication() as TrailApplication).repository.reset()
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setHeight(2992)
            setRealHeight(2992)
        }
    }

    private fun activity(): MainActivity =
        Robolectric.buildActivity(MainActivity::class.java).setup().also { controllers += it }.get()

    private fun show(activity: MainActivity, destination: Int, arguments: Bundle? = null): View {
        val host =
            activity.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        host.navController.navigate(destination, arguments)
        shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)
        return activity.window.decorView
    }

    @Test
    fun overview() {
        activity().window.decorView.captureRoboImage()
    }

    @Test
    fun catalogWithContent() {
        val activity = activity()
        show(activity, R.id.catalogFragment).captureRoboImage()
    }

    @Test
    fun catalogEmpty() {
        val activity = activity()
        val root = show(activity, R.id.catalogFragment)
        root.findViewById<TextView>(R.id.search).text = "kein passender Weg"
        shadowOf(Looper.getMainLooper()).idle()
        root.captureRoboImage()
    }

    @Test
    fun formValidation() {
        val activity = activity()
        val root = show(activity, R.id.createFragment)
        root.findViewById<View>(R.id.save).performClick()
        shadowOf(Looper.getMainLooper()).idle()
        root.captureRoboImage()
    }

    @Test
    fun stageProgress() {
        val activity = activity()
        val view = stageProgressView(activity, listOf("Steg", "Wiese", "Allee"), 1)
        val width = activity.resources.displayMetrics.widthPixels
        val bitmap = Bitmap.createBitmap(width, 288, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        bitmap.captureRoboImage()
    }

    private fun stageProgressView(activity: MainActivity, stages: List<String>, completed: Int, heightPixels: Int = 288): ComposeView {
        val width = activity.resources.displayMetrics.widthPixels
        val view = ComposeView(activity).apply {
            setContent {
                StageProgress(stages, completed, Modifier.height((heightPixels / resources.displayMetrics.density).dp))
            }
        }
        activity.setContentView(view, FrameLayout.LayoutParams(width, heightPixels))
        settle()
        assertEquals(width, view.width)
        assertEquals(heightPixels, view.height)
        return view
    }

    private fun settle() = shadowOf(Looper.getMainLooper()).idleFor(500, TimeUnit.MILLISECONDS)

    private fun repository() = (RuntimeEnvironment.getApplication() as TrailApplication).repository

    private fun model(activity: MainActivity): WalkViewModel = ViewModelProvider(
        activity, WalkViewModelFactory(repository())
    )[WalkViewModel::class.java]

    /** Keep every screen reference at the fixed Pixel-9-Pro-XL window size. */
    private fun captureScreen(activity: MainActivity, name: String) {
        val root = activity.window.decorView
        fun finishDrawableAnimations(view: View) {
            view.jumpDrawablesToCurrentState()
            if (view is ViewGroup) {
                for (index in 0 until view.childCount) finishDrawableAnimations(view.getChildAt(index))
            }
        }
        finishDrawableAnimations(root)
        assertEquals(1344, root.width)
        assertEquals(2992, root.height)
        root.captureRoboImage("src/test/snapshots/${name}_window.png")
        val host = activity.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val scroll = host.childFragmentManager.primaryNavigationFragment!!.requireView() as ViewGroup
        val content = scroll.getChildAt(0)
        val bottom = (content.height - scroll.height + scroll.paddingTop + scroll.paddingBottom).coerceAtLeast(0)
        if (bottom > 0) {
            scroll.scrollTo(0, bottom)
            settle()
            root.captureRoboImage("src/test/snapshots/${name}_bottom.png")
            scroll.scrollTo(0, 0)
            settle()
        }
    }

    private fun screen(destination: Int, name: String, id: Int = 1) {
        val activity = activity()
        show(activity, destination, Bundle().apply { putInt("walkId", id) })
        captureScreen(activity, name)
    }

    @Test
    fun overviewCompleteContent() = screen(R.id.overviewFragment, "overview_full")
    @Test
    fun catalogCompleteContent() = screen(R.id.catalogFragment, "catalog_full")

    @Test
    fun catalogLoading() {
        val activity = activity()
        val root = show(activity, R.id.catalogFragment)
        root.findViewById<View>(R.id.refresh).performClick()
        shadowOf(Looper.getMainLooper()).idle()
        val loading = root.findViewById<ProgressBar>(R.id.loading)
        assertEquals(View.VISIBLE, loading.visibility)
        assertEquals(View.GONE, root.findViewById<View>(R.id.walks).visibility)
        // Freeze the animation, retaining the actual loading state.
        (loading.indeterminateDrawable as? Animatable)?.stop()
        captureScreen(activity, "catalog_loading")
    }

    @Test
    fun catalogError() {
        val activity = activity()
        show(activity, R.id.settingsFragment).findViewById<MaterialCheckBox>(R.id.simulate_error).isChecked = true
        val root = show(activity, R.id.catalogFragment)
        assertEquals(activity.getString(R.string.load_error), root.findViewById<TextView>(R.id.message).text.toString())
        captureScreen(activity, "catalog_error")
    }

    @Test
    fun catalogFavoritesFilter() {
        val activity = activity()
        show(activity, R.id.catalogFragment).findViewById<MaterialCheckBox>(R.id.favorites_filter).isChecked = true
        settle()
        assertEquals(2, model(activity).state.value.walks.size)
        captureScreen(activity, "catalog_favorites")
    }

    @Test
    fun catalogSearchMatch() {
        val activity = activity()
        show(activity, R.id.catalogFragment).findViewById<TextView>(R.id.search).text = "Nordufer"
        settle()
        assertEquals(1, model(activity).state.value.walks.size)
        captureScreen(activity, "catalog_search_match")
    }

    @Test
    fun catalogFavoritesSearchEmpty() {
        val activity = activity()
        val root = show(activity, R.id.catalogFragment)
        root.findViewById<MaterialCheckBox>(R.id.favorites_filter).isChecked = true
        root.findViewById<TextView>(R.id.search).text = "Südhöhe"
        settle()
        assertEquals(View.VISIBLE, root.findViewById<View>(R.id.message).visibility)
        captureScreen(activity, "catalog_favorites_empty")
    }

    @Test
    fun catalogSelection() {
        val activity = activity()
        val root = show(activity, R.id.catalogFragment)
        val list = root.findViewById<RecyclerView>(R.id.walks)
        assertTrue(list.findViewHolderForAdapterPosition(0)!!.itemView.performLongClick())
        settle()
        assertEquals(View.VISIBLE, root.findViewById<View>(R.id.selection).visibility)
        captureScreen(activity, "catalog_selected")
    }

    private val collections = listOf(
        R.id.favoritesFragment to "favorites", R.id.plannedFragment to "planned",
        R.id.completedFragment to "completed", R.id.notesFragment to "notes"
    )

    @Test
    fun collectionsWithContent() {
        collections.forEach { (destination, name) ->
            val activity = activity()
            val root = show(activity, destination)
            assertEquals(View.GONE, root.findViewById<View>(R.id.empty).visibility)
            captureScreen(activity, "${name}_populated")
        }
    }

    @Test
    fun collectionsEmpty() {
        repository().walks.value.forEach {
            repository().save(it.copy(favorite = false, planned = false, completedStages = 0, note = ""))
        }
        collections.forEach { (destination, name) ->
            val activity = activity()
            val root = show(activity, destination)
            assertEquals(View.VISIBLE, root.findViewById<View>(R.id.empty).visibility)
            assertEquals(0, root.findViewById<RecyclerView>(R.id.walks).adapter!!.itemCount)
            captureScreen(activity, "${name}_empty")
        }
    }

    @Test
    fun detailSeedWalks() {
        (1..5).forEach { id -> screen(R.id.detailFragment, "detail_walk_$id", id) }
    }

    @Test
    fun detailToggleCombinations() {
        listOf(false, true).forEach { favorite ->
            listOf(false, true).forEach { planned ->
                repository().save(repository().find(1)!!.copy(favorite = favorite, planned = planned))
                screen(R.id.detailFragment, "detail_favorite_${favorite}_planned_$planned")
            }
        }
    }

    @Test
    fun detailProgressStates() {
        (0..3).forEach { completed ->
            repository().save(repository().find(1)!!.copy(completedStages = completed))
            val activity = activity()
            val root = show(activity, R.id.detailFragment, Bundle().apply { putInt("walkId", 1) })
            assertEquals(completed < 3, root.findViewById<View>(R.id.next_stage).isEnabled)
            captureScreen(activity, "detail_progress_$completed")
        }
    }

    @Test
    fun detailProgressUpdatesInPlace() {
        val activity = activity()
        val root = show(activity, R.id.detailFragment, Bundle().apply { putInt("walkId", 1) })
        (2..3).forEach { completed ->
            root.findViewById<View>(R.id.next_stage).performClick()
            settle()
            assertEquals("$completed von 3 Etappen abgeschlossen",
                root.findViewById<TextView>(R.id.stage_caption).text.toString())
            assertEquals(completed < 3, root.findViewById<View>(R.id.next_stage).isEnabled)
            // Compare the component area only: scrolling/fading scrollbars are unrelated to recomposition.
            val progress = root.findViewById<ComposeView>(R.id.stage_progress)
            val location = IntArray(2)
            progress.getLocationOnScreen(location)
            val reference = BitmapFactory.decodeFile("src/test/snapshots/detail_progress_${completed}_window.png")
            val expected = Bitmap.createBitmap(reference, location[0], location[1], progress.width, progress.height)
            val derivedReference = java.io.File("build/derived-snapshots/detail_progress_$completed.png")
            derivedReference.parentFile!!.mkdirs()
            java.io.FileOutputStream(derivedReference).use {
                expected.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            // Use the same native capture path as the screen references, rather than a software Canvas.
            progress.captureRoboImage(derivedReference.absolutePath)
            reference.recycle()
            expected.recycle()
        }
    }

    @Test
    fun detailLongContentAndGenericArtwork() {
        val walk = repository().save(Walk(0,
            "Ein langer Spaziergang durch die Gärten und entlang des alten Flussufers",
            "Gemeinschaftsgärten am südlichen Stadtrand",
            "Über schmale Wege geht es durch die Gärten zum Ufer. ".repeat(12),
            listOf("Gartentor", "Flussufer", "Heimweg"),
            note = "Bei Regen ist der Weg rutschig.\nEine Pause am Fluss einplanen.\nAm Gartentor treffen."
        ))
        screen(R.id.detailFragment, "detail_long_content", walk.id)
        val activity = activity()
        show(activity, R.id.catalogFragment).findViewById<TextView>(R.id.search).text = walk.title
        settle()
        captureScreen(activity, "catalog_generic_artwork")
    }

    @Test
    fun createBlank() = screen(R.id.createFragment, "create_blank")

    @Test
    fun createFilled() {
        val activity = activity()
        val root = show(activity, R.id.createFragment)
        root.findViewById<TextView>(R.id.title).text = "Neue Runde am Fluss"
        root.findViewById<TextView>(R.id.area).text = "Südpark"
        root.findViewById<TextView>(R.id.description).text = "Vom Park zum Ufer.\nÜber den Steg zurück."
        root.findViewById<TextView>(R.id.note).text = "Wasser und eine Regenjacke mitnehmen."
        settle()
        captureScreen(activity, "create_filled")
        root.findViewById<View>(R.id.save).performClick()
        settle()
        captureScreen(activity, "create_saved_detail")
    }

    @Test
    fun formErrorVariants() {
        listOf("" to "", "" to "Nordufer", "Ab" to "Nordufer", "Gültiger Name" to "")
            .forEachIndexed { index, (title, area) ->
                val activity = activity()
                val root = show(activity, R.id.createFragment)
                root.findViewById<TextView>(R.id.title).text = title
                root.findViewById<TextView>(R.id.area).text = area
                root.findViewById<View>(R.id.save).performClick()
                settle()
                assertTrue(root.findViewById<TextInputLayout>(R.id.title_layout).error != null ||
                    root.findViewById<TextInputLayout>(R.id.area_layout).error != null)
                captureScreen(activity, "create_validation_$index")
            }
    }

    @Test
    fun editPrefilled() = screen(R.id.editFragment, "edit_prefilled")
    @Test
    fun editWithoutNote() = screen(R.id.editFragment, "edit_without_note", 2)

    @Test
    fun editValidationAndCorrection() {
        val activity = activity()
        val root = show(activity, R.id.editFragment, Bundle().apply { putInt("walkId", 1) })
        root.findViewById<TextView>(R.id.title).text = ""
        root.findViewById<TextView>(R.id.area).text = ""
        root.findViewById<View>(R.id.save).performClick()
        settle()
        captureScreen(activity, "edit_validation")
        root.findViewById<TextView>(R.id.title).text = "Korrigierter Weg"
        root.findViewById<TextView>(R.id.area).text = "Südpark"
        root.findViewById<View>(R.id.save).performClick()
        settle()
        captureScreen(activity, "edit_corrected_detail")
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun settingsStatesAndResetDialog() {
        val activity = activity()
        val root = show(activity, R.id.settingsFragment)
        captureScreen(activity, "settings_default")
        val error = root.findViewById<MaterialCheckBox>(R.id.simulate_error)
        error.isChecked = true
        settle()
        assertTrue(error.isChecked)
        assertTrue(model(activity).simulateLoadError)
        captureScreen(activity, "settings_error_enabled")
        root.findViewById<View>(R.id.reset).performClick()
        settle()
        val dialog = ShadowDialog.getLatestDialog()
        assertTrue(dialog.isShowing)
        captureScreenRoboImage("src/test/snapshots/settings_reset_dialog.png")
    }

    @Test
    fun stageProgressVariants() {
        val activity = activity()
        listOf(
            listOf("Steg", "Wiese", "Allee"), listOf("Start"), emptyList(),
            listOf("Tor", "Beete", "Brunnen", "Ufer", "Ziel"),
            listOf("Gemeinschaftsgarten", "Alter Aussichtspunkt", "Flussufer")
        ).forEachIndexed { index, stages ->
            (0..stages.size.coerceAtLeast(1)).forEach { completed ->
                // The original View capture expanded these variant references to the full display height.
                val view = stageProgressView(activity, stages, completed, heightPixels = 2992)
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                view.draw(Canvas(bitmap))
                bitmap.captureRoboImage("src/test/snapshots/stages_${index}_${completed}.png")
            }
        }
    }

}
