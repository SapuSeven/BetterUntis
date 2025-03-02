package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable


@Serializable
data class OfficeHoursParams(
	val klasseId: Long,
	val startDate: Date,
	val auth: Auth
) : BaseParams()
