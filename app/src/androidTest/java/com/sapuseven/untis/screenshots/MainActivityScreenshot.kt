package com.sapuseven.untis.screenshots

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.R
import com.sapuseven.untis.activities.*
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.connectivity.UntisAuthentication
import com.sapuseven.untis.data.connectivity.UntisRequest
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.data.timetable.PeriodData
import com.sapuseven.untis.data.timetable.TimegridItem
import com.sapuseven.untis.helpers.SerializationUtils
import com.sapuseven.untis.helpers.api.LoginErrorInfo
import com.sapuseven.untis.helpers.api.LoginHelper
import com.sapuseven.untis.helpers.config.globalDataStore
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.mocks.MOCK_USER_ID
import com.sapuseven.untis.mocks.timeGrid
import com.sapuseven.untis.mocks.userMock
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.UntisMessage
import com.sapuseven.untis.models.UntisOfficeHour
import com.sapuseven.untis.models.untis.UntisMasterData
import com.sapuseven.untis.models.untis.masterdata.Room
import com.sapuseven.untis.models.untis.params.UserDataParams
import com.sapuseven.untis.models.untis.response.UserDataResponse
import com.sapuseven.untis.models.untis.response.UserDataResult
import com.sapuseven.untis.models.untis.timetable.Period
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.activities.EventListItem
import com.sapuseven.untis.ui.activities.InfoCenter
import com.sapuseven.untis.ui.activities.InfoCenterState
import com.sapuseven.untis.ui.activities.rememberInfoCenterState
import com.sapuseven.untis.utils.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

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
	}

	@OptIn(ExperimentalMaterial3Api::class)
	@Test
	fun mainActivityScreenshot() {
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
					preferences = preferenceWithThemeColor(rule.activity.dataStorePreferences),
					customThemeColor = rule.activity.customThemeColor,
					globalPreferences = rule.activity.globalDataStore
				)

				state.loading.value = Int.MIN_VALUE // Never show loading indicator

				WithScreenshot {
					MainApp(state)
				}
			}
		}
		rule.waitForIdle()
		rule.takeScreenshot("activity-main.png")
	}

	@After
	fun cleanupUserDatabase() {
		UserDatabase.createInstance(rule.activity).deleteUser(MOCK_USER_ID)
	}
}
