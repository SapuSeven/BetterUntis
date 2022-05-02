package com.sapuseven.untis.models.untis.timetable

import kotlinx.serialization.Serializable

// TODO: Display this element in the timetable
@Serializable
data class PeriodExam(
	val id: Int,
	val examtype: String?,
	val name: String?,
	val text: String?
)
