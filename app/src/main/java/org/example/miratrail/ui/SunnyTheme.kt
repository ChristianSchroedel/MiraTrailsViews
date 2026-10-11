package org.example.miratrail.ui

import android.graphics.Paint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.miratrail.R

internal object SunnyColors {
    val Ink = Color(0xFF123B47)
    val Sage = Color(0xFFDCECC6)
    val Cream = Color(0xFFEAF7FA)
    val Muted = Color(0xFF586A63)
    val Outline = Color(0xFFBBD4D8)
    val Coral = Color(0xFFE65F4C)
    val Sky = Color(0xFFBDE8F1)
    val Surface = Color.White
    val CardOutline = Color(0xFFD1E4E4)
    val ButtonOutline = Color(0xFFC7DBDD)
    val ButtonDisabled = Color(0xFFB8C8CD)
    val Hint = Color(0xFF738B95)
    val InputOutline = Color(0xFF999999)
    val Error = Color(0xFFB00020)
}

internal object SunnySpacing {
    val Small = 8.dp
    val Medium = 16.dp
    val PageGutter = 20.dp
}

internal object SunnyFonts {
    val Body = FontFamily.SansSerif
    val Heading = FontFamily(Font(R.font.inter_bold))
    val Semibold = FontFamily(Font(R.font.inter_semibold))
}

private val SunnyColorScheme = lightColorScheme(
    primary = SunnyColors.Coral,
    onPrimary = SunnyColors.Surface,
    background = SunnyColors.Cream,
    onBackground = SunnyColors.Ink,
    surface = SunnyColors.Surface,
    onSurface = SunnyColors.Ink,
    onSurfaceVariant = SunnyColors.Muted,
    outline = SunnyColors.Outline,
    error = SunnyColors.Error
)

@Composable
internal fun SunnyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SunnyColorScheme,
        typography = Typography(
            bodyLarge = sunnyTextStyle(color = Color.Unspecified),
            bodyMedium = sunnyTextStyle(SunnyTextStyle.Caption, color = Color.Unspecified),
            titleLarge = sunnyTextStyle(SunnyTextStyle.PageTitle, color = Color.Unspecified),
            titleMedium = sunnyTextStyle(SunnyTextStyle.CardTitle, color = Color.Unspecified),
            titleSmall = sunnyTextStyle(SunnyTextStyle.SectionTitle, color = Color.Unspecified)
        ),
        content = content
    )
}

internal enum class SunnyTextStyle {
    Body,
    Caption,
    PageTitle,
    CardTitle,
    SectionTitle
}

@Composable
internal fun sunnyTextStyle(
    style: SunnyTextStyle = SunnyTextStyle.Body,
    color: Color = SunnyColors.Ink
): TextStyle {
    val size = when (style) {
        SunnyTextStyle.Body, SunnyTextStyle.SectionTitle -> 16.sp
        SunnyTextStyle.Caption -> 14.sp
        SunnyTextStyle.CardTitle -> 17.sp
        SunnyTextStyle.PageTitle -> 30.sp
    }
    val heading = style in listOf(
        SunnyTextStyle.PageTitle,
        SunnyTextStyle.CardTitle,
        SunnyTextStyle.SectionTitle
    )
    val density = LocalDensity.current
    val lineHeight = remember(size, density, heading, style) {
        with(density) {
            val metrics = Paint().apply { textSize = size.toPx() }.fontMetricsInt
            (metrics.descent - metrics.ascent + if (style == SunnyTextStyle.Body) 4.dp.toPx() else 0f).toSp()
        }
    }
    return TextStyle(
        color = color,
        fontSize = size,
        lineHeight = lineHeight,
        fontFamily = if (heading) SunnyFonts.Heading else SunnyFonts.Body,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
}

