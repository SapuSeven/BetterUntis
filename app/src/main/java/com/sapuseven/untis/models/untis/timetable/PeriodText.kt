package com.sapuseven.untis.models.untis.timetable

import kotlinx.serialization.Serializable

@Serializable
data class PeriodText(
		val lesson: String,
		val substitution: String,
		val info: String,
		val attachments: List<String> // TODO: Determine the element value
)