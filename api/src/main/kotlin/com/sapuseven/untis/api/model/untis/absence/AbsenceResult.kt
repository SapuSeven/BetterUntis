package com.sapuseven.untis.api.model.untis.absence

import kotlinx.serialization.Serializable

@Serializable
data class AbsenceResult(
	val absences: List<StudentAbsence>,
	val attendances: List<StudentAttendance>,
	val conflicts: List<StudentAbsence>,
	val separateSaveAllowed: Boolean
)
