package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
	val id: Long,
	val name: String,
	val firstName: String,
	val lastName: String,
	val departmentIds: List<Long> = emptyList(),
	val foreColor: String?,
	val backColor: String?,
	val entryDate: Date?,
	val exitDate: Date?,
	val active: Boolean,
	val displayAllowed: Boolean
) : Comparable<String> {
	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| firstName.contains(other, true)
		|| lastName.contains(other, true)
	) 0 else name.compareTo(other)
}
