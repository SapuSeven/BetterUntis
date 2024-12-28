package com.sapuseven.untis.api.model.untis.masterdata

import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
	val id: Long,
	val name: String = "",
	val firstName: String = "",
	val lastName: String = "",
	val departmentIds: List<Long> = emptyList(),
	val foreColor: String? = null,
	val backColor: String? = null,
	val entryDate: Date? = null,
	val exitDate: Date? = null,
	val active: Boolean = false,
	val displayAllowed: Boolean = false
) : Comparable<String> {
	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| firstName.contains(other, true)
		|| lastName.contains(other, true)
	) 0 else name.compareTo(other)
}
