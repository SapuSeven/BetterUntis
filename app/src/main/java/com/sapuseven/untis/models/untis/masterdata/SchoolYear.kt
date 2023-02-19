package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class SchoolYear(
	@PrimaryKey val id: Int = 0,
	val name: String = "",
	val startDate: String = "",
	val endDate: String = ""
)
