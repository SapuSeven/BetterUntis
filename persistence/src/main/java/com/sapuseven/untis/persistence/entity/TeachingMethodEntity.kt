package com.sapuseven.untis.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.TeachingMethod
import com.sapuseven.untis.persistence.utils.EntityMapper

@Entity(
	tableName = "TeachingMethod",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class TeachingMethodEntity(
	val id: Long,
	val userId: Long = -1,
	val name: String,
	val longName: String
) {
	companion object : EntityMapper<TeachingMethod, TeachingMethodEntity> {
		override fun map(from: TeachingMethod, userId: Long) = TeachingMethodEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
		)
	}
}
