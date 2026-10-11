package org.example.miratrail.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.miratrail.R
import org.example.miratrail.data.Walk

@Composable
fun CatalogScreen(
    state: CatalogState, onQuery: (String) -> Unit, onFavorites: (Boolean) -> Unit,
    onRefresh: () -> Unit, onOpen: (Walk) -> Unit, onSelect: (Walk) -> Unit
) {
    val gutter = dimensionResource(R.dimen.page_gutter)
    LazyColumn(
        Modifier.fillMaxSize().background(colorResource(R.color.cream)).imePadding(),
        contentPadding = PaddingValues(start = gutter, end = gutter, top = 8.dp)
    ) {
        item(key = "header") {
            Column {
                SunnyText(stringResource(R.string.catalog_heading), SunnyTextStyle.PageTitle)
                Spacer(Modifier.height(16.dp))
                SunnyInput(
                    state.query,
                    onQuery,
                    stringResource(R.string.search_hint),
                    capitalize = false
                )
                Spacer(Modifier.height(16.dp))
                SunnyCheckbox(
                    state.favoritesOnly,
                    stringResource(R.string.only_favorites),
                    onFavorites
                )
                Spacer(Modifier.height(16.dp))
                SunnyText("${state.walks.size} passende Wege")
                val selected = state.walks.firstOrNull { it.id == state.selectedId }
                if (state.phase == CatalogPhase.CONTENT && selected != null) {
                    Spacer(Modifier.height(16.dp))
                    SunnyText(
                        "Ausgewählt: ${selected.title}. Antippen öffnet Details.",
                        padding = 12.dp, background = colorResource(R.color.sage)
                    )
                }
                when (state.phase) {
                    CatalogPhase.LOADING -> Box(
                        Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            Modifier.size(48.dp).semantics { contentDescription = "Wird geladen" },
                            color = colorResource(R.color.coral)
                        )
                    }

                    CatalogPhase.EMPTY, CatalogPhase.ERROR -> SunnyText(
                        stringResource(if (state.phase == CatalogPhase.ERROR) R.string.load_error else R.string.empty_list),
                        padding = 16.dp
                    )

                    CatalogPhase.CONTENT -> Spacer(Modifier.height(16.dp))
                }
            }
        }
        if (state.phase == CatalogPhase.CONTENT) {
            items(state.walks, key = { it.id }) { walk ->
                WalkCard(walk, onOpen, onSelect)
                Spacer(Modifier.height(16.dp))
            }
        }
        item(key = "footer") {
            // XML gave the final card and the refresh button their own 16 dp margins.
            Spacer(Modifier.height(16.dp))
            SunnyButton(stringResource(R.string.refresh), onRefresh, secondary = true)
            SunnyFooter()
        }
    }
}

@Composable
fun CollectionScreen(
    heading: String, subtitle: String, emptyText: String, walks: List<Walk>,
    completed: Boolean, onOpen: (Walk) -> Unit
) {
    val gutter = dimensionResource(R.dimen.page_gutter)
    LazyColumn(
        Modifier.fillMaxSize().background(colorResource(R.color.cream)),
        contentPadding = PaddingValues(start = gutter, end = gutter, top = 8.dp)
    ) {
        item(key = "header") {
            Column {
                SunnyText(heading, SunnyTextStyle.PageTitle)
                Spacer(Modifier.height(16.dp))
                SunnyText(subtitle)
                if (completed && walks.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        Modifier.fillMaxWidth().height(132.dp)
                            .background(colorResource(R.color.sage), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painterResource(R.drawable.figma_badge_check),
                            contentDescription = null,
                            modifier = Modifier.size(76.dp)
                        )
                    }
                }
                if (walks.isEmpty()) SunnyText(emptyText, padding = 16.dp)
                Spacer(Modifier.height(16.dp))
            }
        }
        items(walks, key = { it.id }) { walk ->
            WalkCard(walk, onOpen, collectionCard = true)
            Spacer(Modifier.height(16.dp))
        }
        item(key = "footer") { SunnyFooter() }
    }
}

@Composable
internal fun WalkCard(
    walk: Walk, onOpen: (Walk) -> Unit, onSelect: ((Walk) -> Unit)? = null,
    collectionCard: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(20.dp), color = colorResource(R.color.surface),
        border = BorderStroke(1.dp, colorResource(R.color.card_outline)),
        modifier = Modifier.fillMaxWidth().combinedClickable(
            role = Role.Button, onClickLabel = "Details öffnen",
            onClick = { onOpen(walk) },
            onLongClickLabel = if (onSelect != null) "Weg auswählen" else null,
            onLongClick = onSelect?.let { select -> { select(walk) } }
        )) {
        Column {
            SunnyImage(
                if (collectionCard) WalkIllustrations.collection(walk.id) else WalkIllustrations.catalog(
                    walk.id
                ),
                if (collectionCard) 120.dp else 76.dp
            )
            Column(Modifier.fillMaxWidth().heightIn(min = 84.dp).padding(16.dp)) {
                SunnyText(walk.title, SunnyTextStyle.CardTitle)
                Spacer(Modifier.height(8.dp))
                SunnyText(
                    "${walk.area} · ${walk.completedStages}/${walk.stages.size} Etappen",
                    SunnyTextStyle.Caption, color = colorResource(R.color.muted)
                )
            }
            StageProgress(
                walk.stages, walk.completedStages,
                Modifier.padding(horizontal = 16.dp).clearAndSetSemantics {}, compact = true
            )
        }
    }
}

private fun previewWalks() = listOf(
    Walk(
        1,
        "Am kleinen Fluss",
        "Nordufer",
        "Ein Spaziergang am Fluss.",
        listOf("Steg", "Wiese", "Allee"),
        completedStages = 1
    )
)

@Preview(name = "Alle Wege", widthDp = 448, locale = "de")
@Composable
private fun CatalogPreview() = SunnyTheme {
    CatalogScreen(
        CatalogState(walks = previewWalks(), phase = CatalogPhase.CONTENT),
        {},
        {},
        {},
        {},
        {})
}

@Preview(name = "Merkliste", widthDp = 448, locale = "de")
@Composable
private fun FavoritesPreview() = SunnyTheme {
    CollectionScreen(
        "Merkliste",
        "Wege, die du wiederfinden möchtest.",
        "",
        previewWalks(),
        false,
        {})
}

@Preview(name = "Geplant", widthDp = 448, locale = "de")
@Composable
private fun PlannedPreview() = SunnyTheme {
    CollectionScreen(
        "Geplant",
        "Deine nächsten Spaziergänge.",
        "",
        previewWalks(),
        false,
        {})
}

@Preview(name = "Abgeschlossen", widthDp = 448, locale = "de")
@Composable
private fun CompletedPreview() = SunnyTheme {
    CollectionScreen(
        "Abgeschlossen",
        "Wege mit vollständig erledigten Etappen.",
        "",
        previewWalks().map { it.copy(completedStages = 3) },
        true,
        {})
}

@Preview(name = "Notizen", widthDp = 448, locale = "de")
@Composable
private fun NotesPreview() = SunnyTheme {
    CollectionScreen(
        "Notizen",
        "Wege mit persönlichen Beobachtungen.",
        "",
        previewWalks(),
        false,
        {})
}
