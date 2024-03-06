package com.sapuseven.untis.models.untis.masterdata.timegrid

import com.sapuseven.untis.api.model.untis.Time
import kotlinx.serialization.Serializable

@Serializable
data class Unit(
		val label: String,
		val startTime: Time,
		val endTime: Time
)
