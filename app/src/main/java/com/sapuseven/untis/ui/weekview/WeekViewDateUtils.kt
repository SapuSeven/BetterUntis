package com.sapuseven.untis.ui.weekview

import org.joda.time.DateTimeConstants
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.Weeks

/**
 * Calculates the relative page index corresponding to a date.
 * The page index for today will always be `0`.
 *
 * @param date The date to calculate the page index for
 * @param weekStartDay The first day of the week on a page
 * @param weekLength The number of days displayed per page
 * @param defaultToNext Wether to return the next or the previous week if the specified [date] isn't visible
 * (e.g. for weekends when only week days are displayed)
 * @return The page index relative to the page corresponding to today
 * @see startDateForPageIndex
 */
internal fun pageIndexForDate(
	date: LocalDate,
	weekStartDay: Int = DateTimeConstants.MONDAY,
	weekLength: Int = 5,
	defaultToNext: Boolean = true
): Int {
	val weeks = Weeks.weeksBetween(
		weekStartForDate(LocalDate.now(), weekStartDay),
		weekStartForDate(date, weekStartDay)
	).weeks

	return if (
		Days.daysBetween(weekStartForDate(date, weekStartDay), date).days >= weekLength
		&& defaultToNext
	)
		weeks + 1
	else
		weeks
}

/**
 * Calculates the start date for a specific page.
 *
 * @param pageIndex The page index relative to todays page
 * @param weekStartDay The first day of the week on a page
 * @param weekLength The number of days displayed per page
 * @return The date of the first visible day on the page specified by [pageIndex]
 * @see pageIndexForDate
 */
internal fun startDateForPageIndex(
	pageIndex: Int,
	weekStartDay: Int = DateTimeConstants.MONDAY
): LocalDate {
	return weekStartForDate(LocalDate.now(), weekStartDay).plusWeeks(pageIndex)
}

private fun weekStartForDate(
	date: LocalDate,
	weekStartDay: Int = DateTimeConstants.MONDAY
): LocalDate = date.withDayOfWeek(weekStartDay).let {
	if (it.isAfter(date))
		it.minusWeeks(1)
	else
		it
}
