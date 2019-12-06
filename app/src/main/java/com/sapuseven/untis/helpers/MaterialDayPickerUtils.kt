package com.sapuseven.untis.helpers

import ca.antonious.materialdaypicker.MaterialDayPicker
import java.text.SimpleDateFormat
import java.util.*

fun MaterialDayPicker.Weekday.toLocalizedString(): String =
		SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().apply {
			set(Calendar.DAY_OF_WEEK, toCalendar())
		}.time)

fun MaterialDayPicker.Weekday.toCalendar(): Int = when (this) {
	MaterialDayPicker.Weekday.SUNDAY -> Calendar.SUNDAY
	MaterialDayPicker.Weekday.MONDAY -> Calendar.MONDAY
	MaterialDayPicker.Weekday.TUESDAY -> Calendar.TUESDAY
	MaterialDayPicker.Weekday.WEDNESDAY -> Calendar.WEDNESDAY
	MaterialDayPicker.Weekday.THURSDAY -> Calendar.THURSDAY
	MaterialDayPicker.Weekday.FRIDAY -> Calendar.FRIDAY
	MaterialDayPicker.Weekday.SATURDAY -> Calendar.SATURDAY
}
