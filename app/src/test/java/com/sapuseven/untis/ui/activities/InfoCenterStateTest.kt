package com.sapuseven.untis.ui.activities

import com.sapuseven.untis.ui.activities.infocenter.InfoCenterState
import io.mockk.every
import io.mockk.mockk
import org.joda.time.LocalDateTime
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InfoCenterStateTest {
	val testAbsences = listOf(
		UntisAbsence(
			1, 1, 1,
			UntisDateTime(LocalDateTime.now().minusDays(1).plusHours(2)),
			UntisDateTime(LocalDateTime.now().minusDays(1).plusHours(4)),
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
		every { infoCenterState.absencesSortReversed.value } returns true
		every { infoCenterState.absencesOnlyUnexcused.value } returns true
		every { infoCenterState.absencesTimeRange.value } returns ""
		every { infoCenterState.absenceList } answers { callOriginal() }

		infoCenterState.absenceList!!.forEach {
			Assertions.assertEquals(false, it.excused)
		}
	}

	@Test
	fun absenceSettings_sortReversedUnchecked_newestFirst() {
		val infoCenterState = mockk<InfoCenterState>()

		every { infoCenterState.absences.value } returns testAbsences
		every { infoCenterState.absencesSortReversed.value } returns false
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
	fun absenceSettings_sortReversedChecked_oldestFirst() {
		val infoCenterState = mockk<InfoCenterState>()

		every { infoCenterState.absences.value } returns testAbsences
		every { infoCenterState.absencesSortReversed.value } returns true
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
		every { infoCenterState.absencesSortReversed.value } returns false
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
