package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.untis.UntisMasterData
import com.sapuseven.untis.models.untis.UntisTimetable
import kotlinx.serialization.Serializable

@Serializable
data class TimetableResponse(
		val result: TimetableResult? = null
) : BaseResponse()

@Serializable
data class TimetableResult(
		val timetable: UntisTimetable,
		val masterData: UntisMasterData
)
