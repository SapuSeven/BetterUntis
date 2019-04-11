package com.sapuseven.untis.models.untis.timetable

import kotlinx.serialization.Serializable

@Serializable
data class PeriodHomeworkAttachment(
		val id: Int,
		val name: String,
		val url: String
)