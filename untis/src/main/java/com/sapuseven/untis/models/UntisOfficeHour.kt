package com.sapuseven.untis.models

import com.sapuseven.untis.models.untis.UntisDateTime
import kotlinx.serialization.Serializable

@Serializable
data class UntisOfficeHour(
		val id: Int,
		val startDateTime: UntisDateTime,
		val endDateTime: UntisDateTime,
		val teacherId: Int,
		val imageId: Int,
		val email: String?,
		val phone: String?,
		val displayNameRooms: String,
		val displayNameTeacher: String,
		val registrationPossible: Boolean,
		val registered: Boolean
)
