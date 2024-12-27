package com.sapuseven.untis.api.model.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.serializer.Date
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
data class Klasse(
	val id: Long = 0,
	@Transient val userId: Long = -1,
	val name: String = "",
	val longName: String = "",
	val departmentId: Long = 0,
	val startDate: Date,
	val endDate: Date,
	val foreColor: String? = "",
	val backColor: String? = "",
	val active: Boolean = false,
	val displayable: Boolean = false
) : Comparable<String> {
	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
