package com.sapuseven.untis.views.weekview

import android.content.Context
import android.text.format.DateFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

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
		if (weekLength == 0) return emptyList()

		val days = ArrayList<DateTime>()
		var day: DateTime
		var dayNumber = 0

		while (days.size < size) {
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
	 * @param day The day to calculate the offset for
	 * @param weekStart The day index of the first day of the visible week. Example: [DateTimeConstants.MONDAY]
	 *
	 * @return The offset of [day] relative to the specified [weekStart]
	 */
	fun offsetInWeek(day: DateTime, weekStart: Int): Int {
		return day.dayOfWeek - weekStart
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
		if (weekLength == 0) return 0

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
			actualDays / 7 * (7 - weekLength) + max(0, actualDays % 7 - weekLength)

		return actualDays - skippedDays
	}

	/**
	 * Checks if two [DateTime] objects are on the same day.
	 *
	 * @param dayOne The first day.
	 * @param dayTwo The second day.
	 * @return `true` if the two [DateTime] objects are in the same year and on the same day of year, `false` otherwise.
	 */
	fun isSameDay(dayOne: DateTime, dayTwo: DateTime) = dayOne.year == dayTwo.year && dayOne.dayOfYear == dayTwo.dayOfYear

	/**
	 * Returns a simple time format according to the current system settings
	 * (checks if 24-hour time format is enabled).
	 *
	 * @param context Application context.
	 * @return A [SimpleDateFormat] with the correct time format.
	 */
	fun getTimeFormat(context: Context): SimpleDateFormat {
		return if (DateFormat.is24HourFormat(context))
			SimpleDateFormat("H:mm", Locale.getDefault())
		else
			SimpleDateFormat("h:mm a", Locale.getDefault())
	}
}
