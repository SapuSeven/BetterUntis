package com.sapuseven.untis.models.untis.timetable

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
data class PeriodHomework(
		val id: Int,
		val lessonId: Int,
		val startDate: String,
		val endDate: String,
		val text: String,
		@ContextualSerialization val remark: Any?, // TODO: Determine the element value
		val completed: Boolean,
		val attachments: List<PeriodHomeworkAttachment>
)