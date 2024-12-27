package com.sapuseven.untis.api.model.untis.masterdata

import kotlinx.serialization.Serializable

@Serializable
data class TeachingMethod(
	val id: Long,
	val name: String,
	val longName: String
)
