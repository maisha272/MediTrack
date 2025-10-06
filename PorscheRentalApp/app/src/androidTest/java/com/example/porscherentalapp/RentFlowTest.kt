package com.example.porscherentalapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RentFlowTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun resetSession() {
        rule.scenario.onActivity {
            // deterministic start
            Session.balance = 500
            CarRepository.cars.clear()
            CarRepository.cars.addAll(listOf(
                Car("Porsche","911 Carrera",2020,4.5f,15000,237,R.color.goldYellow), // first
                Car("Porsche","Panamera",   2021,4.8f,10000,280,R.color.royalBlue),
                Car("Porsche","Taycan",     2022,4.6f, 8000,320,R.color.silverGrey),
                Car("Porsche","Macan",      2018,4.0f,30000,180,R.color.metallicGold),
                Car("Porsche","918 Spyder", 2023,4.9f, 5000,400,R.color.burgundy)
            ))
        }
    }

    @Test
    fun rentOnce_updatesCreditsLabel() {
        // Open Rent for first car (911 → 237/day) and confirm
        onView(withId(R.id.btnRent)).perform(click())
        onView(withId(R.id.btnSave)).perform(click())

        // Expect 500 - 237 = 263
        onView(withId(R.id.tvBalance)).check(matches(withText("Credits: 263")))
    }
}
