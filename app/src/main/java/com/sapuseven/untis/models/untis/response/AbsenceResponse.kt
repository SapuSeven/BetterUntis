package com.sapuseven.untis.models.untis.response

import com.sapuseven.untis.api.model.untis.absence.StudentAbsence
import kotlinx.serialization.Serializable

@Serializable
data class AbsenceResponse(
		val result: AbsenceResult? = null
) : BaseResponse()

@Serializable
data class AbsenceResult(
		val absences: List<StudentAbsence>
)
