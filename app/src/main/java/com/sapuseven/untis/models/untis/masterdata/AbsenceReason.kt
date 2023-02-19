package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import com.sapuseven.untis.data.databases.entities.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Entity(
	primaryKeys = ["id", "userId"],
	foreignKeys = [ForeignKey(
		entity = User::class,
		parentColumns = ["id"],
		childColumns = ["userId"],
		onDelete = ForeignKey.CASCADE
	)]
)
data class AbsenceReason(
	val id: Int,
	@Transient val userId: Long = -1,
	val name: String,
	val longName: String,
	val active: Boolean
)
