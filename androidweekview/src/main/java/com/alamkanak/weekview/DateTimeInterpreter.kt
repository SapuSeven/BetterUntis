package com.alamkanak.weekview

import java.util.*

interface DateTimeInterpreter {
	fun interpretDate(date: Calendar): String

	fun interpretSecondaryDate(date: Calendar): String

	fun interpretTime(minutes: Int): String
}
