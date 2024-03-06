package com.sapuseven.untis.api.model.untis.masterdata.timegrid

import kotlinx.serialization.Serializable

@Serializable
data class Day(
		val day: String,
		val units: List<Unit>
)
