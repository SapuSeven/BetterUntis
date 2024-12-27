package com.sapuseven.untis.api.model.untis.masterdata

import kotlinx.serialization.Serializable

@Serializable
data class Room(
	val id: Long,
	val name: String,
	val longName: String,
	val departmentId: Long,
	val foreColor: String?,
	val backColor: String?,
	val active: Boolean,
	val displayAllowed: Boolean
) : Comparable<String> {
	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
