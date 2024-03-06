package com.sapuseven.untis.models.untis.timetable

import kotlinx.serialization.Serializable

// TODO: Display this element in the timetable
@Serializable
data class PeriodExam(
	val id: Int,
	@Transient val userId: Long = -1,
	val examtype: String?,
	val name: String?,
	val text: String?
)
