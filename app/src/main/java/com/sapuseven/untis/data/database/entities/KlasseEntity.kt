package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.Klasse
import com.sapuseven.untis.data.database.Mapper
import kotlinx.serialization.Transient
import java.time.LocalDate

@Entity(
	tableName = "Klasse",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class KlasseEntity(
	val id: Long = 0,
	val userId: Long = -1,
	val name: String = "",
	val longName: String = "",
	val departmentId: Long = 0,
	val startDate: LocalDate? = null,
	val endDate: LocalDate? = null,
	val foreColor: String? = "",
	val backColor: String? = "",
	val active: Boolean = false,
	val displayable: Boolean = false
) : Comparable<String> {
	companion object : Mapper<Klasse, KlasseEntity> {
		override fun map(from: Klasse, userId: Long) = KlasseEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			departmentId = from.departmentId,
			startDate = from.startDate,
			endDate = from.endDate,
			foreColor = from.foreColor,
			backColor = from.backColor,
			active = from.active,
			displayable = from.displayable,
		)
	}

	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
