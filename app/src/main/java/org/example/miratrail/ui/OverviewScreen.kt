package org.example.miratrail.ui

import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.miratrail.R

/** Overview content and scrolling are owned by Compose. */
@Composable
fun OverviewScreen(
    summary: String,
    onNavigate: (TrailRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val ink = SunnyColors.Ink
    val medium = SunnySpacing.Medium
    val density = LocalDensity.current
    // BodyText adds 4 dp to the natural font height, rather than using a total of 20 sp.
    val bodyLineHeight = remember(density) {
        with(density) {
            val metrics = Paint().apply { textSize = 16.sp.toPx() }.fontMetricsInt
            (metrics.descent - metrics.ascent + 4.dp.toPx()).toSp()
        }
    }
    val body = TextStyle(
        color = ink, fontFamily = SunnyFonts.Body, fontSize = 16.sp,
        lineHeight = bodyLineHeight, platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
    SunnyPage(modifier) {
        BasicText(
            stringResource(R.string.overview_heading),
            style = body.copy(
                fontFamily = SunnyFonts.Heading,
                fontSize = 30.sp,
                lineHeight = 36.sp
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(medium))
        BasicText(
            stringResource(R.string.overview_description),
            style = body,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(medium))
        SunnyImage(R.drawable.sunny_overview, 180.dp)
        Spacer(Modifier.height(medium))
        BasicText(
            summary,
            style = body.copy(fontSize = 14.sp, lineHeight = 17.sp, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth()
                .background(SunnyColors.Sage, RoundedCornerShape(12.dp))
                .padding(14.dp)
        )
        Spacer(Modifier.height(medium))
        SunnyButton(
            stringResource(R.string.catalog),
            onClick = { onNavigate(TrailRoute.Catalog) })
        val links = listOf(
            OverviewLink(R.string.create, R.drawable.figma_circle_plus, TrailRoute.Create),
            OverviewLink(R.string.favorites, R.drawable.figma_heart, TrailRoute.Favorites),
            OverviewLink(R.string.planned, R.drawable.figma_calendar, TrailRoute.Planned),
            OverviewLink(R.string.completed, R.drawable.figma_circle_check, TrailRoute.Completed),
            OverviewLink(R.string.notes, R.drawable.figma_notebook, TrailRoute.Notes),
            OverviewLink(R.string.settings, R.drawable.figma_settings, TrailRoute.Settings)
        )
        links.forEach { link ->
            Spacer(Modifier.height(SunnySpacing.Small))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Image(
                    painterResource(link.icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                SunnyButton(
                    stringResource(link.label),
                    secondary = true,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(link.destination) })
            }
        }
        SunnyFooter()
    }
}

private data class OverviewLink(val label: Int, val icon: Int, val destination: TrailRoute)

@Preview(
    name = "Übersicht",
    widthDp = 448,
    locale = "de",
    showBackground = true,
    backgroundColor = 0xFFEAF7FA
)
@Composable
private fun OverviewPreview() {
    SunnyTheme { OverviewScreen("5 Wege · 2 geplant · 1 abgeschlossen", onNavigate = {}) }
}
