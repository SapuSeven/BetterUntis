package com.sapuseven.untis.api.model.untis.classreg

import com.sapuseven.untis.api.serializer.Time
import kotlinx.serialization.Serializable

@Serializable
data class Invigilator(
	val id: Long,
	val startTime: Time,
	val endTime: Time
)
