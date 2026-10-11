package org.example.miratrail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import org.example.miratrail.R
import org.example.miratrail.data.Walk

@Composable
fun DetailScreen(
    walk: Walk,
    onNext: () -> Unit,
    onFavorite: () -> Unit,
    onPlan: () -> Unit,
    onEdit: () -> Unit
) {
    SunnyPage {
        SunnyText(walk.title, SunnyTextStyle.PageTitle)
        Spacer(Modifier.height(16.dp))
        SunnyText(walk.area, SunnyTextStyle.Caption, color = colorResource(R.color.muted))
        Spacer(Modifier.height(16.dp))
        SunnyImage(WalkIllustrations.detail(walk.id), 176.dp)
        Spacer(Modifier.height(16.dp))
        SunnyText(walk.description)
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.surface), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            SunnyText(stringResource(R.string.stages), SunnyTextStyle.SectionTitle)
            Spacer(Modifier.height(16.dp))
            StageProgress(walk.stages, walk.completedStages)
            Spacer(Modifier.height(16.dp))
            SunnyText(
                "${walk.completedStages} von ${walk.stages.size} Etappen abgeschlossen",
                SunnyTextStyle.Caption
            )
        }
        Spacer(Modifier.height(16.dp))
        SunnyText(
            if (walk.note.isBlank()) "Noch keine Notiz." else "Notiz: ${walk.note}",
            padding = 16.dp, background = colorResource(R.color.sage)
        )
        Spacer(Modifier.height(16.dp))
        SunnyButton(stringResource(R.string.next_stage), onNext, enabled = !walk.isComplete)
        Spacer(Modifier.height(16.dp))
        SunnyButton(
            if (walk.favorite) "Aus Merkliste entfernen" else "Merken",
            onFavorite,
            secondary = true
        )
        Spacer(Modifier.height(16.dp))
        SunnyButton(if (walk.planned) "Planung aufheben" else "Planen", onPlan, secondary = true)
        Spacer(Modifier.height(16.dp))
        SunnyButton(stringResource(R.string.edit), onEdit, secondary = true)
        SunnyFooter()
    }
}

data class EditorInput(
    val title: String,
    val area: String,
    val description: String,
    val note: String
)

@Composable
fun EditorScreen(
    state: EditorState,
    editing: Boolean,
    onChange: (EditorInput) -> Unit,
    onSave: () -> Unit
) {
    val input = EditorInput(state.title, state.area, state.description, state.note)
    SunnyPage {
        SunnyText(
            stringResource(if (editing) R.string.edit else R.string.create),
            SunnyTextStyle.PageTitle
        )
        Spacer(Modifier.height(16.dp))
        SunnyImage(
            if (editing) R.drawable.sunny_edit else R.drawable.sunny_create,
            if (editing) 100.dp else 120.dp
        )
        Spacer(Modifier.height(16.dp))
        SunnyInput(
            state.title,
            { onChange(input.copy(title = it)) },
            stringResource(R.string.route_name),
            state.titleError
        )
        Spacer(Modifier.height(16.dp))
        SunnyInput(
            state.area,
            { onChange(input.copy(area = it)) },
            stringResource(R.string.route_area),
            state.areaError
        )
        Spacer(Modifier.height(16.dp))
        SunnyInput(
            state.description,
            { onChange(input.copy(description = it)) },
            stringResource(R.string.route_description),
            minLines = 3,
            multiline = true
        )
        Spacer(Modifier.height(16.dp))
        SunnyInput(
            state.note,
            { onChange(input.copy(note = it)) },
            stringResource(R.string.route_note),
            multiline = true
        )
        Spacer(Modifier.height(16.dp))
        SunnyButton(stringResource(R.string.save), onSave)
        SunnyFooter()
    }
}

@Composable
fun SettingsScreen(initialError: Boolean, onErrorChanged: (Boolean) -> Unit, onReset: () -> Unit) {
    var error by rememberSaveable { mutableStateOf(initialError) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    SunnyPage {
        SunnyText(stringResource(R.string.settings_heading), SunnyTextStyle.PageTitle)
        Spacer(Modifier.height(16.dp))
        SunnyImage(R.drawable.sunny_settings, 136.dp)
        Spacer(Modifier.height(16.dp))
        SunnyText(
            stringResource(R.string.settings_data_description),
            padding = 18.dp,
            background = colorResource(R.color.sage)
        )
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.surface), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            SunnyCheckbox(error, stringResource(R.string.simulate_error)) { checked ->
                error = checked
                onErrorChanged(checked)
            }
            Spacer(Modifier.height(10.dp))
            SunnyText(stringResource(R.string.settings_error_description))
        }
        Spacer(Modifier.height(16.dp))
        SunnyButton(stringResource(R.string.reset), { confirmReset = true })
        SunnyFooter()
    }
    if (confirmReset) {
        Dialog(
            onDismissRequest = { confirmReset = false }
        ) {
            val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window
            SideEffect { dialogWindow.setDimAmount(0.32f) }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = colorResource(R.color.surface),
                shadowElevation = 8.dp,
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp)) {
                        Text(stringResource(R.string.reset), style = sunnyTextStyle())
                        Spacer(Modifier.height(11.dp))
                        Text(
                            stringResource(R.string.reset_confirm),
                            style = sunnyTextStyle(
                                SunnyTextStyle.Caption,
                                Color(0xFF738B95)
                            )
                        )
                    }
                    Spacer(Modifier.height(33.dp))
                    Row(
                        Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ResetDialogButton(stringResource(R.string.cancel)) {
                            confirmReset = false
                        }
                        Spacer(Modifier.width(8.dp))
                        ResetDialogButton(stringResource(R.string.reset)) {
                            onReset()
                            error = false
                            confirmReset = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetDialogButton(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.height(48.dp)
    ) {
        Text(
            label.uppercase(java.util.Locale.getDefault()),
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            letterSpacing = 0.sp
        )
    }
}

@Preview(name = "Details", widthDp = 448, locale = "de")
@Composable
private fun DetailPreview() = SunnyTheme {
    DetailScreen(
        Walk(
            1,
            "Am kleinen Fluss",
            "Nordufer",
            "Ein Spaziergang am Fluss.",
            listOf("Steg", "Wiese", "Allee"),
            completedStages = 1
        ),
        {},
        {},
        {},
        {}
    )
}

@Preview(name = "Neuer Weg", widthDp = 448, locale = "de")
@Composable
private fun CreatePreview() = SunnyTheme {
    var state by remember { mutableStateOf(EditorState()) }
    EditorScreen(
        state,
        false,
        { state = EditorState(it.title, it.area, it.description, it.note) },
        {}
    )
}

@Preview(name = "Weg bearbeiten", widthDp = 448, locale = "de")
@Composable
private fun EditPreview() = SunnyTheme {
    var state by remember {
        mutableStateOf(
            EditorState(
                "Am kleinen Fluss",
                "Nordufer",
                "Ein Spaziergang am Fluss."
            )
        )
    }
    EditorScreen(
        state,
        true,
        { state = EditorState(it.title, it.area, it.description, it.note) },
        {}
    )
}

@Preview(name = "Einstellungen", widthDp = 448, locale = "de")
@Composable
private fun SettingsPreview() = SunnyTheme { SettingsScreen(false, {}, {}) }
