package com.sapuseven.untis.api.model.untis.timetable

import com.sapuseven.untis.api.model.untis.Attachment
import kotlinx.serialization.Serializable


@Serializable
data class LessonTopic(
	val periodId: Long,
	val text: String,
	val attachments: List<Attachment>,
	val startDateTime: String,
	val endDateTime: String,
	val teachingMethodId: Long
)
