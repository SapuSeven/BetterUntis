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
	val text: String,
	val manualNotificationStatus: String? = null, // observed: "NOT_NOTIFIED",
	val autoNotificationStatus: String? = null, // observed: "NO_AUTO_PARENT_NOTIFICATION_AND_NO_ATTEMPT",
	val studentOfAge: Boolean? = null, // observed: false
)
