package com.sapuseven.untis.screenshots

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.activities.RoomFinder
import com.sapuseven.untis.activities.RoomFinderState
import com.sapuseven.untis.data.databases.RoomFinderDatabase
import com.sapuseven.untis.data.databases.LegacyUserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.mocks.MOCK_USER_ID
import com.sapuseven.untis.mocks.timeGrid
import com.sapuseven.untis.mocks.userMock
import com.sapuseven.untis.models.RoomFinderItem
import com.sapuseven.untis.models.untis.UntisMasterData
import com.sapuseven.untis.models.untis.masterdata.Room
import com.sapuseven.untis.preferences.dataStorePreferences
import com.sapuseven.untis.utils.WithScreenshot
import com.sapuseven.untis.utils.preferenceWithTheme
import com.sapuseven.untis.utils.takeScreenshot
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomFinderActivityScreenshot {
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
	fun roomFinderActivityScreenshot() {
		rule.setContent {
			rule.activity.setSystemUiColor(rememberSystemUiController())
			rule.activity.setUser(userMock(), false)
			rule.activity.AppTheme(systemUiController = null, initialDarkTheme = false) {
				WithScreenshot {
					RoomFinder(RoomFinderState(
						user = rule.activity.user!!,
						timetableDatabaseInterface = TimetableDatabaseInterface(
							database = LegacyUserDatabase.createInstance(rule.activity),
							id = MOCK_USER_ID
						),
						preferences = preferenceWithTheme(rule.activity.dataStorePreferences),
						contextActivity = rule.activity,
						scope = rememberCoroutineScope(),
						roomFinderDatabase = mockRoomFinderDatabase(),
						hourIndex = remember { mutableStateOf(2) },
						showElementPicker = remember { mutableStateOf(false) }
					))
				}
			}
		}
		rule.takeScreenshot("activity-roomfinder.png")
	}

	@After
	fun cleanupUserDatabase() {
		LegacyUserDatabase.createInstance(rule.activity).deleteUser(MOCK_USER_ID)
	}
}

fun mockRoomFinderDatabase(): RoomFinderDatabase = object : RoomFinderDatabase {
	val roomList = mutableListOf(
		RoomFinderItem(
			1,
			listOf(true, false, false, false)
		),
		RoomFinderItem(
			2,
			listOf(true, true, false, false)
		),
		RoomFinderItem(
			3,
			listOf(true, false, false, true)
		),
		RoomFinderItem(
			4,
			listOf(false, false, false, true)
		),
		RoomFinderItem(
			5,
			listOf(false, false, true, true)
		)
	)

	override fun addRoom(room: RoomFinderItem) {
		roomList.add(room)
	}

	override fun deleteRoom(id: Int): Boolean = roomList.removeIf { it.id == id }

	override fun getRoom(id: Int): RoomFinderItem? = roomList.find { it.id == id }

	override fun getAllRooms(): List<RoomFinderItem> = roomList
}
