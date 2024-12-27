package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.model.untis.enumeration.DutyType
import kotlinx.serialization.Serializable

@Serializable
data class Duty(
	val id: Long,
	val name: String,
	val longName: String,
	val type: DutyType
)
