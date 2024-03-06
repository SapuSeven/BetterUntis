package com.sapuseven.untis.api.model.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// TODO: These fields are only a guess. The actual fields are unknown as the response for the test school was empty
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
data class EventReasonGroup(
	val id: Int,
	@Transient val userId: Long = -1,
	val name: String,
	val longName: String,
	val active: Boolean
)
