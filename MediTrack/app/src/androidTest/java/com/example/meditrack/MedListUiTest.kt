package com.example.meditrack

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.meditrack.ui.MedListActivity
import org.hamcrest.CoreMatchers.anyOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedListUiTest {

    @Test
    fun showsToolbarAndRecycler() {
        ActivityScenario.launch(MedListActivity::class.java).use {
            onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
            onView(withId(R.id.recycler)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun fab_opensAddEditScreen() {
        ActivityScenario.launch(MedListActivity::class.java).use {
            onView(withId(R.id.fab)).perform(click())
            onView(withId(R.id.edtName)).check(matches(isDisplayed()))
        }
    }

    /**
     * On small screens the items go into overflow.
     * On wider screens they are shown directly on the toolbar.
     * This test accepts BOTH cases.
     */
    @Test
    fun toolbar_hasCalendarAndHistory() {
        ActivityScenario.launch(MedListActivity::class.java).use {
            val ctx = InstrumentationRegistry.getInstrumentation().targetContext

            // 1) try to open overflow – may fail if items are already visible
            try {
                openActionBarOverflowOrOptionsMenu(ctx)
            } catch (e: Exception) {
                // ignore – most likely no overflow button
            }

            // 2) now assert that either a view with text "Calendar" or with id action_calendar exists
            onView(anyOf(withText("Calendar"), withId(R.id.action_calendar)))
                .check(matches(isDisplayed()))

            onView(anyOf(withText("History"), withId(R.id.action_history)))
                .check(matches(isDisplayed()))
        }
    }
}

