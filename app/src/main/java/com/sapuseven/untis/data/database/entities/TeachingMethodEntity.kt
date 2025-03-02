package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.Holiday
import com.sapuseven.untis.api.model.untis.masterdata.TeachingMethod
import com.sapuseven.untis.data.database.Mapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
	companion object : Mapper<TeachingMethod, TeachingMethodEntity> {
		override fun map(from: TeachingMethod, userId: Long) = TeachingMethodEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
		)
	}
}
