package com.sapuseven.untis.models.untis.response

import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse {
	val id: String? = null
	//val error: UntisError? = null
	val jsonrpc: String? = null
}

