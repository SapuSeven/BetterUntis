package com.sapuseven.untis.models.untis

import com.sapuseven.untis.models.UnknownObject
import kotlinx.serialization.Serializable

@Serializable
data class UntisAttachment(
		val id: Int,
		val name: String,
		val url: String
)
