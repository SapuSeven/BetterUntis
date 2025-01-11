package com.sapuseven.untis.api.model.untis

import kotlinx.serialization.Serializable

@Serializable
data class MessageOfDay(
	val id: Long,
	val subject: String,
	val body: String,
	val attachments: List<Attachment>
)
