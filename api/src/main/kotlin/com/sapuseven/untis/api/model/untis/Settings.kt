package com.sapuseven.untis.api.model.untis

import com.sapuseven.untis.api.model.untis.enumeration.DefaultAbsenceEndTime
import com.sapuseven.untis.api.serializer.Time
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
	val showAbsenceReason: Boolean = false,
	val showAbsenceText: Boolean = false,
	val absenceCheckRequired: Boolean = false,
	val defaultAbsenceReasonId: Long? = null,
	val defaultLatenessReasonId: Long? = null,
	val defaultAbsenceEndTime: DefaultAbsenceEndTime? = null,
	val customAbsenceEndTime: Time? = null,
	val showCalendarDetails: Boolean = false,
	val defaultAbsenceExcuseStatusId: Long? = null
)
