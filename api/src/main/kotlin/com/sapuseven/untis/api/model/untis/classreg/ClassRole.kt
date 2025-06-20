package com.sapuseven.untis.api.model.untis.classreg

import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable


@Serializable
data class ClassRole(
	val dutyId: Long,
	val studentId: Long,
	val klasseId: Long,
	val startDate: Date,
	val endDate: Date,
	val text: String
)
