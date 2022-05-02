package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class UntisAttachment(
		val id: UnknownObject?,
		val name: String,
		val url: String
) {
	init {
		UnknownObject.validate(mapOf("id" to id))
	}
}
