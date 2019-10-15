package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class UntisOfficeHour(
		val id: Int,
		val startDateTime: String,
		val endDateTime: String,
		val teacherId: Int,
		val imageId: Int,
		val email: String?,
		val phone: String?,
		val displayNameRooms: String,
		val displayNameTeacher: String,
		val registrationPossible: Boolean,
		val registered: Boolean
)
