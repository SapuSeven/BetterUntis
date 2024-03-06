package com.sapuseven.untis.api.model.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class Teacher(
	val id: Int = 0,
	@Transient val userId: Long = -1,
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
