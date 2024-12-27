package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.AbsenceReason
import com.sapuseven.untis.api.model.untis.masterdata.Department
import com.sapuseven.untis.data.database.Mapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(
	tableName = "Department",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class DepartmentEntity(
	val id: Long,
	val userId: Long = -1,
	val name: String,
	val longName: String
) {
	companion object : Mapper<Department, DepartmentEntity> {
		override fun map(from: Department, userId: Long) = DepartmentEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
		)
	}
}
