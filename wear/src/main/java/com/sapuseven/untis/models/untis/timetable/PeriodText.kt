package com.sapuseven.untis.models.untis.timetable

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class PeriodText(
		val lesson: String,
		val substitution: String,
		val info: String,
		val attachments: UnknownObject? = null
) {
	init {
		UnknownObject.validate(mapOf("attachments" to attachments))
	}
}
