package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class StudentAbsencesParams(
	val startDate: Date,
	val endDate: Date,
	val includeExcused: Boolean,
	val includeUnExcused: Boolean,
	val auth: Auth
) : BaseParams()
