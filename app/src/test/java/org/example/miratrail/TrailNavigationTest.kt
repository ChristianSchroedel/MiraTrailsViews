package org.example.miratrail

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import org.example.miratrail.ui.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TrailNavigationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun savedBackStackRetainsArgumentsEntryIdentityAndBackOrder() {
        lateinit var navigation: TrailNavigation
        val restoration = StateRestorationTester(compose)
        restoration.setContent {
            val state = rememberTrailNavigation()
            SideEffect { navigation = state }
        }
        val routes = listOf(
            TrailRoute.Catalog, TrailRoute.Detail(4), TrailRoute.Edit(4),
            TrailRoute.Create, TrailRoute.Create
        )
        compose.runOnIdle { routes.forEach(navigation::navigate) }
        val entries = compose.runOnIdle { navigation.backStack.toList() }
        assertEquals(entries.size, entries.toSet().size)
        restoration.emulateSavedInstanceStateRestore()
        compose.runOnIdle {
            assertEquals(entries, navigation.backStack.toList())
            routes.asReversed().forEach { route ->
                assertEquals(route, navigation.currentRoute)
                navigation.goBack()
            }
            assertEquals(TrailRoute.Overview, navigation.currentRoute)
            assertFalse(navigation.canGoBack)
            navigation.goBack()
            assertEquals(TrailRoute.Overview, navigation.currentRoute)
        }
    }
}
