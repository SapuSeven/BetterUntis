package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.enumeration.DutyType
import com.sapuseven.untis.api.model.untis.masterdata.Duty
import com.sapuseven.untis.data.database.Mapper

@Entity(
	tableName = "Duty",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class DutyEntity(
	val id: Long,
	val userId: Long = -1,
	val name: String,
	val longName: String,
	val type: DutyType
) {
	companion object : Mapper<Duty, DutyEntity> {
		override fun map(from: Duty, userId: Long) = DutyEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			type = from.type,
		)
	}
}
