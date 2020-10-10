package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.models.untis.UntisAuth
import com.sapuseven.untis.models.untis.UntisDate
import kotlinx.serialization.Serializable

@Serializable
data class AbsenceParams(
		val startDate: UntisDate,
		val endDate: UntisDate,
		val includeExcused: Boolean,
		val includeUnExcused: Boolean,
		val auth: UntisAuth
) : BaseParams()
