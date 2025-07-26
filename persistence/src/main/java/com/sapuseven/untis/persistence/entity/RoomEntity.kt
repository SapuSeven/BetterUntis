package com.sapuseven.untis.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.Room
import com.sapuseven.untis.persistence.utils.EntityMapper

@Entity(
	tableName = "Room",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class RoomEntity(
	override val id: Long = 0,
	override val userId: Long = -1,
	override val name: String = "",
	val longName: String = "",
	val departmentId: Long = 0,
	override val foreColor: String? = null,
	override val backColor: String? = null,
	override val active: Boolean = false,
	val displayAllowed: Boolean = false
) : ElementEntity(), Comparable<String> {
	companion object : EntityMapper<Room, RoomEntity> {
		override fun map(from: Room, userId: Long) = RoomEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			departmentId = from.departmentId,
			foreColor = from.foreColor,
			backColor = from.backColor,
			active = from.active,
			displayAllowed = from.displayAllowed,
		)
	}

	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
