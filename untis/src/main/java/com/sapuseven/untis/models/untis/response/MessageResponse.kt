package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisMessage
import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
		val result: MessageResult? = null
) : BaseResponse()

@Serializable
data class MessageResult(
		val messages: List<UntisMessage>
)
