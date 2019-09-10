package com.sapuseven.untis

import com.sapuseven.untis.activities.LoginActivity
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityInstrumentedTest {
	/*@Test
	fun useAppContext() {
		// Context of the app under test.
		val appContext = InstrumentationRegistry.getTargetContext()
		assertEquals("com.sapuseven.untis", appContext.packageName)
	}*/

	@get:Rule
	val loginActivityTestRule = ActivityTestRule(LoginActivity::class.java)

	@Test
	fun automatedDebug() {
		onView(withId(R.id.button_login_manual_data_input))
				.perform(click())

		onView(withId(R.id.button_logindatainput_login))
				.perform(click())
	}

	@Test
	fun focusSearchField_changesView() {
		onView(withId(R.id.textview_login_welcome))
				.check(matches(isDisplayed()))
		onView(withId(R.id.textview_login_please_log_in))
				.check(matches(isDisplayed()))
		onView(withId(R.id.button_login_scan_code))
				.check(matches(isDisplayed()))
		onView(withId(R.id.button_login_manual_data_input))
				.check(matches(isDisplayed()))
		onView(withId(R.id.recyclerview_login_search_results))
				.check(matches(not(isDisplayed())))
		onView(withId(R.id.textview_login_search_message))
				.check(matches(not(isDisplayed())))
		onView(withId(R.id.progressbar_login_search_loading))
				.check(matches(not(isDisplayed())))

		onView(withId(R.id.edittext_login_search))
				.perform(click())

		onView(withId(R.id.textview_login_welcome))
				.check(matches(not(isDisplayed())))
		onView(withId(R.id.textview_login_please_log_in))
				.check(matches(not(isDisplayed())))
		onView(withId(R.id.button_login_scan_code))
				.check(matches(not(isDisplayed())))
		onView(withId(R.id.button_login_manual_data_input))
				.check(matches(not(isDisplayed())))
		onView(withId(R.id.recyclerview_login_search_results))
				.check(matches(isDisplayed()))
		onView(withId(R.id.textview_login_search_message))
				.check(matches(isDisplayed()))
		onView(withId(R.id.progressbar_login_search_loading))
				.check(matches(not(isDisplayed())))
	}
}
