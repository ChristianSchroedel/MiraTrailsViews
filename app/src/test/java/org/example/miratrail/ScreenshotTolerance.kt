package org.example.miratrail

import com.dropbox.differ.Image
import com.dropbox.differ.ImageComparator
import com.dropbox.differ.Mask
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.RoborazziOptions

/** Screen-only tolerance: about 2,010 of 4,021,248 pixels, without positional matching. */
internal val screenImageComparator = object : ImageComparator {
    private val colors = SimpleImageComparator(maxDistance = 0.01f, hShift = 0, vShift = 0)

    override fun compare(left: Image, right: Image, diff: Mask?): ImageComparator.ComparisonResult {
        if (left.width != right.width || left.height != right.height) {
            val width = maxOf(left.width, right.width)
            val height = maxOf(left.height, right.height)
            return ImageComparator.ComparisonResult(width * height, width * height, width, height)
        }
        return colors.compare(left, right, diff)
    }
}

internal fun acceptsScreenComparison(result: ImageComparator.ComparisonResult) =
    result.pixelDifferences.toDouble() / result.pixelCount <= 0.0005

@OptIn(ExperimentalRoborazziApi::class)
internal fun screenScreenshotOptions() = RoborazziOptions(
    taskType = RoborazziTaskType.Verify,
    compareOptions = RoborazziOptions.CompareOptions(
        imageComparator = screenImageComparator,
        resultValidator = ::acceptsScreenComparison
    )
)
