package com.sapuseven.untis.api.model.untis.classreg

import com.sapuseven.untis.api.serializer.DateTime
import kotlinx.serialization.Serializable

@Serializable
data class Exam(
	val id: Long,
	val examType: String?,
	val startDateTime: DateTime,
	val endDateTime: DateTime,
	val departmentId: Long,
	val subjectId: Long,
	val klasseIds: List<Long> = emptyList(),
	val roomIds: List<Long> = emptyList(),
	val teacherIds: List<Long> = emptyList(),
	val invigilators: List<Invigilator> = emptyList(),
	val name: String,
	val text: String
)
