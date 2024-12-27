package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.Subject
import com.sapuseven.untis.data.database.Mapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Entity(
	tableName = "Subject",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class SubjectEntity(
	val id: Long = 0,
	val userId: Long = -1,
	val name: String = "",
	val longName: String = "",
	val departmentIds: List<Long> = emptyList(),
	val foreColor: String? = null,
	val backColor: String? = null,
	val active: Boolean = false,
	val displayAllowed: Boolean = false
) : Comparable<String> {
	companion object : Mapper<Subject, SubjectEntity> {
		override fun map(from: Subject, userId: Long) = SubjectEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			longName = from.longName,
			departmentIds = from.departmentIds,
			foreColor = from.foreColor,
			backColor = from.backColor,
			active = from.active,
			displayAllowed = from.displayAllowed,
		)
	}

	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| longName.contains(other, true)
	) 0 else name.compareTo(other)
}
