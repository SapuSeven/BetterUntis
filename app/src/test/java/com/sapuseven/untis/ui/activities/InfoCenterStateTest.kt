package com.sapuseven.untis.ui.activities

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sapuseven.untis.activities.BaseComposeActivity
import com.sapuseven.untis.data.connectivity.UntisApiConstants
import com.sapuseven.untis.data.databases.UserDatabase
import com.sapuseven.untis.helpers.timetable.TimetableDatabaseInterface
import com.sapuseven.untis.models.UntisAbsence
import com.sapuseven.untis.models.UntisMessage
import com.sapuseven.untis.models.UntisOfficeHour
import com.sapuseven.untis.models.untis.UntisDateTime
import com.sapuseven.untis.models.untis.UntisUserData
import com.sapuseven.untis.preferences.DataStorePreferences
import io.mockk.every
import io.mockk.mockk
import org.joda.time.LocalDateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InfoCenterStateTest {
	val testAbsences = listOf(
		UntisAbsence(
			1, 1, 1,
			UntisDateTime(LocalDateTime.now().minusDays(1)),
			UntisDateTime(LocalDateTime.now().minusDays(1).plusHours(2)),
			true,
			false,
			null,
			0,
			"Absence Unexcused Yesterday",
			""
		),
		UntisAbsence(
			2, 1, 1,
			UntisDateTime(LocalDateTime.now().minusDays(1)),
			UntisDateTime(LocalDateTime.now().minusDays(1).plusHours(2)),
			true,
			true,
			null,
			0,
			"Absence Excused Yesterday",
			""
		),
		UntisAbsence(
			3, 1, 1,
			UntisDateTime(LocalDateTime.now().minusDays(8)),
			UntisDateTime(LocalDateTime.now().minusDays(8).plusHours(2)),
			true,
			true,
			null,
			0,
			"Absence Excused Last Week",
			""
		),
		UntisAbsence(
			4, 1, 1,
			UntisDateTime(LocalDateTime.now().minusDays(35)),
			UntisDateTime(LocalDateTime.now().minusDays(35).plusHours(2)),
			true,
			false,
			null,
			0,
			"Absence Unexcused Last Month",
			""
		),
	)

	@Test
	fun absenceSettings_mock() {
		val infoCenterState = mockk<InfoCenterState>()

		every { infoCenterState.absences.value } returns testAbsences

		Assertions.assertIterableEquals(
			testAbsences,
			infoCenterState.absences.value
		)
	}

	@Test
	fun absenceSettings_filterUnexcused() {
		val infoCenterState = mockk<InfoCenterState>()

		every { infoCenterState.absences.value } returns testAbsences
		every { infoCenterState.absencesSortAscending.value } returns true
		every { infoCenterState.absencesOnlyUnexcused.value } returns true
		every { infoCenterState.absencesTimeRange.value } returns ""
		every { infoCenterState.absenceList } answers { callOriginal() }

		infoCenterState.absenceList!!.forEach {
			Assertions.assertEquals(false, it.excused)
		}
	}

	@Test
	fun absenceSettings_sortAscending() {
		val infoCenterState = mockk<InfoCenterState>()

		every { infoCenterState.absences.value } returns testAbsences
		every { infoCenterState.absencesSortAscending.value } returns true
		every { infoCenterState.absencesOnlyUnexcused.value } returns false
		every { infoCenterState.absencesTimeRange.value } returns ""
		every { infoCenterState.absenceList } answers { callOriginal() }

		Assertions.assertIterableEquals(
			listOf(
				testAbsences[0],
				testAbsences[1],
				testAbsences[2],
				testAbsences[3],
			),
			infoCenterState.absenceList
		)
	}

	@Test
	fun absenceSettings_sortDescending() {
		val infoCenterState = mockk<InfoCenterState>()

		every { infoCenterState.absences.value } returns testAbsences
		every { infoCenterState.absencesSortAscending.value } returns false
		every { infoCenterState.absencesOnlyUnexcused.value } returns false
		every { infoCenterState.absencesTimeRange.value } returns ""
		every { infoCenterState.absenceList } answers { callOriginal() }

		Assertions.assertIterableEquals(
			listOf(
				testAbsences[3],
				testAbsences[2],
				testAbsences[1],
				testAbsences[0],
			),
			infoCenterState.absenceList
		)
	}

	@Test
	fun absenceSettings_timeRange_fourteenDays() {
		val infoCenterState = mockk<InfoCenterState>()

		every { infoCenterState.absences.value } returns testAbsences
		every { infoCenterState.absencesSortAscending.value } returns true
		every { infoCenterState.absencesOnlyUnexcused.value } returns false
		every { infoCenterState.absencesTimeRange.value } returns "fourteen_days"
		every { infoCenterState.absenceList } answers { callOriginal() }

		Assertions.assertIterableEquals(
			listOf(
				testAbsences[0],
				testAbsences[1],
				testAbsences[2],
			),
			infoCenterState.absenceList
		)
	}
}
