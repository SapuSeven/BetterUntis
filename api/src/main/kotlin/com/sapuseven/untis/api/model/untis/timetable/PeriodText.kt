package com.sapuseven.untis.api.model.untis.timetable

import com.sapuseven.untis.api.model.untis.Attachment
import kotlinx.serialization.Serializable

@Serializable
data class PeriodText(
	val lesson: String,
	val substitution: String,
	val info: String,
	val attachments: List<Attachment>? = null,
)
