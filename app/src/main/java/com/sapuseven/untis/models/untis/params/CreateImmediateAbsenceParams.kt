package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.api.model.untis.Auth
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable
data class CreateImmediateAbsenceParams(
	val periodId: Int,
	val studentId: Int,
	//val startTime: LocalTime,
	//val endTime: LocalTime,
	val auth: Auth
) : BaseParams()
