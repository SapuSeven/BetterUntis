package com.sapuseven.untis.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.SchoolYear
import com.sapuseven.untis.persistence.utils.EntityMapper
import java.time.LocalDate

@Entity(
	tableName = "SchoolYear",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class SchoolYearEntity(
	val id: Long = 0,
	val userId: Long = -1,
	val name: String = "",
	val startDate: LocalDate,
	val endDate: LocalDate
) {
	companion object : EntityMapper<SchoolYear, SchoolYearEntity> {
		override fun map(from: SchoolYear, userId: Long) = SchoolYearEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			startDate = from.startDate,
			endDate = from.endDate,
		)
	}
}
