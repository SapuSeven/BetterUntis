package com.sapuseven.untis.screenshots

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.data.database.LegacyUserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.mocks.MOCK_USER_ID
import com.sapuseven.untis.mocks.timeGrid
import com.sapuseven.untis.mocks.userMock
import com.sapuseven.untis.models.untis.UntisMasterData
import com.sapuseven.untis.api.model.untis.masterdata.Room
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.ui.activities.EventListItem
import com.sapuseven.untis.ui.activities.InfoCenter
import com.sapuseven.untis.ui.activities.InfoCenterState
import com.sapuseven.untis.ui.activities.rememberInfoCenterState
import com.sapuseven.untis.utils.WithScreenshot
import com.sapuseven.untis.utils.preferenceWithTheme
import com.sapuseven.untis.utils.takeScreenshot
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InfoCenterActivityScreenshot {
	@get:Rule
	val rule = createAndroidComposeRule<BaseComposeActivity>()

	@Before
	fun setupUserDatabase() {
		LegacyUserDatabase.createInstance(rule.activity).setAdditionalUserData(
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

	@Test
	fun infoCenterActivityScreenshot() {
		val messages = listOf(
			UntisMessage(
				1,
				"School messages...",
				"You can view messages from your school here in the <b>Info Center</b>.",
				emptyList()
			),
			UntisMessage(
				2,
				"...and more!",
				"There are even more tabs with other useful information at the bottom:<br>" +
						"<br><ul>" +
						"  <li>&nbsp;<b>Events</b> shows all upcoming exams and homework assignments.</li>" +
						"  <li>&nbsp;<b>Absences</b> lets you manage your absences.</li>" +
						"  <li>&nbsp;<b>Office Hours</b> lists the available office hours for teachers.</li>" +
						"</ul>",
				emptyList()
			)
		)

		rule.setContent {
			rule.activity.setSystemUiColor(rememberSystemUiController())
			rule.activity.setUser(userMock(), false)
			rule.activity.AppTheme(systemUiController = null, initialDarkTheme = false) {
				val state = rememberInfoCenterState(
					user = rule.activity.user!!,
					userDatabase = rule.activity.userDatabase,
					timetableDatabaseInterface = TimetableDatabaseInterface(
						database = LegacyUserDatabase.createInstance(rule.activity),
						id = MOCK_USER_ID
					),
					preferences = preferenceWithTheme(rule.activity.dataStorePreferences),
					contextActivity = rule.activity,
					selectedItem = rememberSaveable { mutableStateOf(InfoCenterState.ID_MESSAGES) },
					messages = remember { mutableStateOf<List<UntisMessage>?>(messages) },
					officeHours = remember { mutableStateOf<List<UntisOfficeHour>?>(null) },
					events = remember { mutableStateOf<List<EventListItem>?>(null) },
					absences = remember { mutableStateOf<List<UntisAbsence>?>(null) },
					messagesLoading = rememberSaveable { mutableStateOf(false) },
					eventsLoading = rememberSaveable { mutableStateOf(false) },
					absencesLoading = rememberSaveable { mutableStateOf(false) },
					officeHoursLoading = rememberSaveable { mutableStateOf(false) }
				)
				state.messages.value = messages

				WithScreenshot {
					InfoCenter(state)
				}
			}
		}
		rule.takeScreenshot("activity-infocenter.png")
	}

	@After
	fun cleanupUserDatabase() {
		LegacyUserDatabase.createInstance(rule.activity).deleteUser(MOCK_USER_ID)
	}
}
