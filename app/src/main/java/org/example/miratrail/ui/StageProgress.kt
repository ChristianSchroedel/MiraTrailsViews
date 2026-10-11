package org.example.miratrail.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.miratrail.R

/**
 * Stage progress for both the detail screen and compact walk cards.
 * For card segments, use compact = true and Modifier.padding(horizontal = 16.dp),
 * within a WalkCard. No background is drawn.
 */
@Composable
fun StageProgress(
    stages: List<String>,
    completedStages: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val visibleStages = stages.ifEmpty { listOf("Start") }
    val completed = completedStages.coerceIn(0, visibleStages.size)
    val sage = SunnyColors.Sage
    val coral = SunnyColors.Coral
    val ink = SunnyColors.Ink
    val check = painterResource(R.drawable.figma_check)
    val open = painterResource(R.drawable.figma_stage_open)
    val labelPaint =
        remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER } }
    val summary =
        "Etappen: $completed von ${visibleStages.size} abgeschlossen. ${visibleStages.joinToString(", ")}."

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(if (compact) 16.dp else 66.dp)
            .semantics { contentDescription = summary }
    ) {
        if (compact) {
            val gap = 5.dp.toPx()
            val segmentWidth = (size.width - gap * (visibleStages.size - 1)) / visibleStages.size
            visibleStages.indices.forEach { index ->
                drawRoundRect(
                    color = if (index < completed) coral else sage,
                    topLeft = Offset(index * (segmentWidth + gap), 0f),
                    size = Size(segmentWidth, 4.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        } else {
            val left = 41.dp.toPx()
            val right = size.width - 41.dp.toPx()
            val centerY = 15.dp.toPx()
            val step =
                if (visibleStages.size == 1) 0f else (right - left) / (visibleStages.size - 1)
            drawLine(sage, Offset(left, centerY), Offset(right, centerY), strokeWidth = 2.dp.toPx())
            labelPaint.color = ink.toArgb()
            // Use Android Paint font metrics and a 52 dp text baseline.
            labelPaint.textSize = 12f * density * fontScale
            visibleStages.forEachIndexed { index, name ->
                val x = left + step * index
                drawCircle(if (index < completed) coral else sage, 15.dp.toPx(), Offset(x, centerY))
                val halfSize = 8.dp.toPx()
                // Round icon bounds down to integer pixels.
                val iconLeft = (x - halfSize).toInt().toFloat()
                val iconTop = (centerY - halfSize).toInt().toFloat()
                val iconSize = Size(
                    (x + halfSize).toInt() - iconLeft,
                    (centerY + halfSize).toInt() - iconTop
                )
                translate(iconLeft, iconTop) {
                    with(if (index < completed) check else open) { draw(iconSize) }
                }
                drawContext.canvas.nativeCanvas.drawText(name, x, 52.dp.toPx(), labelPaint)
            }
        }
    }
}

@Preview(name = "Etappen", widthDp = 448, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StageProgressPreview() {
    StageProgress(
        listOf("Steg", "Wiese", "Allee"),
        completedStages = 1,
        modifier = Modifier.height(96.dp)
    )
}

@Preview(name = "Abgeschlossen", widthDp = 448, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StageProgressCompletedPreview() {
    StageProgress(
        listOf("Steg", "Wiese", "Allee"),
        completedStages = 3,
        modifier = Modifier.height(96.dp)
    )
}

@Preview(name = "Kompakt", widthDp = 448, showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StageProgressCompactPreview() {
    StageProgress(
        listOf("Steg", "Wiese", "Allee"), completedStages = 1,
        modifier = Modifier.padding(horizontal = 16.dp), compact = true
    )
}
