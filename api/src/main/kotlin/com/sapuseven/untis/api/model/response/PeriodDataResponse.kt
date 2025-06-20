package com.sapuseven.untis.api.model.response

import com.sapuseven.untis.api.model.untis.MasterData
import com.sapuseven.untis.api.model.untis.Person
import com.sapuseven.untis.api.model.untis.Timetable
import com.sapuseven.untis.api.model.untis.timetable.PeriodData
import kotlinx.serialization.Serializable

@Serializable
data class PeriodDataResponse(
		val result: PeriodDataResult? = null
) : BaseResponse()

@Serializable
data class PeriodDataResult(
	val dataByTTId: Map<Long, PeriodData>,
	val referencedStudents: List<Person>
)
