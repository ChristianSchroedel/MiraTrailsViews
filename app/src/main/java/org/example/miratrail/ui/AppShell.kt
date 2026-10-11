package org.example.miratrail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.miratrail.R

/** Owns system, cutout and IME insets once for the entire Compose app. */
@Composable
internal fun AppShell(
    route: TrailRoute,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    insets: WindowInsets = WindowInsets.safeDrawing,
    content: @Composable () -> Unit
) {
    Column(
        modifier
            .fillMaxSize()
            .background(SunnyColors.Cream)
            .windowInsetsPadding(insets)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .background(SunnyColors.Sky)
                .padding(start = SunnySpacing.PageGutter),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                stringResource(R.string.design_direction),
                style = TextStyle(
                    color = SunnyColors.Ink,
                    fontFamily = SunnyFonts.Semibold,
                    fontSize = 10.sp,
                    letterSpacing = 0.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = true)
                )
            )
        }
        AppToolbar(route, onBack)
        Box(Modifier.fillMaxWidth().weight(1f)) { content() }
    }
}

@Composable
private fun AppToolbar(route: TrailRoute, onBack: () -> Unit) {
    val showBack = route != TrailRoute.Overview
    Box(Modifier.fillMaxWidth().height(58.dp)) {
        Text(
            stringResource(route.title),
            style = TextStyle(
                color = SunnyColors.Ink,
                fontFamily = SunnyFonts.Semibold,
                fontSize = 16.sp,
                letterSpacing = 0.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                // Match the former toolbar title baseline after vertical centering.
                .offset(y = 0.5.dp)
                .padding(start = if (showBack) 72.dp else 16.dp, end = 68.dp)
        )
        if (showBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).size(56.dp)
            ) {
                Icon(
                    painterResource(R.drawable.figma_back),
                    contentDescription = stringResource(R.string.navigate_back),
                    tint = SunnyColors.Ink,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Image(
            painterResource(R.drawable.figma_sun),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = SunnySpacing.PageGutter)
                .size(20.dp)
        )
    }
}

@Preview(name = "App-Shell: Übersicht", widthDp = 448, heightDp = 997, locale = "de")
@Composable
private fun OverviewShellPreview() {
    SunnyTheme {
        AppShell(TrailRoute.Overview, onBack = {}, insets = WindowInsets(0, 0, 0, 0)) {
            OverviewScreen("5 Wege · 2 geplant · 1 abgeschlossen", onNavigate = {})
        }
    }
}

@Preview(name = "App-Shell: Formular", widthDp = 448, heightDp = 997, locale = "de")
@Composable
private fun EditorShellPreview() {
    SunnyTheme {
        AppShell(TrailRoute.Create, onBack = {}, insets = WindowInsets(0, 0, 0, 0)) {
            EditorScreen(EditorState(), editing = false, onChange = {}, onSave = {})
        }
    }
}
