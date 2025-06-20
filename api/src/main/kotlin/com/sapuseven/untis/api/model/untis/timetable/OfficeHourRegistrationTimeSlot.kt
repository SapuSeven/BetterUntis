package com.sapuseven.untis.api.model.untis.timetable

import com.sapuseven.untis.api.model.untis.enumeration.OfficeHourRegistrationTimeSlotState
import com.sapuseven.untis.api.serializer.Time
import kotlinx.serialization.Serializable


@Serializable
data class OfficeHourRegistrationTimeSlot(
	val startTime: Time,
	val endTime: Time,
	val state: OfficeHourRegistrationTimeSlotState
)
