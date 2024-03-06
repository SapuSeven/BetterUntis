package com.sapuseven.untis.models.untis.masterdata

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AbsenceReason(
	val id: Int,
	@Transient val userId: Long = -1,
	val name: String,
	val longName: String,
	val active: Boolean
)
