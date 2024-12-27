package com.sapuseven.untis.api.model.untis.timetable

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PeriodExam(
	val id: Long,
	@Transient val userId: Long = -1,
	val examtype: String?,
	val name: String?,
	val text: String?
)
