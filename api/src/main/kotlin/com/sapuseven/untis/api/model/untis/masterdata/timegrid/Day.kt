package com.sapuseven.untis.api.model.untis.masterdata.timegrid

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
data class Day(
	val day: DayOfWeek,
	val units: List<Unit>
)
