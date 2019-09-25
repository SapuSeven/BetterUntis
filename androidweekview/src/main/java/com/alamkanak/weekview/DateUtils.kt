package com.alamkanak.weekview

import android.content.Context
import android.text.format.DateFormat
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

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
	 * Converts between displayed days and actual days, accounting for skipped days (if [weekLength] < `7`).
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

	@Deprecated("Replace all calls to this with logic that uses the other functions of this class")
	fun getDaysUntilDate(date: Calendar): Int {
		val dateInMillis = date.timeInMillis
		val todayInMillis = today().timeInMillis
		val diff = dateInMillis - todayInMillis
		return (diff / (1000L * 60L * 60L * 24L)).toInt()
	}

	@Deprecated("Replace all calls to this with logic that uses the other functions of this class")
	fun getDisplayedDays(startDay: Calendar, size: Int, weekStart: Int, weekEnd: Int): Int {
		var startDayIndex = startDay.get(Calendar.DAY_OF_WEEK)
		if (startDayIndex > weekEnd) startDayIndex -= 7 // TODO: Is this line correct?
		val offsetForWeekStart = if (startDayIndex > weekStart) startDayIndex - weekStart else 0

		var days = 0

		for (i in 0 until abs(size)) {
			startDay.add(Calendar.DATE, if (size > 0) 1 else -1)
			if (startDay.get(Calendar.DAY_OF_WEEK) in weekStart..weekEnd)
				days += if (size > 0) 1 else -1
		}

		return days + offsetForWeekStart
	}

	fun isSameDay(dayOne: DateTime, dayTwo: DateTime) = dayOne.year == dayTwo.year && dayOne.dayOfYear == dayTwo.dayOfYear

	/**
	 * Returns a calendar instance at the start of today with an optional offset
	 *
	 * @param offset the days to add to today
	 * @return the calendar instance
	 */
	fun today(offset: Int = 0): Calendar {
		val today = Calendar.getInstance()
		today.set(Calendar.HOUR_OF_DAY, 0)
		today.set(Calendar.MINUTE, 0)
		today.set(Calendar.SECOND, 0)
		today.set(Calendar.MILLISECOND, 0)
		today.add(Calendar.DATE, offset)
		return today
	}

	fun getTimeFormat(context: Context): SimpleDateFormat {
		return if (DateFormat.is24HourFormat(context))
			SimpleDateFormat("H:mm", Locale.getDefault())
		else
			SimpleDateFormat("h:mm a", Locale.getDefault())
	}
}
