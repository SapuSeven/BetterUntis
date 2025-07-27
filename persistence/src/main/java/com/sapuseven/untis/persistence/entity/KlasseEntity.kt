package com.sapuseven.untis.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.enumeration.ElementType
import com.sapuseven.untis.api.model.untis.masterdata.Klasse
import com.sapuseven.untis.persistence.utils.EntityMapper
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
	override val id: Long = 0,
	override val userId: Long = -1,
	override val name: String = "",
	val longName: String = "",
	val departmentId: Long = 0,
	val startDate: LocalDate? = null,
	val endDate: LocalDate? = null,
	override val foreColor: String? = "",
	override val backColor: String? = "",
	override val active: Boolean = false,
	val displayable: Boolean = false
) : ElementEntity(), Comparable<String> {
	companion object : EntityMapper<Klasse, KlasseEntity> {
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

	override fun getType() = ElementType.CLASS

	override fun getShortName(default: String) = name

	override fun getLongName(default: String) = longName

	override fun isAllowed() = displayable
}
