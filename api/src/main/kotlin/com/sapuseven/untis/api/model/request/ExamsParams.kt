package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class ExamsParams(
	val id: Long,
	val type: ElementType,
	val startDate: Date,
	val endDate: Date,
	val auth: Auth
) : BaseParams()
