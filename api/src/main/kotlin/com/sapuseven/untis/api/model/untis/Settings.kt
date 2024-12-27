package com.sapuseven.untis.api.model.untis

import com.sapuseven.untis.api.serializer.Time
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
	val showAbsenceReason: Boolean,
	val showAbsenceText: Boolean,
	val absenceCheckRequired: Boolean,
	val defaultAbsenceReasonId: Int,
	val defaultLatenessReasonId: Int,
	val defaultAbsenceEndTime: Time,
	val customAbsenceEndTime: Time?,
	val showCalendarDetails: Boolean = false
)
