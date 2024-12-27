package com.sapuseven.untis.api.model.untis

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
	val id: Long,
	val name: String,
	val url: String
)
