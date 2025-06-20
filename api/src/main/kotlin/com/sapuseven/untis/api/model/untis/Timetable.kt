package com.sapuseven.untis.api.model.untis

import com.sapuseven.untis.api.model.untis.timetable.Period
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class Timetable(
	val displayableStartDate: Date,
	val displayableEndDate: Date,
	val periods: List<Period> = emptyList()
)
