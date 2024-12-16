package com.sapuseven.untis.mocks

import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_ABSENCES
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_CLASSREGISTER
import com.sapuseven.untis.data.connectivity.UntisApiConstants.RIGHT_OFFICEHOURS
import com.sapuseven.untis.models.untis.UntisTime
import com.sapuseven.untis.models.untis.UntisUserData
import com.sapuseven.untis.api.model.untis.masterdata.TimeGrid
import com.sapuseven.untis.api.model.untis.masterdata.timegrid.Day
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
	userData = UntisUserData(
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
		startTime = UntisTime("T07:45"),
		endTime = UntisTime("T08:35")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "2",
		startTime = UntisTime("T08:40"),
		endTime = UntisTime("T09:30")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "3",
		startTime = UntisTime("T09:45"),
		endTime = UntisTime("T10:35")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "4",
		startTime = UntisTime("T10:40"),
		endTime = UntisTime("T11:30")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "5",
		startTime = UntisTime("T11:35"),
		endTime = UntisTime("T12:25")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "6",
		startTime = UntisTime("T12:30"),
		endTime = UntisTime("T13:20")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "7",
		startTime = UntisTime("T13:25"),
		endTime = UntisTime("T14:15")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "8",
		startTime = UntisTime("T14:20"),
		endTime = UntisTime("T15:10")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "9",
		startTime = UntisTime("T15:20"),
		endTime = UntisTime("T16:10")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "10",
		startTime = UntisTime("T16:15"),
		endTime = UntisTime("T17:05")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "11",
		startTime = UntisTime("T17:10"),
		endTime = UntisTime("T18:00")
	),
	com.sapuseven.untis.api.model.untis.masterdata.timegrid.Unit(
		label = "12",
		startTime = UntisTime("T18:00"),
		endTime = UntisTime("T18:45")
	),
)
