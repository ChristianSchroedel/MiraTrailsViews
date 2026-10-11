package org.example.miratrail.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import org.example.miratrail.data.WalkRepository

@Composable
internal fun TrailNavDisplay(
    navigation: TrailNavigation,
    repository: WalkRepository,
    model: WalkViewModel
) {
    NavDisplay(
        backStack = navigation.backStack,
        onBack = navigation::goBack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryProvider = { key ->
            NavEntry(key) {
                TrailDestination((key as TrailEntry).route, navigation, repository, model)
            }
        }
    )
}

@Composable
private fun TrailDestination(
    route: TrailRoute,
    navigation: TrailNavigation,
    repository: WalkRepository,
    model: WalkViewModel
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    when (route) {
        TrailRoute.Overview -> {
            val walks = repository.walks.screenValue(lifecycleOwner)
            OverviewScreen(
                summary = "${walks.size} Wege · ${walks.count { it.planned }} geplant · ${walks.count { it.isComplete }} abgeschlossen",
                onNavigate = navigation::navigate
            )
        }

        TrailRoute.Catalog -> {
            LaunchedEffect(Unit) { model.load() }
            CatalogScreen(
                state = model.state.screenValue(lifecycleOwner),
                onQuery = model::setQuery,
                onFavorites = model::setFavoritesOnly,
                onRefresh = model::load,
                onOpen = { navigation.navigate(TrailRoute.Detail(it.id)) },
                onSelect = { model.select(it.id) }
            )
        }

        is TrailRoute.Detail -> {
            val walk = repository.walks.screenValue(lifecycleOwner)
                .firstOrNull { it.id == route.walkId }
            if (walk != null) {
                DetailScreen(
                    walk = walk,
                    onNext = { model.completeNextStage(route.walkId) },
                    onFavorite = { model.toggleFavorite(route.walkId) },
                    onPlan = { model.togglePlanned(route.walkId) },
                    onEdit = { navigation.navigate(TrailRoute.Edit(route.walkId)) }
                )
            }
        }

        TrailRoute.Create, is TrailRoute.Edit -> {
            val walkId = (route as? TrailRoute.Edit)?.walkId ?: 0
            val editor: EditorViewModel = viewModel(
                factory = EditorViewModelFactory(repository, walkId)
            )
            EditorScreen(
                state = editor.state.screenValue(lifecycleOwner),
                editing = walkId != 0,
                onChange = { editor.change(it.title, it.area, it.description, it.note) },
                onSave = {
                    editor.save(walkId)?.let { navigation.navigate(TrailRoute.Detail(it.id)) }
                }
            )
        }

        TrailRoute.Settings -> SettingsScreen(
            model.simulateLoadError,
            model::setSimulateLoadError,
            model::reset
        )

        else -> CollectionDestination(route, navigation, repository)
    }
}

@Composable
private fun CollectionDestination(
    route: TrailRoute,
    navigation: TrailNavigation,
    repository: WalkRepository
) {
    val walks = repository.walks.screenValue(LocalLifecycleOwner.current)
    val collection = when (route) {
        TrailRoute.Favorites -> CollectionContent(
            "Merkliste", "Wege, die du wiederfinden möchtest.",
            "Noch kein Weg gemerkt. Öffne einen Weg und tippe auf Merken."
        )

        TrailRoute.Planned -> CollectionContent(
            "Geplant", "Deine nächsten Spaziergänge.",
            "Noch nichts geplant. Plane einen Weg in seiner Detailansicht."
        )

        TrailRoute.Completed -> CollectionContent(
            "Abgeschlossen", "Wege mit vollständig erledigten Etappen.",
            "Noch kein Weg abgeschlossen."
        )

        TrailRoute.Notes -> CollectionContent(
            "Notizen", "Wege mit persönlichen Beobachtungen.",
            "Noch keine Notiz. Bearbeite einen Weg, um eine zu schreiben."
        )

        else -> error("Not a collection: $route")
    }
    CollectionScreen(
        heading = collection.heading,
        subtitle = collection.subtitle,
        emptyText = collection.emptyText,
        walks = walks.filter {
            when (route) {
                TrailRoute.Favorites -> it.favorite
                TrailRoute.Planned -> it.planned && !it.isComplete
                TrailRoute.Completed -> it.isComplete
                TrailRoute.Notes -> it.note.isNotBlank()
                else -> false
            }
        },
        completed = route == TrailRoute.Completed,
        onOpen = { navigation.navigate(TrailRoute.Detail(it.id)) }
    )
}

private data class CollectionContent(
    val heading: String,
    val subtitle: String,
    val emptyText: String
)
