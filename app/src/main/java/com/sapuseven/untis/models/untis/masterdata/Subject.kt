package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.data.databases.entities.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Subject(
	val id: Int = 0,
	@Transient val userId: Long = -1,
	val name: String = "",
	val longName: String = "",
	val departmentIds: List<Int> = emptyList(),
	val foreColor: String? = null,
	val backColor: String? = null,
	val active: Boolean = false,
	val displayAllowed: Boolean = false
) : Comparable<String> {
	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
