package com.sapuseven.untis.helpers

import com.sapuseven.untis.ui.common.Weekday
import java.text.SimpleDateFormat
import java.util.*

fun Weekday.toLocalizedString(): String =
		SimpleDateFormat("EEEE", Locale.getDefault()).format(Calendar.getInstance().apply {
			set(Calendar.DAY_OF_WEEK, toCalendar())
		}.time)

fun Weekday.toCalendar(): Int = when (this) {
	Weekday.SUNDAY -> Calendar.SUNDAY
	Weekday.MONDAY -> Calendar.MONDAY
	Weekday.TUESDAY -> Calendar.TUESDAY
	Weekday.WEDNESDAY -> Calendar.WEDNESDAY
	Weekday.THURSDAY -> Calendar.THURSDAY
	Weekday.FRIDAY -> Calendar.FRIDAY
	Weekday.SATURDAY -> Calendar.SATURDAY
}
