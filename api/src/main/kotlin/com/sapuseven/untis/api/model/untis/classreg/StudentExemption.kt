package com.sapuseven.untis.api.model.untis.classreg

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
	val weekDay: DayOfWeek // TODO: Write a deserializer for this enum: MON(2), TUE(3), WED(4), THU(5), FRI(6), SAT(7), SUN(1)
)
