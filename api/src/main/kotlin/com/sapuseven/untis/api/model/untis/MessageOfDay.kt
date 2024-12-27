package com.sapuseven.untis.api.model.untis

data class MessageOfDay(
	val id: Long,
	val subject: String,
	val body: String,
	val attachments: List<Attachment>
)
