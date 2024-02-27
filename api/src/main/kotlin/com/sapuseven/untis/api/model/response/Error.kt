package com.sapuseven.untis.api.model.response

import kotlinx.serialization.Serializable

@Serializable
data class Error(
	val code: Int,
	val message: String? = null,
	val data: UntisErrorData? = null
)

@Serializable
data class UntisErrorData(
		val exceptionTypeName: String? = null,
		val message: String? = null,
		val serverTime: Long? = null
)
