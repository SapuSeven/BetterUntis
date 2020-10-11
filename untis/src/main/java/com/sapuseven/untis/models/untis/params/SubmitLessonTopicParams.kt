package com.sapuseven.untis.models.untis.params

import com.sapuseven.untis.models.untis.UntisAuth
import kotlinx.serialization.Serializable

@Serializable
data class SubmitLessonTopicParams(
		val lessonTopic: String,
		val ttId: Int,
		val auth: UntisAuth
) : BaseParams()
