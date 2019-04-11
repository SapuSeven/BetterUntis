package com.sapuseven.untis.models.untis

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class UntisError(
		val code: Int,
		val message: String,
		@Optional val data: UntisErrorData? = null
)

@Serializable
data class UntisErrorData(
		val serverTime: Long
)