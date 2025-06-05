package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.EventReasonGroup
import com.sapuseven.untis.data.database.Mapper

@Entity(
	tableName = "EventReasonGroup",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class EventReasonGroupEntity(
	val id: Long,
	val userId: Long = -1,
	val name: String,
	val longName: String,
	val active: Boolean
) {
	companion object : Mapper<EventReasonGroup, EventReasonGroupEntity> {
		override fun map(from: EventReasonGroup, userId: Long) = EventReasonGroupEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			active = from.active,
		)
	}
}
