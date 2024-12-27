package com.sapuseven.untis.api.model.untis.absence

import com.sapuseven.untis.api.serializer.DateTime
import kotlinx.serialization.Serializable

@Serializable
data class StudentAbsence(
	val id: Long,
	val studentId: Long,
	val klasseId: Long,
	val startDateTime: DateTime,
	val endDateTime: DateTime,
	val owner: Boolean,
	val excused: Boolean,
	val excuse: Excuse?,
	val absenceReasonId: Long,
	val absenceReason: String,
	val text: String
)
