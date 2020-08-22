package com.sapuseven.untis.views.weekview

import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

data class HolidayChip(val text: String = "Test", val startDate: String, val endDate: String) {
	private val startDateTime = parseDate(startDate)
	private val endDateTime = parseDate(endDate)

	companion object {
		val DATE_FORMAT: DateTimeFormatter = ISODateTimeFormat.date()
	}

	fun isOnDay(day: DateTime): Boolean = Interval(startDateTime, endDateTime.millisOfDay().withMaximumValue()).contains(day)

	private fun parseDate(date: String) = DATE_FORMAT.parseDateTime(date)
}
