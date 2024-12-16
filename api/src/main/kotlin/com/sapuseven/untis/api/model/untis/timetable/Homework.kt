package com.sapuseven.untis.api.model.untis.timetable

import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class Homework(
		val id: Int,
		val lessonId: Int,
		val startDate: Date,
		val endDate: Date,
		val text: String,
		val remark: String? = null,
		val completed: Boolean,
		val attachments: List<Attachment>
)
