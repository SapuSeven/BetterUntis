package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class MessagesOfDayParams(
	val date: Date,
	val auth: Auth
) : BaseParams()
