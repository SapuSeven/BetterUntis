package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UnknownObject
import com.sapuseven.untis.models.UntisHomework
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkResponse(
		val result: HomeworkResult? = null
) : BaseResponse()

@Serializable
data class HomeworkResult(
		val homeWorks: List<UntisHomework>,
		val lessonsById: UnknownObject
)
