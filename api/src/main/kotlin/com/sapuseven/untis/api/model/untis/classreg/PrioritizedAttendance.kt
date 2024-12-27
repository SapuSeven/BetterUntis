package com.sapuseven.untis.api.model.untis.classreg

import com.sapuseven.untis.api.model.untis.enumeration.ActivityType
import kotlinx.serialization.Serializable


@Serializable
data class PrioritizedAttendance(
	val periodId: Long,
	val subjectId: Long,
	val studentId: Long,
	val activityType: ActivityType?,
	val startDateTime: String?,
	val endDateTime: String?,
	val teacherIds: List<Long?> = emptyList(),
	val klassenIds: List<Long?> = emptyList(),
	val roomIds: List<Long?> = emptyList()
)
