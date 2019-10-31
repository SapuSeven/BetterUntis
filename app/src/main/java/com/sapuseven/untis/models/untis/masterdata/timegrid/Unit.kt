package com.sapuseven.untis.models.untis.masterdata.timegrid

import kotlinx.serialization.Serializable

@Serializable
data class Unit(
		val label: String,
		val startTime: String,
		val endTime: String
)