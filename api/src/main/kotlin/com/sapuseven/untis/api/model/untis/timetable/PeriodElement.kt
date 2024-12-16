package com.sapuseven.untis.api.model.untis.timetable

import kotlinx.serialization.Serializable

@Serializable
data class PeriodElement(
		val type: String,
		val id: Int,
		val orgId: Int = id
)
