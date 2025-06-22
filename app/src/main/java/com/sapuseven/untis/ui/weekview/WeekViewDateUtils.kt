package com.sapuseven.untis.ui.weekview

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields

/**
 * Calculates the relative page index corresponding to a date.
 * The page index for today will always be `0`.
 *
 * @param date The date to calculate the page index for
 * @param clock The clock to use for the current date
 * @param firstDayOfWeek The first day of the week on a page
 * @param weekLength The number of days displayed per page
 * @param defaultToNext Wether to return the next or the previous week if the specified [date] isn't visible
 * (e.g. for weekends when only week days are displayed)
 * @return The page index relative to the page corresponding to today
 * @see startDateForPageIndex
 */
internal fun pageIndexForDate(
	date: LocalDate,
	clock: Clock = Clock.systemDefaultZone(),
	firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
	weekLength: Int = 5,
	defaultToNext: Boolean = true,
): Long {
	val weeks = ChronoUnit.WEEKS.between(
		weekStartForDate(LocalDate.now(clock), firstDayOfWeek), weekStartForDate(date, firstDayOfWeek)
	)

	return if (ChronoUnit.DAYS.between(
			weekStartForDate(date, firstDayOfWeek),
			date
		) >= weekLength && defaultToNext
	) weeks + 1
	else weeks
}

/**
 * Calculates the start date for a specific page.
 *
 * @param pageIndex The page index relative to todays page
 * @param clock The clock to use for the current date
 * @param firstDayOfWeek The first day of the week on a page
 * @param weekLength The number of days displayed per page
 * @return The date of the first visible day on the page specified by [pageIndex]
 * @see pageIndexForDate
 */
internal fun startDateForPageIndex(
	pageIndex: Long,
	clock: Clock = Clock.systemDefaultZone(),
	firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY,
): LocalDate {
	return weekStartForDate(LocalDate.now(clock), firstDayOfWeek).plusWeeks(pageIndex)
}

private fun weekStartForDate(
	date: LocalDate, firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
): LocalDate = date.with(WeekFields.of(firstDayOfWeek, 4).dayOfWeek(), 1).let {
	if (it.isAfter(date)) it.minusWeeks(1)
	else it
}

internal class DateIterator(
	val startDate: LocalDate, val endDateInclusive: LocalDate, val stepDays: Long
) : Iterator<LocalDate> {
	private var currentDate = startDate

	override fun hasNext() = currentDate <= endDateInclusive

	override fun next(): LocalDate {
		val next = currentDate
		currentDate = currentDate.plusDays(stepDays)
		return next
	}
}

internal class DateProgression(
	override val start: LocalDate, override val endInclusive: LocalDate, val stepDays: Long = 1
) : Iterable<LocalDate>, ClosedRange<LocalDate> {

	override fun iterator(): Iterator<LocalDate> = DateIterator(start, endInclusive, stepDays)

	infix fun step(days: Long) = DateProgression(start, endInclusive, days)
}

internal operator fun LocalDate.rangeTo(other: LocalDate) = DateProgression(this, other)
