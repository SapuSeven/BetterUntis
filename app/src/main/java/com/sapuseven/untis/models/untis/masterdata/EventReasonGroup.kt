package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.data.databases.entities.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

// TODO: These fields are only a guess. The actual fields are unknown as the response for the test school was empty
@Serializable
data class EventReasonGroup(
	val id: Int,
	@Transient val userId: Long = -1,
	val name: String,
	val longName: String,
	val active: Boolean
)
