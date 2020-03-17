package com.sapuseven.untis.models.untis.timetable

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class PeriodHomework(
		val id: Int,
		val lessonId: Int,
		val startDate: String,
		val endDate: String,
		val text: String,
		val remark: UnknownObject?, // TODO: Determine the element value
		val completed: Boolean,
		val attachments: List<PeriodHomeworkAttachment>
) {
	init {
		UnknownObject.validate(mapOf("remark" to remark))
	}
}