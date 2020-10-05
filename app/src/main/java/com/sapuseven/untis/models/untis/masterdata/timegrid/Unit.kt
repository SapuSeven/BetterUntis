package com.sapuseven.untis.models.untis.masterdata.timegrid

import com.sapuseven.untis.models.untis.UntisTime
import kotlinx.serialization.Serializable

@Serializable
data class Unit(
		val label: String,
		val startTime: UntisTime,
		val endTime: UntisTime
)