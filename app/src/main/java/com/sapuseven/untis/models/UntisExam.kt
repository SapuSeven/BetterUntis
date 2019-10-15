package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class UntisExam(
		val id: Int,
		val examType: String,
		val startDateTime: String,
		val endDateTime: String,
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
		val startTime: String,
		val endTime: String
)
