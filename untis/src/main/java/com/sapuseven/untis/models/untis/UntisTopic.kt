package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class UntisTopic(
		val text: String,
		val periodId: Int,
		val teachingMethodId: Int,
		val startDateTime: UntisDateTime,
		val endDateTime: UntisDateTime,
		val attachments: List<UntisAttachment>
)
