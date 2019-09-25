package com.alamkanak.weekview

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

data class HolidayChip(val text: String = "Test", val startDate: String, val endDate: String) {
	private val startDateCalendar = parseDate(startDate)
	private val endDateCalendar = parseDate(endDate)

	companion object {
		val DATE_FORMAT: DateTimeFormatter = ISODateTimeFormat.date()
	}

	fun isOnDay(day: DateTime) =
			day.year >= startDateCalendar.year
					&& day.dayOfYear >= startDateCalendar.dayOfYear
					&& day.year <= endDateCalendar.year
					&& day.dayOfYear <= endDateCalendar.dayOfYear

	private fun parseDate(date: String) = DATE_FORMAT.parseDateTime(date)
}
