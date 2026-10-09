package org.example.miratrail

import android.os.SystemClock
import android.view.View
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Matcher
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationAndFlowsTest {
    @get:Rule
    val activity = ActivityScenarioRule(MainActivity::class.java)

    private fun openLoadedCatalog() {
        onView(withId(R.id.open_catalog)).perform(click())
        onView(isRoot()).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> = isRoot()
            override fun getDescription() = "wait for the catalog content"
            override fun perform(uiController: UiController, view: View) {
                val deadline = SystemClock.uptimeMillis() + 5000
                while ((view.findViewById<RecyclerView>(R.id.walks)?.let {
                        it.isShown && (it.adapter?.itemCount ?: 0) > 0
                    } != true) && SystemClock.uptimeMillis() < deadline) {
                    uiController.loopMainThreadForAtLeast(50)
                }
                check(view.findViewById<RecyclerView>(R.id.walks)?.adapter?.itemCount ?: 0 > 0) { "Catalog did not show content" }
            }
        })
    }

    private fun openWalk(position: Int) {
        onView(withId(R.id.walks)).perform(object : ViewAction {
            override fun getConstraints(): Matcher<View> =
                isAssignableFrom(RecyclerView::class.java)

            override fun getDescription() = "open walk at position $position"
            override fun perform(uiController: UiController, view: View) {
                val list = view as RecyclerView
                list.scrollToPosition(position)
                uiController.loopMainThreadUntilIdle()
                checkNotNull(list.findViewHolderForAdapterPosition(position)).itemView.performClick()
            }
        })
    }

    @Test
    fun allOverviewDestinationsOpen() {
        val destinations = listOf(
            R.id.open_catalog to R.id.search,
            R.id.open_create to R.id.title_layout,
            R.id.open_favorites to R.id.heading,
            R.id.open_planned to R.id.heading,
            R.id.open_completed to R.id.heading,
            R.id.open_notes to R.id.heading,
            R.id.open_settings to R.id.simulate_error
        )
        destinations.forEach { (button, evidence) ->
            onView(withId(button)).perform(click())
            onView(withId(evidence)).check(matches(isDisplayed()))
            pressBack()
        }
    }

    @Test
    fun catalogOpensDetailsAndEditor() {
        openLoadedCatalog()
        openWalk(0)
        onView(withId(R.id.stage_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.edit)).perform(click())
        onView(withId(R.id.title)).check(matches(withText("Am kleinen Fluss")))
    }

    @Test
    fun validationIsVisibleAndValidWalkCanBeSaved() {
        onView(withId(R.id.open_create)).perform(click())
        onView(withId(R.id.save)).perform(click())
        onView(withText("Bitte einen Namen eingeben.")).check(matches(isDisplayed()))
        onView(withId(R.id.title)).perform(replaceText("Neue Runde"))
        onView(withId(R.id.area)).perform(replaceText("Südpark"))
        onView(withId(R.id.save)).perform(click())
        onView(withText("Neue Runde")).check(matches(isDisplayed()))
    }

    @Test
    fun completedWalkDisablesNextStage() {
        openLoadedCatalog()
        openWalk(3)
        onView(withId(R.id.next_stage)).check(matches(not(isEnabled())))
    }

    @Test
    fun searchCanShowEmptyState() {
        openLoadedCatalog()
        onView(withId(R.id.search)).perform(replaceText("unbekannter Ort"))
        onView(withId(R.id.message)).check(matches(withText(R.string.empty_list)))
    }

    @Test
    fun stageViewHasAccessibleSummary() {
        openLoadedCatalog()
        openWalk(0)
        onView(withId(R.id.stage_progress)).check(matches(isDisplayed()))
        onView(withId(R.id.stage_progress)).check(
            matches(
                androidx.test.espresso.matcher.ViewMatchers.withContentDescription(
                    "Etappen: 1 von 3 abgeschlossen. Alter Steg, Wiese, Baumallee."
                )
            )
        )
    }
}
