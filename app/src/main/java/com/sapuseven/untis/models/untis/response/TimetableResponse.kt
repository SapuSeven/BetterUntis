package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisTimetable
import kotlinx.serialization.Serializable

@Serializable
data class TimetableResponse(
		val result: UntisTimetable? = null
) : BaseResponse()