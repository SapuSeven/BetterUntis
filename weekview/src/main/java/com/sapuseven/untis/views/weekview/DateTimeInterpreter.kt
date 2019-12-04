package com.sapuseven.untis.views.weekview

import org.joda.time.DateTime

interface DateTimeInterpreter {
	fun interpretDate(date: DateTime): String

	fun interpretSecondaryDate(date: DateTime): String

	fun interpretTime(minutes: Int): String
}
