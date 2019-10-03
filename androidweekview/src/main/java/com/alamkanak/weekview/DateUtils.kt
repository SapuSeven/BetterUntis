package com.alamkanak.weekview

import android.content.Context
import android.text.format.DateFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

internal object DateUtils {
	/**
	 * Returns a list of days starting at [startDay] with a length of [size] taking into account
	 * the week start day and length to exclude all weekend days.
	 *
	 * @param startDay the first day that should be in the returned list
	 * @param size The number of days to be returned
	 * @param weekStart The day index of the first day of the visible week. Example: [DateTimeConstants.MONDAY]
	 * @param weekLength The length of the displayed week. Example: `5` for a week from [DateTimeConstants.MONDAY] until [DateTimeConstants.FRIDAY]
	 *
	 * @return A list with all days that are within the visible week, starting with [startDay]
	 */
	fun getDateRange(startDay: DateTime, size: Int, weekStart: Int, weekLength: Int): List<DateTime> {
		val days = ArrayList<DateTime>()
		var day: DateTime
		var dayNumber = 0

		while (days.size <= size) {
			day = startDay.plusDays(dayNumber)

			if (day.dayOfWeek in weekStart until weekStart + weekLength)
				days.add(day)
			dayNumber++
		}

		return days
	}

	/**
	 * Calculates the offset of a day relative to the specified week start.
	 *
	 * If the day is not within the specified week start and end dates, an offset of `0` is returned instead.
	 *
	 * @param day The day to calculate the offset for
	 * @param weekStart The day index of the first day of the visible week. Example: [DateTimeConstants.MONDAY]
	 * @param weekLength The length of the displayed week. Example: `5` for a week from [DateTimeConstants.MONDAY] until [DateTimeConstants.FRIDAY]
	 *
	 * @return The offset of [day] relative to the specified [weekStart]. Never negative.
	 */
	fun offsetInWeek(day: DateTime, weekStart: Int, weekLength: Int): Int {
		val offset = day.dayOfWeek - weekStart
		return if (offset in 0 until weekStart + weekLength - 1) offset else 0
	}

	/**
	 * Converts between from displayed days to actual days, accounting for skipped days (if [weekLength] < `7`).
	 *
	 * @param displayedDays The amount of displayed days, starting at the first visible day of the week.
	 * @param weekLength The length of the displayed week. Example: `5` for a week from [DateTimeConstants.MONDAY] until [DateTimeConstants.FRIDAY]
	 *
	 * @return The amount of actual days.
	 */
	fun actualDays(displayedDays: Int, weekLength: Int): Int {
		val skippedDays = if (displayedDays < 0)
			(displayedDays + 1) / weekLength * (7 - weekLength) - (7 - weekLength)
		else
			displayedDays / weekLength * (7 - weekLength)

		return displayedDays + skippedDays
	}

	/**
	 * Converts from actual days to displayed days, accounting for skipped days (if [weekLength] < `7`).
	 *
	 * @param actualDays The amount of actual days, starting at the first visible day of the week.
	 * @param weekLength The length of the displayed week. Example: `5` for a week from [DateTimeConstants.MONDAY] until [DateTimeConstants.FRIDAY]
	 *
	 * @return The amount of actual days.
	 */
	fun displayedDays(actualDays: Int, weekLength: Int): Int {
		val skippedDays = if (actualDays < 0)
			(actualDays + 1) / 7 * (7 - weekLength) - (7 - weekLength)
		else
			actualDays / 7 * (7 - weekLength)

		return actualDays - skippedDays
	}

	fun isSameDay(dayOne: DateTime, dayTwo: DateTime) = dayOne.year == dayTwo.year && dayOne.dayOfYear == dayTwo.dayOfYear

	fun getTimeFormat(context: Context): SimpleDateFormat {
		return if (DateFormat.is24HourFormat(context))
			SimpleDateFormat("H:mm", Locale.getDefault())
		else
			SimpleDateFormat("h:mm a", Locale.getDefault())
	}
}
