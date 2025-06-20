package com.sapuseven.untis.api.model.untis.masterdata

import kotlinx.serialization.Serializable

@Serializable
data class Department(
	val id: Long,
	val name: String,
	val longName: String
)
