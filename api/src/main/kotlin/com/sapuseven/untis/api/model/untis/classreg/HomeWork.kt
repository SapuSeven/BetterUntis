package com.sapuseven.untis.api.model.untis.classreg

import com.sapuseven.untis.api.model.untis.Attachment
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class HomeWork(
	val id: Long,
	val lessonId: Long,
	val startDate: Date,
	val endDate: Date,
	val text: String,
	val remark: String? = null,
	val completed: Boolean,
	val attachments: List<Attachment>
)
