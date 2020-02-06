package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class UntisError(
		val code: Int,
		val message: String?,
		val data: UntisErrorData? = null
)

@Serializable
data class UntisErrorData(
		val exceptionTypeName: String? = null,
		val message: String? = null,
		val serverTime: Long? = null
)
