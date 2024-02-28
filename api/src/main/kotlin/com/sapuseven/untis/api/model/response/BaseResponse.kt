package com.sapuseven.untis.api.model.response

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse {
	val id: String? = null
	val jsonrpc: String? = null
	val error: Error? = null
}
