package com.sapuseven.untis.ui.weekview

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal class WeekViewDateUtilsTest {
	var instantExpected: String = "2023-06-19T00:00:00Z"
	var clock: Clock = Clock.fixed(Instant.parse(instantExpected), ZoneId.of("UTC"))

	@Test
	fun pageIndexForDate_weekdays() {
		val nowDate = LocalDate.now(clock)

		assertEquals(0, pageIndexForDate(nowDate, clock)) // Monday
		assertEquals(0, pageIndexForDate(nowDate.plusDays(1), clock)) // Tuesday
		assertEquals(1, pageIndexForDate(nowDate.plusWeeks(1), clock)) // next Monday
		assertEquals(2, pageIndexForDate(nowDate.plusWeeks(2), clock)) // Monday in 2 weeks
		assertEquals(-1, pageIndexForDate(nowDate.minusWeeks(1), clock)) // last Monday
		assertEquals(-2, pageIndexForDate(nowDate.minusWeeks(2), clock)) // Monday 2 weeks ago
	}

	@Test
	fun pageIndexForDate_weekends() {
		val nowDate = LocalDate.now(clock)

		assertEquals(0, pageIndexForDate(nowDate.minusDays(1), clock)) // last Sunday
		assertEquals(1, pageIndexForDate(nowDate.plusDays(5), clock)) // Saturday

		assertEquals(
			-1,
			pageIndexForDate(nowDate.minusDays(1), clock, defaultToNext = false)
		) // last Sunday
		assertEquals(0, pageIndexForDate(nowDate.plusDays(5), clock, defaultToNext = false)) // Saturday
	}

	@Test
	fun pageIndexForDate_weekLength_weekends() {
		val nowDate = LocalDate.now(clock)

		assertEquals(0, pageIndexForDate(nowDate.minusDays(3), clock, weekLength = 3)) // last Friday
		assertEquals(1, pageIndexForDate(nowDate.plusDays(3), clock, weekLength = 3)) // Thursday
	}

	@Test
	fun startDateForPageIndex_defaults() {
		assertEquals(LocalDate.of(2023, 6, 19), startDateForPageIndex(0, clock))
		assertEquals(LocalDate.of(2023, 6, 26), startDateForPageIndex(1, clock))
		assertEquals(LocalDate.of(2023, 6, 12), startDateForPageIndex(-1, clock))
	}

	@ParameterizedTest
	@ValueSource(longs = [-5, -1, 0, 1, 2, 5, 100])
	fun startDateForPageIndex_pageIndexForDate_isIsomorphic(pageIndex: Long) {
		assertEquals(pageIndex, pageIndexForDate(startDateForPageIndex(pageIndex)))
	}
}
