package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.untis.UntisError
import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
open class BaseResponse {
	val id: String? = null
	@Optional val error: UntisError? = null
	val jsonrpc: String? = null
}