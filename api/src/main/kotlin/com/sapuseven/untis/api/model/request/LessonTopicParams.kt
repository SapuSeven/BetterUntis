package com.sapuseven.untis.api.model.request

import com.sapuseven.untis.api.model.untis.Auth
import kotlinx.serialization.Serializable

@Serializable
data class SubmitLessonTopicParams(
	val ttId: Long,
	val lessonTopic: String,
	val auth: Auth
) : BaseParams()
