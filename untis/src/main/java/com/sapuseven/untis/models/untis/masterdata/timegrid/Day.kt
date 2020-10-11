package com.sapuseven.untis.models.untis.masterdata.timegrid

import kotlinx.serialization.Serializable

@Serializable
data class Day(
		val day: String,
		val units: List<Unit>
)