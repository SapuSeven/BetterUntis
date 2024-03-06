package com.sapuseven.untis.api.model.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
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
data class Holiday(
	val id: Int = 0,
	@Transient val userId: Long = -1,
	val name: String = "",
	val longName: String = "",
	val startDate: String = "",
	val endDate: String = ""
)
