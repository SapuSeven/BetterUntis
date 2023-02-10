package com.sapuseven.untis.screenshots

import android.graphics.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.activities.*
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.config.globalDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.mocks.MOCK_USER_ID
import com.sapuseven.untis.mocks.userMock
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.preferences.materialColors
import com.sapuseven.untis.utils.*
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class MainActivityScreenshot {
	@get:Rule
	val rule = createAndroidComposeRule<BaseComposeActivity>()

	@Before
	fun setupUserDatabase() {
		val masterData = runBlocking { loadMasterData(SCREENSHOT_API_URL) }

		assertNotNull(masterData)

		UserDatabase.createInstance(rule.activity).setAdditionalUserData(
			MOCK_USER_ID, masterData
		)
		DateTimeUtils.setCurrentMillisFixed(
			DateTime.now()
				.withDayOfWeek(2)
				.withTime(10, 15, 0, 0)
				.millis
		)
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotRed() {
		mainActivityScreenshotWithTheme(materialColors[0], false, "1-red")
	}

	/*@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotOrange() {
		mainActivityScreenshotWithTheme(materialColors[15], false, "2-orange")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotYellow() {
		mainActivityScreenshotWithTheme(materialColors[13], false, "3-yellow")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotGreen() {
		mainActivityScreenshotWithTheme(materialColors[11], false, "4-green")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotBlue() {
		mainActivityScreenshotWithTheme(materialColors[6], false, "5-blue")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotIndigo() {
		mainActivityScreenshotWithTheme(materialColors[5], false, "6-indigo")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotPurple() {
		mainActivityScreenshotWithTheme(materialColors[3], false, "7-purple")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotRedDark() {
		mainActivityScreenshotWithTheme(materialColors[0], true, "dark-1-red")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotOrangeDark() {
		mainActivityScreenshotWithTheme(materialColors[15], true, "dark-2-orange")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotYellowDark() {
		mainActivityScreenshotWithTheme(materialColors[13], true, "dark-3-yellow")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotGreenDark() {
		mainActivityScreenshotWithTheme(materialColors[11], true, "dark-4-green")
	}*/

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotBlueDark() {
		mainActivityScreenshotWithTheme(materialColors[6], true, "dark-5-blue")
	}

	/*@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotIndigoDark() {
		mainActivityScreenshotWithTheme(materialColors[5], true, "dark-6-indigo")
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshotPurpleDark() {
		mainActivityScreenshotWithTheme(materialColors[3], true, "dark-7-purple")
	}*/

	@OptIn(ExperimentalMaterial3Api::class)
	private fun mainActivityScreenshotWithTheme(
		themeColor: Color,
		darkTheme: Boolean = false,
		themeColorName: String
	) {
		rule.setContent {
			rule.activity.setSystemUiColor(rememberSystemUiController())
			rule.activity.setUser(userMock(), false)
			rule.activity.AppTheme(systemUiController = null, initialDarkTheme = false) {
				val state = rememberMainAppState(
					user = rule.activity.user!!,
					contextActivity = rule.activity,
					timetableDatabaseInterface = TimetableDatabaseInterface(
						database = UserDatabase.createInstance(rule.activity),
						id = MOCK_USER_ID
					),
					preferences = preferenceWithTheme(
						rule.activity.dataStorePreferences,
						themeColor,
						darkTheme
					),
					customThemeColor = rule.activity.customThemeColor,
					globalPreferences = rule.activity.globalDataStore
				)

				state.loading.value = Int.MIN_VALUE // Never show loading indicator

				WithScreenshot {
					MainApp(state)
				}

				//rule.mainClock.advanceTimeUntil(30000) { !state.isLoading } // Doesn't seem to work at all
			}
		}

		rule.takeScreenshot("activity-main-${themeColorName}.png")
	}

	@After
	fun cleanupUserDatabase() {
		UserDatabase.createInstance(rule.activity).deleteUser(MOCK_USER_ID)
	}
}
