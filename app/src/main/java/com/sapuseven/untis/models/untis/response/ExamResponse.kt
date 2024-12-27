package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.api.model.untis.classreg.Exam
import kotlinx.serialization.Serializable

@Serializable
data class ExamResponse(
		val result: ExamResult? = null
) : BaseResponse()

@Serializable
data class ExamResult(
		val id: Int,
		val type: String,
		val exams: List<Exam>
)
