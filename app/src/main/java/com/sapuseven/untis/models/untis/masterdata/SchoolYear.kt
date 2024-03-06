package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.sapuseven.untis.data.databases.entities.User
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SchoolYear(
	val id: Int = 0,
	@Transient val userId: Long = -1,
	val name: String = "",
	val startDate: String = "",
	val endDate: String = ""
)
