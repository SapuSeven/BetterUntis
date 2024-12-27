package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class Holiday(
	val id: Long,
	val name: String,
	val longName: String,
	val startDate: Date,
	val endDate: Date
)
