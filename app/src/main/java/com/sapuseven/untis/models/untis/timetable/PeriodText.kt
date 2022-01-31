package com.sapuseven.untis.models.untis.timetable

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class PeriodText(
		val lesson: String,
		val substitution: String,
		val info: String,
		val staffInfo: UnknownObject? = null, // Type is probably String, but UnknownObject is used for now to determine possible contents
		val attachments: UnknownObject? = null,
		val staffAttachments: UnknownObject? = null
) {
	init {
		UnknownObject.validate(mapOf("staffInfo" to staffInfo))
		UnknownObject.validate(mapOf("attachments" to attachments))
		UnknownObject.validate(mapOf("staffAttachments" to staffAttachments))
	}
}
