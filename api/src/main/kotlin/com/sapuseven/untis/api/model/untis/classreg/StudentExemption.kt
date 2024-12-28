package com.sapuseven.untis.api.model.untis.classreg

import com.sapuseven.untis.api.serializer.DayOfWeekSerializer
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
data class StudentExemption(
	val endDate: String,
	val endTime: String,
	val exemptionReason: String,
	val id: Long,
	val startDate: String,
	val startTime: String,
	val studentId: Long,
	val subjectGroup: String,
	val subjectId: Long,
	val text: String,
	@Serializable(with = DayOfWeekSerializer::class)
	val weekDay: DayOfWeek
)
