package com.sapuseven.untis.api.model.untis.timetable

import com.sapuseven.untis.api.model.untis.classreg.HomeWork
import com.sapuseven.untis.api.model.untis.enumeration.PeriodRight
import com.sapuseven.untis.api.model.untis.enumeration.PeriodState
import com.sapuseven.untis.api.model.untis.messenger.MessengerChannel
import com.sapuseven.untis.api.serializer.DateTime
import kotlinx.serialization.Serializable

@Serializable
data class Period(
	val id: Long,
	val lessonId: Long,
	var startDateTime: DateTime,
	var endDateTime: DateTime,
	val foreColor: String,
	val backColor: String,
	val innerForeColor: String,
	val innerBackColor: String,
	val text: PeriodText,
	val elements: List<PeriodElement> = emptyList(),
	val can: List<PeriodRight> = emptyList(),
	val `is`: List<PeriodState> = emptyList(),
	val homeWorks: List<HomeWork>?,
	val exam: PeriodExam? = null,
	val isOnlinePeriod: Boolean? = null,
	val messengerChannel: MessengerChannel? = null,
	val onlinePeriodLink: String? = null, // Note: This value still seems to be present in the response, but missing in the Untis Mobile sources
	val blockHash: Int? = null // Note: This value still seems to be present in the response, but missing in the Untis Mobile sources
) {
	companion object {
		const val CODE_REGULAR = "REGULAR"
		const val CODE_CANCELLED = "CANCELLED"
		const val CODE_IRREGULAR = "IRREGULAR"
		const val CODE_EXAM = "EXAM"
	}

	fun can(PersiodRight: PeriodRight): Boolean {
		return can.contains(PersiodRight)
	}

	fun `is`(PersiodState: PeriodState): Boolean {
		return `is`.contains(PersiodState)
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
