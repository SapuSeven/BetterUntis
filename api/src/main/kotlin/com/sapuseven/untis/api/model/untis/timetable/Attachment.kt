package com.sapuseven.untis.api.model.untis.timetable

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
		val id: Int,
		val name: String,
		val url: String
)
