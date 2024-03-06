package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.api.model.untis.Auth
import com.sapuseven.untis.api.model.untis.Time
import kotlinx.serialization.Serializable

@Serializable
data class CreateImmediateAbsenceParams(
		val periodId: Int,
		val studentId: Int,
		val startTime: Time,
		val endTime: Time,
		val auth: Auth
) : BaseParams()
