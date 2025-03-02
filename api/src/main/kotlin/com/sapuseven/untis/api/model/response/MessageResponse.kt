package com.sapuseven.untis.api.model.response

import com.sapuseven.untis.api.model.untis.MessageOfDay
import kotlinx.serialization.Serializable

@Serializable
data class MessagesOfDayResponse(
		val result: MessagesOfDayResult? = null
) : BaseResponse()

@Serializable
data class MessagesOfDayResult(
		val messages: List<MessageOfDay>
)
