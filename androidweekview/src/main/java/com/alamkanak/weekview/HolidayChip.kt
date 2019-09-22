package com.alamkanak.weekview

import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat
import java.util.*

data class HolidayChip(val text: String = "Test", val startDate: String, val endDate: String) {
	private val startDateCalendar = parseDate(startDate)
	private val endDateCalendar = parseDate(endDate)

	companion object {
		val DATE_FORMAT: DateTimeFormatter = ISODateTimeFormat.date()
	}

	fun isOnDay(day: Calendar) =
			day.get(Calendar.YEAR) >= startDateCalendar.year().get()
					&& day.get(Calendar.DAY_OF_YEAR) >= startDateCalendar.dayOfYear().get()
					&& day.get(Calendar.YEAR) <= endDateCalendar.year().get()
					&& day.get(Calendar.DAY_OF_YEAR) <= endDateCalendar.dayOfYear().get()

	private fun parseDate(date: String) = DATE_FORMAT.parseDateTime(date)
}
