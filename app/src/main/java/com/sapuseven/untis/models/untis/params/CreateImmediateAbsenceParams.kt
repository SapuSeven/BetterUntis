package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.models.untis.UntisAuth
import com.sapuseven.untis.models.untis.UntisTime
import kotlinx.serialization.Serializable

@Serializable
data class CreateImmediateAbsenceParams(
		val periodId: Int,
		val studentId: Int,
		val startTime: UntisTime,
		val endTime: UntisTime,
		val auth: UntisAuth
) : BaseParams()
