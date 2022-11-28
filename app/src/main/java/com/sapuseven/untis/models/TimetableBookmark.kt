package com.sapuseven.untis.models

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class TimetableBookmark @OptIn(ExperimentalSerializationApi::class) constructor(
	@JsonNames("classId") val elementId: Int,
	@JsonNames("type") val elementType: String,
	val displayName: String,
	@EncodeDefault(EncodeDefault.Mode.NEVER) private val drawableId: Int = -1 // Unused, included for backwards compatibility
) {
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as TimetableBookmark

		if (elementId != other.elementId) return false
		if (elementType != other.elementType) return false

		return true
	}

	override fun hashCode(): Int {
		var result = elementId
		result = 31 * result + elementType.hashCode()
		return result
	}
}
