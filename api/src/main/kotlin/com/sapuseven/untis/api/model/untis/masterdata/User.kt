package com.sapuseven.untis.api.model.untis.masterdata

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * This class is just a placehodler for the real user data class, which will be provided from another module
 */
@Entity
internal data class User(
	@PrimaryKey(autoGenerate = true) val id: Long
)
