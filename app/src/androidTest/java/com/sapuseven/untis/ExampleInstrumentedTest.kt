package com.sapuseven.untis

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
	fun testTakeScreenshot() {
		Screengrab.screenshot("test")
	}
}
