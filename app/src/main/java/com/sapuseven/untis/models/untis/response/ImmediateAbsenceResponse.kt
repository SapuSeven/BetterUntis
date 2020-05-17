package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.models.UntisAbsence
import kotlinx.serialization.Serializable

@Serializable
data class ImmediateAbsenceResponse(
		val result: ImmediateAbsenceResult? = null
) : BaseResponse()

@Serializable
data class ImmediateAbsenceResult(
		val absences: List<UntisAbsence>
)
