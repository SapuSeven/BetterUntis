package com.sapuseven.untis.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.masterdata.EventReason
import com.sapuseven.untis.persistence.utils.EntityMapper

@Entity(
	tableName = "EventReason",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class EventReasonEntity(
	val id: Long,
	val userId: Long = -1,
	val name: String,
	val longName: String,
	val elementType: ElementType,
	val groupId: Long,
	val active: Boolean
) {
	companion object : EntityMapper<EventReason, EventReasonEntity> {
		override fun map(from: EventReason, userId: Long) = EventReasonEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			elementType = from.elementType,
			groupId = from.groupId,
			active = from.active,
		)
	}
}
