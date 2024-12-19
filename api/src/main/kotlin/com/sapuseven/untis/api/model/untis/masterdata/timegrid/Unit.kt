package com.sapuseven.untis.api.model.untis.masterdata.timegrid

import com.sapuseven.untis.api.serializer.Time
import kotlinx.serialization.Serializable

@Serializable
data class Unit(
		val label: String,
		val startTime: Time,
		val endTime: Time
)
