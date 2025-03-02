package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class SchoolYear(
	val id: Long,
	val name: String,
	val startDate: Date,
	val endDate: Date
)
