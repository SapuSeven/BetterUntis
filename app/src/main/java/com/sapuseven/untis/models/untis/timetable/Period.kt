package com.sapuseven.untis.models.untis.timetable

import com.sapuseven.untis.models.UnknownObject
import com.sapuseven.untis.models.UntisHomework
import com.sapuseven.untis.models.untis.UntisDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Period(
		val id: Int,
		val lessonId: Int,
		var startDateTime: UntisDateTime,
		var endDateTime: UntisDateTime,
		val foreColor: String,
		val backColor: String,
		val innerForeColor: String,
		val innerBackColor: String,
		val text: PeriodText,
		val elements: List<PeriodElement>,
		val can: List<String>,
		val `is`: List<String>,
		val homeWorks: List<UntisHomework>?,
		val exam: UnknownObject? = null, // This element is currently not being used by BetterUntis. More data required
		val isOnlinePeriod: Boolean? = null,
		val onlinePeriodLink: String? = null,
		val messengerChannel: PeriodMessengerChannel? = null,
		val blockHash: UnknownObject? = null
) {
	companion object {
		const val CODE_REGULAR = "REGULAR"
		const val CODE_CANCELLED = "CANCELLED"
		const val CODE_IRREGULAR = "IRREGULAR"
		const val CODE_EXAM = "EXAM"
	}

	init {
		UnknownObject.validate(mapOf("exam" to exam))
		UnknownObject.validate(mapOf("blockHash" to blockHash))
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
