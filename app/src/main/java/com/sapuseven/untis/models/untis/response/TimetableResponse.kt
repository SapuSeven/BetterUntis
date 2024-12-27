package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.api.model.untis.MasterData
import com.sapuseven.untis.api.model.untis.Timetable
import kotlinx.serialization.Serializable

@Serializable
data class TimetableResponse(
		val result: TimetableResult? = null
) : BaseResponse()

@Serializable
data class TimetableResult(
		val timetable: Timetable,
		val masterData: MasterData
)
