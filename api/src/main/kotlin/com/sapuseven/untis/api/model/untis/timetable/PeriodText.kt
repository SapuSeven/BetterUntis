package com.sapuseven.untis.api.model.untis.timetable

import kotlinx.serialization.Serializable

@Serializable
data class PeriodText(
		val lesson: String,
		val substitution: String,
		val info: String,
		//TODO val staffInfo: UnknownObject? = null, // Type is probably String, but UnknownObject is used for now to determine possible contents
		val attachments: List<Attachment>? = null,
		//TODO val staffAttachments: UnknownObject? = null
) {
	/*init {
		UnknownObject.validate(mapOf("staffInfo" to staffInfo))
		UnknownObject.validate(mapOf("staffAttachments" to staffAttachments))
	}*/
}
