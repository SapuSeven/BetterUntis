package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class UntisHomework(
		val id: Int,
		val lessonId: Int,
		val startDate: String,
		val endDate: String,
		val text: String,
		val remark: UnknownObject?,
		val completed: Boolean,
		val attachments: List<UnknownObject>
)
