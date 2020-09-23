package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisAbsence
import kotlinx.serialization.Serializable

@Serializable
data class CreateImmediateAbsenceResponse(
		val result: CreateImmediateAbsenceResult? = null
) : BaseResponse()

@Serializable
data class CreateImmediateAbsenceResult(
		val absences: List<UntisAbsence>
)
