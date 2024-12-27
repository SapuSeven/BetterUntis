package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.EventReasonGroup
import com.sapuseven.untis.api.model.untis.masterdata.ExcuseStatus
import com.sapuseven.untis.data.database.Mapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(
	tableName = "ExcuseStatus",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class ExcuseStatusEntity(
	val id: Long,
	val userId: Long = -1,
	val name: String,
	val longName: String,
	val excused: Boolean,
	val active: Boolean
) {
	companion object : Mapper<ExcuseStatus, ExcuseStatusEntity> {
		override fun map(from: ExcuseStatus, userId: Long) = ExcuseStatusEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			excused = from.excused,
			active = from.active,
		)
	}
}
