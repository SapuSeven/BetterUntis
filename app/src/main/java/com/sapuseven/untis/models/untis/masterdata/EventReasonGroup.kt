package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// TODO: These fields are only a guess. The actual fields are unknown as the response for the test school was empty
@Serializable
@Entity
data class EventReasonGroup(
	@PrimaryKey val id: Int,
	val name: String,
	val longName: String,
	val active: Boolean
)
