package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.AbsenceReason
import com.sapuseven.untis.data.database.Mapper

@Entity(
	tableName = "AbsenceReason",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class AbsenceReasonEntity(
	val id: Long,
	val userId: Long = -1,
	val name: String,
	val longName: String,
	val active: Boolean
) {
	companion object : Mapper<AbsenceReason, AbsenceReasonEntity> {
		override fun map(from: AbsenceReason, userId: Long) = AbsenceReasonEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			active = from.active,
		)
	}
}
