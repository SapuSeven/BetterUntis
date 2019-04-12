package com.sapuseven.untis.models.untis.timetable

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
data class Period(
		val id: Int,
		val lessonId: Int,
		var startDateTime: String,
		var endDateTime: String,
		val foreColor: String,
		val backColor: String,
		val innerForeColor: String,
		val innerBackColor: String,
		val text: PeriodText,
		val elements: List<PeriodElement>,
		val can: List<String>,
		val `is`: List<String>,
		val homeWorks: List<PeriodHomework>,
		@ContextualSerialization val messengerChannel: Any? = null // This is a new element with unknown usage, it disappeared again after a while
) {
	companion object {
		const val CODE_REGULAR = "REGULAR"
		const val CODE_CANCELLED = "CANCELLED"
		const val CODE_IRREGULAR = "IRREGULAR"
		const val CODE_EXAM = "EXAM"
	}
}