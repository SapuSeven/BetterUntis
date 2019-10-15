package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.models.untis.UntisAuth
import com.sapuseven.untis.models.untis.UntisDate
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkParams(
		val id: Int,
		val type: String,
		val startDate: UntisDate,
		val endDate: UntisDate,
		val auth: UntisAuth
) : BaseParams()
