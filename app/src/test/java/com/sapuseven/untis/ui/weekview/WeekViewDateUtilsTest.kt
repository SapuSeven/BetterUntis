package com.sapuseven.untis.ui.weekview

import org.joda.time.DateTimeConstants
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll

import org.junit.jupiter.api.Test

internal class WeekViewDateUtilsTest {
	@Test
    fun pageIndexForDate_weekdays() {
		val nowDate = LocalDate.now()

		assertEquals(0, pageIndexForDate(nowDate)) // Monday
		assertEquals(0, pageIndexForDate(nowDate.plusDays(1))) // Tuesday
		assertEquals(1, pageIndexForDate(nowDate.plusWeeks(1))) // next Monday
		assertEquals(2, pageIndexForDate(nowDate.plusWeeks(2))) // Monday in 2 weeks
		assertEquals(-1, pageIndexForDate(nowDate.minusWeeks(1))) // last Monday
		assertEquals(-2, pageIndexForDate(nowDate.minusWeeks(2))) // Monday 2 weeks ago
    }

	@Test
    fun pageIndexForDate_weekends() {
		val nowDate = LocalDate.now()

		assertEquals(0, pageIndexForDate(nowDate.minusDays(1))) // last Sunday
		assertEquals(1, pageIndexForDate(nowDate.plusDays(5))) // Saturday

		assertEquals(-1, pageIndexForDate(nowDate.minusDays(1), defaultToNext = false)) // last Sunday
		assertEquals(0, pageIndexForDate(nowDate.plusDays(5), defaultToNext = false)) // Saturday
    }

	@Test
    fun pageIndexForDate_weekLength_weekends() {
		val nowDate = LocalDate.now()

		assertEquals(0, pageIndexForDate(nowDate.minusDays(3), weekLength = 3)) // last Friday
		assertEquals(1, pageIndexForDate(nowDate.plusDays(3), weekLength = 3)) // Thursday
    }

    @Test
    fun startDateForPageIndex_defaults() {
		assertEquals(LocalDate(2023, 6, 19), startDateForPageIndex(0))
		assertEquals(LocalDate(2023, 6, 26), startDateForPageIndex(1))
		assertEquals(LocalDate(2023, 6, 12), startDateForPageIndex(-1))
	}

	companion object {
		@JvmStatic
		@BeforeAll
		fun setDateTime(): Unit {
			val nowDate = LocalDate(2023, 6, 19)
			DateTimeUtils.setCurrentMillisFixed(nowDate.toDateTimeAtStartOfDay().millis)
			assertEquals(DateTimeConstants.MONDAY, nowDate.dayOfWeek)
		}
	}
}
