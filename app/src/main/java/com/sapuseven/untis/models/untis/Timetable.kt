package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.untis.timetable.Period
import kotlinx.serialization.Serializable

@Serializable
data class Timetable(
		val displayableStartDate: String,
		val displayableEndDate: String,
		val periods: List<Period>
)