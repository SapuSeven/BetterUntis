package com.sapuseven.untis.models.untis.masterdata

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class Duty(
	@PrimaryKey val id: Int,
	val name: String,
	val longName: String,
	val type: String
)
