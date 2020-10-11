package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class UntisSettings(
		val showAbsenceReason: Boolean,
		val showAbsenceText: Boolean,
		val absenceCheckRequired: Boolean,
		val defaultAbsenceReasonId: Int,
		val defaultLatenessReasonId: Int,
		val defaultAbsenceEndTime: String,
		val customAbsenceEndTime: String?,
		val showCalendarDetails: Boolean = false
)
