package com.sapuseven.untis.mocks

import com.sapuseven.untis.api.model.untis.Time
import com.sapuseven.untis.api.model.untis.UserData
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_ABSENCES
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_CLASSREGISTER
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_OFFICEHOURS
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
import com.sapuseven.untis.data.databases.entities.User
import com.sapuseven.untis.utils.SCREENSHOT_API_URL
import com.sapuseven.untis.utils.SCREENSHOT_PROFILE_NAME

const val MOCK_USER_ID: Long = Long.MAX_VALUE

fun userMock(profileName: String = SCREENSHOT_PROFILE_NAME, apiUrl: String = SCREENSHOT_API_URL): User = User(
	id = MOCK_USER_ID,
	profileName = profileName,
	apiUrl = apiUrl,
	schoolId = "test",
	user = "test.user",
	key = "test.key",
	anonymous = false,
	timeGrid = timeGrid(),
	masterDataTimestamp = 0,
	userData = UserData(
		elemType = "STUDENT",
		1,
		"Test Student",
		"Test School",
		1,
		children = emptyList(),
		klassenIds = emptyList(),
		rights = listOf(RIGHT_OFFICEHOURS, RIGHT_ABSENCES, RIGHT_CLASSREGISTER)
	),
	settings = null,
	created = null,
	bookmarks = emptySet()
)

fun timeGrid(): TimeGrid = TimeGrid(
	days = days()
)

fun days(): List<Day> {
	val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

	return days.map {
		Day(
			day = it,
			units = units()
		)
	}
}

fun units(): List<com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit> = listOf(
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "1",
		startTime = Time("T07:45"),
		endTime = Time("T08:35")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "2",
		startTime = Time("T08:40"),
		endTime = Time("T09:30")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "3",
		startTime = Time("T09:45"),
		endTime = Time("T10:35")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "4",
		startTime = Time("T10:40"),
		endTime = Time("T11:30")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "5",
		startTime = Time("T11:35"),
		endTime = Time("T12:25")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "6",
		startTime = Time("T12:30"),
		endTime = Time("T13:20")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "7",
		startTime = Time("T13:25"),
		endTime = Time("T14:15")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "8",
		startTime = Time("T14:20"),
		endTime = Time("T15:10")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "9",
		startTime = Time("T15:20"),
		endTime = Time("T16:10")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "10",
		startTime = Time("T16:15"),
		endTime = Time("T17:05")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "11",
		startTime = Time("T17:10"),
		endTime = Time("T18:00")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "12",
		startTime = Time("T18:00"),
		endTime = Time("T18:45")
	),
)
