package org.example.miratrail

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.navigation.fragment.NavHostFragment
import com.github.takahirom.roborazzi.captureRoboImage
import java.util.concurrent.TimeUnit
import org.example.miratrail.ui.StageProgressView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-rDE-w360dp-h800dp-notnight-xxhdpi")
class VisualReferenceTest {
    private fun activity(): MainActivity =
        Robolectric.buildActivity(MainActivity::class.java).setup().get()

    private fun show(activity: MainActivity, destination: Int): View {
        val host =
            activity.supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        host.navController.navigate(destination)
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
    fun detail() {
        show(activity(), R.id.detailFragment).captureRoboImage()
    }

    @Test
    fun create() {
        show(activity(), R.id.createFragment).captureRoboImage()
    }

    @Test
    fun edit() {
        show(activity(), R.id.editFragment).captureRoboImage()
    }

    @Test
    fun favorites() {
        show(activity(), R.id.favoritesFragment).captureRoboImage()
    }

    @Test
    fun planned() {
        show(activity(), R.id.plannedFragment).captureRoboImage()
    }

    @Test
    fun completed() {
        show(activity(), R.id.completedFragment).captureRoboImage()
    }

    @Test
    fun notes() {
        show(activity(), R.id.notesFragment).captureRoboImage()
    }

    @Test
    fun settings() {
        show(activity(), R.id.settingsFragment).captureRoboImage()
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
        val view = StageProgressView(activity)
        view.show(listOf("Steg", "Wiese", "Allee"), 1)
        view.measure(
            View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(288, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, 1080, 288)
        val bitmap = Bitmap.createBitmap(1080, 288, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        bitmap.captureRoboImage()
    }
}
