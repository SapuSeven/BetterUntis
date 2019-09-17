package com.alamkanak.weekview

import android.content.Context
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

internal object DateUtils {
	fun getDateRange(daysSinceToday: Int, size: Int, weekStart: Int, weekEnd: Int): List<Calendar> {
		val days = ArrayList<Calendar>()
		var day: Calendar

		var today = today().get(Calendar.DAY_OF_WEEK)
		if (today > weekEnd) today -= 7
		val offset = if (today < weekStart) weekStart - today else 0
		var dayNumber = daysSinceToday
		while (days.size <= size) {
			day = today()
			day.add(Calendar.DATE, dayNumber - 1 + offset)

			if (day.get(Calendar.DAY_OF_WEEK) in weekStart..weekEnd)
				days.add(day)
			dayNumber++
		}

		return days
	}

	fun withTimeAtStartOfDay(date: Calendar): Calendar {
		date.set(Calendar.HOUR_OF_DAY, 0)
		date.set(Calendar.MINUTE, 0)
		date.set(Calendar.SECOND, 0)
		date.set(Calendar.MILLISECOND, 0)
		return date
	}

	fun withTimeAtEndOfDay(date: Calendar): Calendar {
		date.set(Calendar.HOUR_OF_DAY, 23)
		date.set(Calendar.MINUTE, 59)
		date.set(Calendar.SECOND, 59)
		date.set(Calendar.MILLISECOND, 999)
		return date
	}

	fun getDaysUntilDate(date: Calendar): Int {
		val dateInMillis = date.timeInMillis
		val todayInMillis = today().timeInMillis
		val diff = dateInMillis - todayInMillis
		return (diff / (1000L * 60L * 60L * 24L)).toInt()
	}

	fun getDisplayedDays(startDay: Calendar, size: Int, weekStart: Int, weekEnd: Int): Int {
		var startDayIndex = startDay.get(Calendar.DAY_OF_WEEK)
		if (startDayIndex > weekEnd) startDayIndex -= 7 // TODO: Is this line correct?
		val offsetForWeekStart = if (startDayIndex > weekStart) startDayIndex - weekStart else 0

		var days = 0

		for (i in 0 until Math.abs(size)) {
			startDay.add(Calendar.DATE, if (size > 0) 1 else -1)
			if (startDay.get(Calendar.DAY_OF_WEEK) in weekStart..weekEnd)
				days += if (size > 0) 1 else -1
		}

		return days + offsetForWeekStart
	}

	fun isSameDay(dayOne: Calendar, dayTwo: Calendar): Boolean {
		return dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR) && dayOne.get(Calendar.DAY_OF_YEAR) == dayTwo.get(Calendar.DAY_OF_YEAR)
	}

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
