package com.sapuseven.untis.api.model.untis.masterdata.timegrid

import com.sapuseven.untis.api.serializer.DayOfWeekSerializer
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
data class Day(
	@Serializable(with = DayOfWeekSerializer::class)
	val day: DayOfWeek,
	val units: List<Unit>
)
