package com.sapuseven.untis.api.model.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.api.serializer.Date
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
	primaryKeys = ["id", "userId"],
	indices = [Index("id"), Index("userId")],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class Student(
	val id: Long,
	@Transient val userId: Long = -1,
	val klasseId: Long? = null,
	val firstName: String,
	val lastName: String,
	val birthDate: Date? = null
) {
	fun fullName(): String = "$firstName $lastName"
}
