package com.sapuseven.untis.models

import kotlinx.serialization.Serializable

@Serializable
data class TimetableBookmark(
	val elementId: Int,
	val elementType: String,
	val displayName: String
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
