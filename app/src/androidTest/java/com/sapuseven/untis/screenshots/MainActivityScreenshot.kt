package com.sapuseven.untis.screenshots

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.activities.*
import com.sapuseven.untis.data.databases.UserDatabase
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
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.activities.EventListItem
import com.sapuseven.untis.ui.activities.InfoCenter
import com.sapuseven.untis.ui.activities.InfoCenterState
import com.sapuseven.untis.ui.activities.rememberInfoCenterState
import com.sapuseven.untis.utils.WithScreenshot
import com.sapuseven.untis.utils.preferenceWithThemeColor
import com.sapuseven.untis.utils.takeScreenshot
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityScreenshot {
	@get:Rule
	val rule = createAndroidComposeRule<BaseComposeActivity>()

	@Before
	fun setupUserDatabase() {
		UserDatabase.createInstance(rule.activity).setAdditionalUserData(
			MOCK_USER_ID, UntisMasterData(
				absenceReasons = emptyList(),
				departments = emptyList(),
				duties = emptyList(),
				eventReasons = emptyList(),
				eventReasonGroups = emptyList(),
				excuseStatuses = emptyList(),
				holidays = emptyList(),
				klassen = emptyList(),
				rooms = listOf(
					Room(1, "A001", "A001"),
					Room(2, "A002", "A002"),
					Room(3, "A003", "A003"),
					Room(4, "A004", "A004"),
					Room(5, "A005", "A005"),
				),
				subjects = emptyList(),
				teachers = emptyList(),
				teachingMethods = emptyList(),
				schoolyears = emptyList(),
				timeGrid = timeGrid(),
			)
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

				WithScreenshot {
					MainApp(state)
				}
			}
		}
		rule.takeScreenshot("activity-main.png")
	}

	@After
	fun cleanupUserDatabase() {
		UserDatabase.createInstance(rule.activity).deleteUser(MOCK_USER_ID)
	}
}
