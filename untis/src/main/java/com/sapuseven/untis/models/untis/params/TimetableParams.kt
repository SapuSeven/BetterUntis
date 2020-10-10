package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.models.untis.UntisAuth
import com.sapuseven.untis.models.untis.UntisDate
import kotlinx.serialization.Serializable

@Serializable
data class TimetableParams(
		val id: Int,
		val type: String,
		val startDate: UntisDate,
		val endDate: UntisDate,
		val masterDataTimestamp: Long, // TODO: Try how the response behaves depending on changes to this value
		val timetableTimestamp: Long,
		val timetableTimestamps: List<Long>,
		val auth: UntisAuth
) : BaseParams()
