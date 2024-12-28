package com.sapuseven.untis.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.Teacher
import com.sapuseven.untis.data.database.Mapper
import java.time.LocalDate

@Entity(
	tableName = "Teacher",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class TeacherEntity(
	val id: Long = 0,
	val userId: Long = -1,
	val name: String = "",
	val firstName: String = "",
	val lastName: String = "",
	val departmentIds: List<Long> = emptyList(),
	val foreColor: String? = null,
	val backColor: String? = null,
	val entryDate: LocalDate? = null,
	val exitDate: LocalDate? = null,
	val active: Boolean = false,
	val displayAllowed: Boolean = false
) : Comparable<String> {
	companion object : Mapper<Teacher, TeacherEntity> {
		override fun map(from: Teacher, userId: Long) = TeacherEntity(
			id = from.id,
			userId = userId,
			name = from.name,
			firstName = from.firstName,
			lastName = from.lastName,
			departmentIds = from.departmentIds,
			foreColor = from.foreColor,
			backColor = from.backColor,
			entryDate = from.entryDate,
			exitDate = from.exitDate,
			active = from.active,
			displayAllowed = from.displayAllowed,
		)
	}

	override fun compareTo(other: String) = if (
		name.contains(other, true)
		|| firstName.contains(other, true)
		|| lastName.contains(other, true)
	) 0 else name.compareTo(other)
}
