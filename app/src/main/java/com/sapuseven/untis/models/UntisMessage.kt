package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class UntisMessage(
		var id: Int,
		var subject: String,
		var body: String,
		var attachments: List<UntisAttachment>
)
