package com.sapuseven.untis.persistence.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.model.untis.masterdata.Student
import com.sapuseven.untis.persistence.utils.EntityMapper
import java.time.LocalDate

@Entity(
	tableName = "Student",
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class StudentEntity(
	val id: Long,
	@Transient val userId: Long = -1,
	val klasseId: Long? = null,
	val firstName: String,
	val lastName: String,
	val birthDate: LocalDate? = null
) {
	companion object : EntityMapper<Student, StudentEntity> {
		override fun map(from: Student, userId: Long) = StudentEntity(
			id = from.id,
			userId = userId,
			klasseId = from.klasseId,
			firstName = from.firstName,
			lastName = from.lastName,
			birthDate = from.birthDate,
		)
	}

	fun fullName(): String = "$firstName $lastName"
}

