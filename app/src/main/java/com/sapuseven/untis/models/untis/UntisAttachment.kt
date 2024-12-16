package com.sapuseven.untis.models.untis

import kotlinx.serialization.Serializable

@Serializable
data class UntisAttachment(
	val id: Int,
	val name: String,
	val url: String
)
