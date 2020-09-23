package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisHomework
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkResponse(
		val result: HomeworkResult? = null
) : BaseResponse()

@Serializable
data class HomeworkResult(
		val homeWorks: List<UntisHomework>,
		val lessonsById: Map<String, UntisHomeworkLesson>
)

@Serializable
data class UntisHomeworkLesson(
		val id: Int,
		val subjectId: Int,
		val klassenIds: List<Int>,
		val teacherIds: List<Int>
)
