package com.sapuseven.untis.models.untis.timetable

import com.sapuseven.untis.models.UnknownObject
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
		val homeWorks: List<PeriodHomework>?,
		val exam: UnknownObject? = null, // This element is currently not being used by BetterUntis. More data required
		val messengerChannel: UnknownObject? = null // This is a new element with unknown usage
) {
	companion object {
		const val CODE_REGULAR = "REGULAR"
		const val CODE_CANCELLED = "CANCELLED"
		const val CODE_IRREGULAR = "IRREGULAR"
		const val CODE_EXAM = "EXAM"
	}

	init {
		UnknownObject.validate(mapOf("messengerChannel" to messengerChannel))
	}

	fun equalsIgnoreTime(second: Period): Boolean {
		return `is` == second.`is`
				&& can == second.can
				&& elements == second.elements
				&& text == second.text
				&& foreColor == second.foreColor
				&& backColor == second.backColor
				&& innerForeColor == second.innerForeColor
				&& innerBackColor == second.innerBackColor
				&& lessonId == second.lessonId
	}
}
