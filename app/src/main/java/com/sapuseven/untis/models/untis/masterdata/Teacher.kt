package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Teacher(
	@PrimaryKey val id: Int = 0,
	val name: String = "",
	val firstName: String = "",
	val lastName: String = "",
	val departmentIds: List<Int> = emptyList(),
	val foreColor: String? = null,
	val backColor: String? = null,
	val entryDate: String? = null,
	val exitDate: String? = null,
	val active: Boolean = false,
	val displayAllowed: Boolean = false
) : Comparable<String> {
	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| firstName.contains(other, true)
		|| lastName.contains(other, true)
	) 0 else name.compareTo(other)
}
