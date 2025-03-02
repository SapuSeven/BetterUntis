package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class PeriodDataParams(
	val ttIds: Set<Long>,
	val auth: Auth
) : BaseParams()
