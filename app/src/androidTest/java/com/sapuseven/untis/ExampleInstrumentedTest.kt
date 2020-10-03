package com.sapuseven.untis

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.NavigationViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.sapuseven.untis.activities.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

class ExampleInstrumentedTest {
	@Rule
	@JvmField
	val localeTestRule = LocaleTestRule()

	@get:Rule
	var activityRule = ActivityScenarioRule(MainActivity::class.java)

	@Before
	fun init() {
		Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
	}

	@Test
	fun screenshot1() {
		onView(withId(R.id.content_main)).check(matches(isDisplayed()))

		Screengrab.screenshot("1")
	}

	@Test
	fun screenshot3() {
		onView(withId(R.id.drawer_layout)).check(matches(isClosed())).perform(DrawerActions.open())
		onView(withId(R.id.navigationview_main)).perform(NavigationViewActions.navigateTo(R.id.nav_free_rooms))

		onView(withId(R.id.content_roomfinder)).check(matches(isDisplayed()))

		Screengrab.screenshot("3")
	}
}
