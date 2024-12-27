package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.api.model.untis.timetable.OfficeHour
import kotlinx.serialization.Serializable

@Serializable
data class OfficeHoursResponse(
		val result: OfficeHoursResult? = null
) : BaseResponse()

@Serializable
data class OfficeHoursResult(
		val officeHours: List<OfficeHour>
)
