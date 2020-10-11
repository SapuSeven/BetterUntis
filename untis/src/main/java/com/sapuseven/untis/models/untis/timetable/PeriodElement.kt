package com.sapuseven.untis.models.untis.timetable

import kotlinx.serialization.Serializable

@Serializable
data class PeriodElement(
		val type: String,
		val id: Int,
		val orgId: Int
) : java.io.Serializable
