package org.example.miratrail.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import java.util.UUID
import kotlinx.serialization.Serializable
import org.example.miratrail.R

@Serializable
sealed interface TrailRoute : NavKey {
    @Serializable
    data object Overview : TrailRoute
    @Serializable
    data object Catalog : TrailRoute
    @Serializable
    data class Detail(val walkId: Int = 1) : TrailRoute
    @Serializable
    data object Create : TrailRoute
    @Serializable
    data class Edit(val walkId: Int = 1) : TrailRoute
    @Serializable
    data object Favorites : TrailRoute
    @Serializable
    data object Planned : TrailRoute
    @Serializable
    data object Completed : TrailRoute
    @Serializable
    data object Notes : TrailRoute
    @Serializable
    data object Settings : TrailRoute
}

/** Identical routes can occur more than once, with independent state and ViewModels. */
@Serializable
internal data class TrailEntry(val route: TrailRoute, val id: String) : NavKey

class TrailNavigation internal constructor(internal val backStack: NavBackStack<NavKey>) {
    val currentRoute: TrailRoute get() = (backStack.last() as TrailEntry).route
    val canGoBack: Boolean get() = backStack.size > 1

    fun navigate(route: TrailRoute) {
        backStack.add(TrailEntry(route, UUID.randomUUID().toString()))
    }

    fun goBack() {
        if (canGoBack) backStack.removeAt(backStack.lastIndex)
    }
}

@Composable
internal fun rememberTrailNavigation(): TrailNavigation {
    val backStack = rememberNavBackStack(TrailEntry(TrailRoute.Overview, "overview"))
    return remember(backStack) { TrailNavigation(backStack) }
}

@get:StringRes
internal val TrailRoute.title: Int
    get() = when (this) {
        TrailRoute.Overview -> R.string.overview
        TrailRoute.Catalog -> R.string.catalog
        is TrailRoute.Detail -> R.string.detail
        TrailRoute.Create -> R.string.create
        is TrailRoute.Edit -> R.string.edit
        TrailRoute.Favorites -> R.string.favorites
        TrailRoute.Planned -> R.string.planned
        TrailRoute.Completed -> R.string.completed
        TrailRoute.Notes -> R.string.notes
        TrailRoute.Settings -> R.string.settings
    }
