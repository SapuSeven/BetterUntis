package com.sapuseven.untis.models

import com.sapuseven.untis.models.untis.UntisDate
import com.sapuseven.untis.models.untis.UntisHomeworkAttachment
import kotlinx.serialization.Serializable

@Serializable
data class UntisHomework(
		val id: Int,
		val lessonId: Int,
		val startDate: UntisDate,
		val endDate: UntisDate,
		val text: String,
		val remark: String? = null,
		val completed: Boolean,
		val attachments: List<UntisHomeworkAttachment>
)
