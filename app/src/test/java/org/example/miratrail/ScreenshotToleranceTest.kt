package org.example.miratrail

import com.dropbox.differ.Color
import com.dropbox.differ.Image
import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenshotToleranceTest {
    private fun catalog(): Image {
        val bitmap = ImageIO.read(File("src/test/snapshots/org.example.miratrail.VisualReferenceTest.catalogWithContent.png"))
        return object : Image {
            override val width = bitmap.width
            override val height = bitmap.height
            override fun getPixel(x: Int, y: Int) = Color(bitmap.getRGB(x, y))
        }
    }

    private fun accepts(reference: Image, actual: Image): Boolean {
        return acceptsScreenComparison(screenImageComparator.compare(reference, actual))
    }

    @Test
    fun changedImageDimensionsAreRejected() {
        val reference = catalog()
        val taller = object : Image by reference {
            override val height = reference.height + 1
        }
        assertFalse(accepts(reference, taller))
    }

    @Test
    fun minorColorRoundingIsAccepted() {
        val reference = catalog()
        val rounded = object : Image by reference {
            override fun getPixel(x: Int, y: Int): Color {
                val pixel = reference.getPixel(x, y)
                return pixel.copy(r = (pixel.r + 1f / 255f).coerceAtMost(1f),
                    g = (pixel.g + 1f / 255f).coerceAtMost(1f), b = (pixel.b + 1f / 255f).coerceAtMost(1f))
            }
        }
        assertTrue(accepts(reference, rounded))
    }

    @Test
    fun missingCatalogCardsAreRejected() {
        val reference = catalog()
        val empty = object : Image by reference {
            override fun getPixel(x: Int, y: Int) = if (y >= 1000) Color(234, 247, 250) else reference.getPixel(x, y)
        }
        assertFalse(accepts(reference, empty))
    }

    @Test
    fun onePixelHeadingShiftIsRejected() {
        val reference = catalog()
        val shifted = object : Image by reference {
            override fun getPixel(x: Int, y: Int) = reference.getPixel(
                if (x in 60..1000 && y in 300..400) x - 1 else x, y)
        }
        assertFalse(accepts(reference, shifted))
    }
}
