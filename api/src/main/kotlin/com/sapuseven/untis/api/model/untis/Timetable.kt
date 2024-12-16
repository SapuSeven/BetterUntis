package com.sapuseven.untis.api.model.untis

import com.sapuseven.untis.api.model.untis.timetable.Period
import kotlinx.serialization.Serializable

@Serializable
data class Timetable(
	val displayableStartDate: String,
	val displayableEndDate: String,
	val periods: List<Period>
)
