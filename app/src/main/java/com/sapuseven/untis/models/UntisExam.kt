package com.sapuseven.untis.models

import com.sapuseven.untis.models.untis.UntisDateTime
import com.sapuseven.untis.models.untis.UntisTime
import kotlinx.serialization.Serializable

@Serializable
data class UntisExam(
		val id: Int,
		val examType: String?,
		val startDateTime: UntisDateTime,
		val endDateTime: UntisDateTime,
		val departmentId: Int,
		val subjectId: Int,
		val klasseIds: List<Int>,
		val roomIds: List<Int>,
		val teacherIds: List<Int>,
		val invigilators: List<UntisInvigilator>,
		val name: String,
		val text: String
)

@Serializable
data class UntisInvigilator(
		val id: Int,
		val startTime: UntisTime,
		val endTime: UntisTime
)
