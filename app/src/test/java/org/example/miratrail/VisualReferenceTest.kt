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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDisplay

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "de-rDE-w448dp-h997dp-notnight-xxhdpi")
class VisualReferenceTest {
    @Before
    fun configurePixel9ProXlDisplay() {
        // 997 dp at 480 dpi rounds to 2991 px; the demo device has 2992 px.
        shadowOf(ShadowDisplay.getDefaultDisplay()).apply {
            setHeight(2992)
            setRealHeight(2992)
        }
    }

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
        val width = activity.resources.displayMetrics.widthPixels
        view.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(288, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, width, 288)
        val bitmap = Bitmap.createBitmap(width, 288, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        bitmap.captureRoboImage()
    }
}
